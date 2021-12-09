package cberg.sudoku.solver

import cberg.sudoku.game.Grid
import cberg.sudoku.game.Position

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
    fun analyze(grid: Grid) = grid._analyze()
    abstract fun Grid._analyze(): Sequence<Hint>
    override fun toString() = name
}

object NakedSingle : Technique("Naked Single") {
    override fun Grid._analyze(): Sequence<Hint> = cells.asSequence().mapNotNull { cell ->
        cell.candidates.singleOrNull()?.let { candidate ->
            Hint(Action.SetDigit(cell.position, candidate), Reason(cell.position, candidate), this@NakedSingle)
        }
    }
}

object HiddenSingle : Technique("Hidden Single") {
    override fun Grid._analyze(): Sequence<Hint> = singleDigit { digit ->
        houses().mapNotNull { house ->
            house.singleOrNull { position -> digit in position.candidates }
                ?.let { position ->
                    Hint(Action.SetDigit(position, digit), Reason(position, digit), this@HiddenSingle)
                }
        }
    }
}

class NakedTuple(private val n: Int) : Technique("Naked ${tupleString(n)}") {
    override fun Grid._analyze(): Sequence<Hint> = houses().flatMap { house ->
        house.filter { position -> position.isEmpty() }
            .tuplesOfSize(n)
            .associateWith { positions -> positions.candidates }
            .filterValues { candidates -> candidates.size == n }
            .mapNotNull { (positions, candidates) ->
                val actions = house
                    .filterNot { position -> position in positions }
                    .associateWith { position -> candidates intersect position.candidates }
                    .filterValues { candidatesToErase -> candidatesToErase.isNotEmpty() }
                    .map { (position, candidatesToErase) ->
                        Action.EraseCandidates(position, candidatesToErase)
                    }
                if (actions.isNotEmpty()) {
                    val reason = Reason(positions, candidates)
                    Hint(actions, reason, this@NakedTuple)
                } else {
                    null
                }
            }
    }
}

class HiddenTuple(private val n: Int) : Technique("Hidden ${tupleString(n)}") {
    override fun Grid._analyze(): Sequence<Hint> = houses().flatMap { house ->
        val emptyPositions = house.filter { position -> position.isEmpty() }

        emptyPositions.candidates.toList()
            .tuplesOfSize(n)
            .map { tuple -> tuple.toSet() }
            .associateWith { tuple -> emptyPositions.filter { position -> position.containsCandidatesIn(tuple) } }
            .filterValues { positions -> positions.size == n }
            .mapNotNull { (tuple, positions) ->
                val actions = positions
                    .associateWith { position -> position.candidates - tuple }
                    .filterValues { candidatesToErase -> candidatesToErase.isNotEmpty() }
                    .map { (position, candidatesToErase) -> Action.EraseCandidates(position, candidatesToErase) }
                if (actions.isNotEmpty()) {
                    val reason = Reason(positions, tuple)
                    Hint(actions, reason, this@HiddenTuple)
                } else {
                    null
                }
            }
    }
}

abstract class XWingBase(private val groupSize: Int, name: String) : Technique(name) {
    override fun Grid._analyze() = analyze(rows(), Position::col) + analyze(cols(), Position::row)

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
                rows().elementAt(pos1.row) - pos1 - pos2
            } else {
                cols().elementAt(pos1.col) - pos1 - pos2
            }
        }
    }
}

object SashimiXWing : XWingBase(3, "Sashimi X-Wing") {
    override fun Grid.getActionPositions(groups: Map<Int, List<Position>>): Collection<Position> {
        val sashimiPositions = groups.values.mapNotNull { group -> group.singleOrNull() }
        return commonPeers(sashimiPositions)
    }
}

object LockedCandidates : Technique("Locked Candidates") {
    override fun Grid._analyze(): Sequence<Hint> = singleDigit { digit ->
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

fun singleDigit(block: (digit: Int) -> Sequence<Hint>): Sequence<Hint> =
    Grid.digits.asSequence().flatMap(block)

private fun tupleString(n: Int): String {
    require(n >= 1)
    return when (n) {
        1 -> "Single"
        2 -> "Pair"
        3 -> "Triple"
        4 -> "Quadruple"
        5 -> "Quintuple"
        else -> "$n-tuple"
    }
}

fun <E> List<E>.tuplesOfSize(n: Int): List<List<E>> {
    require(n >= 1) { "n must be >= 1 but is $n" }

    return combinations(size, n).map { it.map { i -> this[i] } }
}

private fun combinations(n: Int, k: Int): List<List<Int>> {
    var combinations = (0..n - k).map { listOf(it) }
    for (i in 1 until k) {
        combinations = combinations.flatMap { partial ->
            (partial.last() + 1..n - k + i).map { next -> partial + next }
        }
    }
    return combinations
}

private fun <E> Sequence<E>.tuplesOfSize(n: Int): Sequence<List<E>> {
    require(n >= 1)

    return if (n == 1) {
        map { listOf(it) }
    } else {
        flatMapIndexed { i, v -> drop(i + 1).tuplesOfSize(n - 1).map { it + v } }
    }
}
