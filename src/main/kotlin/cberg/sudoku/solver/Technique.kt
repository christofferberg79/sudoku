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


sealed interface Technique {
    fun analyze(grid: Grid): Sequence<Hint>

    object NakedSingle : Technique {
        override fun analyze(grid: Grid): Sequence<Hint> = grid.cells.asSequence().mapNotNull { cell ->
            cell.candidates.singleOrNull()?.let { candidate ->
                val position = cell.position
                Hint(Action.SetDigit(position, candidate), Reason(position, candidate), this)
            }
        }

        override fun toString() = "Naked Single"
    }

    object HiddenSingle : Technique {
        override fun analyze(grid: Grid): Sequence<Hint> = singleDigit { digit ->
            houses.mapNotNull { house ->
                house.singleOrNull { position -> digit in grid.cellAt(position).candidates }
                    ?.let { position ->
                        Hint(Action.SetDigit(position, digit), Reason(position, digit), this)
                    }
            }
        }

        override fun toString() = "Hidden Single"
    }

    data class NakedTuple(private val n: Int) : Technique {
        override fun analyze(grid: Grid): Sequence<Hint> = houses.flatMap { house ->
            grid.emptyCellsOf(house)
                .tuplesOfSize(n)
                .associateWith { cells -> candidatesOf(cells) }
                .filterValues { candidates -> candidates.size == n }
                .mapNotNull { (cells, candidates) ->
                    val positions = positionsOf(cells)
                    val actions = house
                        .filterNot { position -> position in positions }
                        .associateWith { position -> candidates.intersect(grid.cellAt(position).candidates) }
                        .filterValues { candidatesToErase -> candidatesToErase.isNotEmpty() }
                        .map { (position, candidatesToErase) ->
                            Action.EraseCandidates(position, candidatesToErase)
                        }
                    if (actions.isNotEmpty()) {
                        val reason = Reason(positions, candidates)
                        Hint(actions, reason, this)
                    } else {
                        null
                    }
                }
        }

        override fun toString() = "Naked ${tupleString(n)}"
    }

    data class HiddenTuple(private val n: Int) : Technique {
        override fun analyze(grid: Grid): Sequence<Hint> = houses.flatMap { house ->
            val emptyCells = grid.emptyCellsOf(house)

            candidatesOf(emptyCells).toList()
                .tuplesOfSize(n)
                .map { tuple -> tuple.toSet() }
                .associateWith { tuple -> emptyCells.filter { cell -> cell.containsCandidatesIn(tuple) } }
                .filterValues { cells -> cells.size == n }
                .mapNotNull { (tuple, cells) ->
                    val actions = cells
                        .associate { cell -> cell.position to cell.candidates - tuple }
                        .filterValues { candidatesToErase -> candidatesToErase.isNotEmpty() }
                        .map { (position, candidatesToErase) -> Action.EraseCandidates(position, candidatesToErase) }
                    if (actions.isNotEmpty()) {
                        val reason = Reason(positionsOf(cells), tuple)
                        Hint(actions, reason, this)
                    } else {
                        null
                    }
                }
        }

        override fun toString() = "Hidden ${tupleString(n)}"
    }

    abstract class XWingBase(private val groupSize: Int) : Technique {
        override fun analyze(grid: Grid) = grid.analyze(rows, Position::col) + grid.analyze(cols, Position::row)

        private fun Grid.analyze(lines: List<List<Position>>, coordinate: Position.() -> Int) =
            singleDigit { digit ->
                lines.asSequence()
                    .map { line -> emptyCellsOf(line)
                        .filter { cell -> digit in cell.candidates }
                        .map { cell -> cell.position }}
                    .filter { line -> line.size == 2 }
                    .tuplesOfSize(2)
                    .map { tuples -> tuples.flatten() }
                    .map { positions -> positions.groupBy { position -> coordinate(position) } }
                    .filter { groups -> groups.size == groupSize }
                    .mapNotNull { groups ->
                        val actions = getActionPositions(groups)
                            .map { pos -> cellAt(pos) }
                            .filter { cell -> digit in cell.candidates }
                            .map { cell -> Action.EraseCandidates(cell.position, digit) }

                        if (actions.isNotEmpty()) {
                            val reason = Reason(groups.values.flatten(), digit)
                            Hint(actions, reason, this@XWingBase)
                        } else {
                            null
                        }
                    }
            }

