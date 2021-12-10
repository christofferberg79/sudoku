package cberg.sudoku.solver

import cberg.sudoku.game.*

/*
    TODO: implement more techniques

    - Finned X-Wing
    - Swordfish
    - Finned Swordfish
    - Sashimi Swordfish
    - Forcing Chain
    - XY-Wing
    - Unique Rectangle (types 1-5)
    - Nishio

    https://www.sudokuonline.io/tips/advanced-sudoku-strategies
    https://www.sudokuoftheday.com/techniques/
    https://www.sudopedia.org/wiki/Main_Page
 */


abstract class Technique(private val name: String) {
    fun analyze(grid: Grid) = grid.analyzeInternal()
    protected abstract fun Grid.analyzeInternal(): Sequence<Hint>
    override fun toString() = name
}

object NakedSingle : Technique("Naked Single") {
    override fun Grid.analyzeInternal() = cells().mapNotNull { cell ->
        cell.candidates.singleOrNull()?.let { candidate ->
            Hint(Action.SetDigit(cell.position, candidate), Reason(cell.position, candidate), this@NakedSingle)
        }
    }
}

object HiddenSingle : Technique("Hidden Single") {
    override fun Grid.analyzeInternal() = singleDigit { digit ->
        houses().mapNotNull { house ->
            house.singleOrNull { position -> digit in position.candidates }
                ?.let { position ->
                    Hint(Action.SetDigit(position, digit), Reason(position, digit), this@HiddenSingle)
                }
        }
    }
}

abstract class TupleBase(private val n: Int, name: String) : Technique(name) {
    override fun Grid.analyzeInternal() = houses().flatMap { house ->
        house.candidates.toList().tuplesOfSize(n).mapNotNull { tuple ->
            analyze(house.toSet(), tuple.toSet())
        }
    }

    protected abstract fun Grid.analyze(house: Set<Position>, tuple: Set<Int>): Hint?
}

class NakedTuple(n: Int) : TupleBase(n, "Naked ${tupleString(n)}") {
    override fun Grid.analyze(house: Set<Position>, tuple: Set<Int>): Hint? {
        val (naked, rest) = house.filter { it.isEmpty() }.partition { tuple.containsAll(it.candidates) }
        if (naked.size != tuple.size) {
            return null
        }
        val toErase = rest.associateWith { tuple intersect it.candidates }.filterValues { it.isNotEmpty() }
        if (toErase.isEmpty()) {
            return null
        }

        val reason = Reason(naked, tuple)
        val actions = toErase.map { (position, candidates) -> Action.EraseCandidates(position, candidates) }

        return Hint(actions, reason, this@NakedTuple)
    }
}

class HiddenTuple(n: Int) : TupleBase(n, "Hidden ${tupleString(n)}") {
    override fun Grid.analyze(house: Set<Position>, tuple: Set<Int>): Hint? {
        val hidden = house.filter { position -> position.candidates.any { it in tuple } }
        if (hidden.size != tuple.size) {
            return null
        }
        val toErase = hidden.associateWith { it.candidates - tuple }.filterValues { it.isNotEmpty() }
        if (toErase.isEmpty()) {
            return null
        }

        val reason = Reason(hidden, tuple)
        val actions = toErase.map { (position, candidates) -> Action.EraseCandidates(position, candidates) }

        return Hint(actions, reason, this@HiddenTuple)
    }
}

abstract class XWingBase(private val groupSize: Int, name: String) : Technique(name) {
    override fun Grid.analyzeInternal() = analyze(rows(), Position::col) + analyze(cols(), Position::row)

    private fun Grid.analyze(lines: Sequence<List<Position>>, coordinate: Position.() -> Int) =
        singleDigit { digit ->
            lines
                .map { line ->
                    line.filter { position -> digit in position.candidates }
                }
                .filter { line -> line.size == 2 }
                .tuplesOfSize(2)
                .map { tuples -> tuples.flatten() }
                .map { positions -> positions.groupBy { position -> coordinate(position) } }
                .filter { groups -> groups.size == groupSize }
                .mapNotNull { groups ->
                    val actions = getActionPositions(groups)
                        .filter { position -> digit in position.candidates }
                        .map { position -> Action.EraseCandidates(position, digit) }

                    if (actions.isNotEmpty()) {
                        val reason = Reason(groups.values.flatten(), digit)
                        Hint(actions, reason, this@XWingBase)
                    } else {
                        null
                    }
                }
        }

    abstract fun Grid.getActionPositions(groups: Map<Int, List<Position>>): Collection<Position>

}

object XWing : XWingBase(2, "X-Wing") {
    override fun Grid.getActionPositions(groups: Map<Int, List<Position>>): List<Position> {
        return groups.values.flatMap { (pos1, pos2) ->
            if (pos1.row == pos2.row) {
                rows.elementAt(pos1.row) - pos1 - pos2
            } else {
                cols.elementAt(pos1.col) - pos1 - pos2
            }
        }
    }
}

object SashimiXWing : XWingBase(3, "Sashimi X-Wing") {
    override fun Grid.getActionPositions(groups: Map<Int, List<Position>>): Collection<Position> {
        val sashimiPositions = groups.values.mapNotNull { group -> group.singleOrNull() }
        return sashimiPositions.commonPeers()
    }
}

object LockedCandidates : Technique("Locked Candidates") {
    override fun Grid.analyzeInternal() = singleDigit { digit ->
        lines().flatMap { line ->
            boxes().mapNotNull { box ->
                analyze(line.toSet(), box.toSet(), digit)
            }
        }
    }

    private fun Grid.analyze(line: Set<Position>, box: Set<Position>, digit: Int): Hint? {
        val intersection = box intersect line
        if (intersection.none { position -> digit in position.candidates }) {
            return null
        }

        val restOfLine = line - intersection
        val linePositionsWithCandidate = restOfLine.filter { position -> digit in position.candidates }

        val restOfBox = box - intersection
        val boxPositionsWithCandidate = restOfBox.filter { position -> digit in position.candidates }

        val toErase = when {
            linePositionsWithCandidate.isEmpty() -> boxPositionsWithCandidate
            boxPositionsWithCandidate.isEmpty() -> linePositionsWithCandidate
            else -> emptyList()
        }

        if (toErase.isEmpty()) {
            return null
        }

        val actions = toErase.map { position -> Action.EraseCandidates(position, digit) }
        val reason = Reason(intersection.toList(), digit)
        return Hint(actions, reason, this@LockedCandidates)
    }
}

private fun digits() = DIGITS.asSequence()
private fun Grid.cells() = cells.asSequence()
private fun houses() = houses.asSequence()
private fun rows() = rows.asSequence()
private fun cols() = cols.asSequence()
private fun boxes() = boxes.asSequence()
private fun lines() = lines.asSequence()

private fun List<Position>.commonPeers() = map(Position::peers)
    .reduce { peers1, peers2 -> peers1 intersect peers2 }

private fun singleDigit(block: (digit: Int) -> Sequence<Hint>) = digits().flatMap(block)