        abstract fun getActionPositions(groups: Map<Int, List<Position>>): Collection<Position>

    }

    object XWing : XWingBase(2) {
        override fun getActionPositions(groups: Map<Int, List<Position>>): List<Position> {
            val positions: List<Position> = groups.values.flatMap { (pos1, pos2) ->
                if (pos1.row == pos2.row) {
                    rows[pos1.row] - pos1 - pos2
                } else {
                    cols[pos1.col] - pos1 - pos2
                }
            }
            return positions
        }

        override fun toString() = "X-Wing"
    }

    object SashimiXWing : XWingBase(3) {
        override fun getActionPositions(groups: Map<Int, List<Position>>): Set<Position> {
            val sashimiCells = groups.values.mapNotNull { group -> group.singleOrNull() }
            return sashimiCells.commonPeers()
        }

        override fun toString() = "Sashimi X-Wing"
    }

    object LockedCandidates : Technique {
        override fun analyze(grid: Grid): Sequence<Hint> {
            return analyze(grid, lines, boxes.asSequence())
        }

        private fun analyze(
            grid: Grid,
            lines: Sequence<List<Position>>,
            boxes: Sequence<List<Position>>
        ): Sequence<Hint> {
            return singleDigit { digit ->
                lines.flatMap { line ->
                    boxes.mapNotNull { box ->
                        grid.analyze(line.toSet(), box.toSet(), digit)
                    }
                }
            }
        }

        private fun Grid.analyze(line: Set<Position>, box: Set<Position>, digit: Int): Hint? {
            val intersection = box.intersect(line)
            if (intersection.none { it.hasCandidate(digit) }) {
                return null
            }

            val restOfLine = line - intersection
            val lineCellsWithCandidate = restOfLine.filter { it.hasCandidate(digit) }

            val restOfBox = box - intersection
            val boxCellsWithCandidate = restOfBox.filter { it.hasCandidate(digit) }

            val toErase = when {
                lineCellsWithCandidate.isEmpty() -> boxCellsWithCandidate
                boxCellsWithCandidate.isEmpty() -> lineCellsWithCandidate
                else -> emptyList()
            }

            if (toErase.isEmpty()) {
                return null
            }

            val actions = toErase.map { position -> Action.EraseCandidates(position, digit) }
            val reason = Reason(intersection.toList(), digit)
            return Hint(actions, reason, this@LockedCandidates)
        }

        override fun toString() = "Locked Candidates"
    }
}

private fun List<Position>.commonPeers() =
    map { position -> position.peers() }
        .reduce { peers1, peers2 -> peers1.intersect(peers2) }

fun singleDigit(block: (digit: Int) -> Sequence<Hint>): Sequence<Hint> =
    Grid.digits.asSequence().flatMap(block)

private fun tupleString(n: Int): String {
    require(n >= 2)
    return when (n) {
        2 -> "Pair"
        3 -> "Triple"
        else -> "$n-tuple"
    }
}

private fun Grid.emptyCellsOf(house: List<Position>) =
    house.map { position -> cellAt(position) }
        .filter { cell -> cell.isEmpty() }

private fun Cell.containsCandidatesIn(tuple: Set<Int>) = candidates.any { candidate -> candidate in tuple }

private fun positionsOf(cells: Iterable<Cell>) = cells.map { cell -> cell.position }

@OptIn(ExperimentalStdlibApi::class)
private fun candidatesOf(cells: Iterable<Cell>): Set<Int> = buildSet {
    cells.forEach { cell -> addAll(cell.candidates) }
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
