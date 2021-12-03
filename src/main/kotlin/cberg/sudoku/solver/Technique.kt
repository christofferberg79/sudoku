package cberg.sudoku.solver

import cberg.sudoku.game.*

/*
    TODO: implement more techniques

    - Finned X-Wing
    - Sashimi X-Wing
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
        override fun analyze(grid: Grid): Sequence<Hint> = houses.flatMap { house ->
            Grid.digits.asSequence().mapNotNull { digit ->
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
                .associateWith { cells -> cells.getMarks() }
                .filterValues { marks -> marks.size == n }
                .mapNotNull { (cells, marks) ->
                    val positions = positionsOf(cells)
                    val actions = house
                        .filterNot { position -> position in positions }
                        .associateWith { position -> marks.intersect(grid.cellAt(position).candidates) }
                        .filterValues { marksToErase -> marksToErase.isNotEmpty() }
                        .map { (position, marksToErase) -> Action.EraseCandidates(position, marksToErase.toSet()) }
                    if (actions.isNotEmpty()) {
                        val reason = Reason(positions, marks)
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

            emptyCells
                .getMarks()
                .tuplesOfSize(n)
                .map { tuple -> tuple.toSet() }
                .associateWith { tuple -> emptyCells.filter { cell -> cell.containsMarksIn(tuple) } }
                .filterValues { cells -> cells.size == n }
                .mapNotNull { (tuple, cells) ->
                    val actions = cells
                        .associate { cell -> cell.position to cell.candidates - tuple }
                        .filterValues { marksToErase -> marksToErase.isNotEmpty() }
                        .map { (position, marksToErase) -> Action.EraseCandidates(position, marksToErase) }
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

    object XWing : Technique {
        override fun analyze(grid: Grid) =
            analyze(grid, rows, { row }, cols, { col }) + analyze(grid, cols, { col }, rows, { row })

        private fun analyze(
            grid: Grid,
            primaryHouses: List<List<Position>>,
            primaryIndex: Position.() -> Int,
            secondaryHouses: List<List<Position>>,
            secondaryIndex: Position.() -> Int
        ): Sequence<Hint> = Grid.digits.asSequence().flatMap { digit ->
            primaryHouses
                .map { primaryHouse ->
                    grid.emptyCellsOf(primaryHouse).filter { cell -> digit in cell.candidates }
                }
                .filter { primaryHouse -> primaryHouse.size == 2 }
                .groupBy { primaryHouse -> primaryHouse.map { cell -> cell.position.secondaryIndex() }.toSet() }
                .filterValues { primaryHouses -> primaryHouses.size == 2 }
                .mapNotNull { (secondaryIndices, primaryHouses) ->
                    // each entry has 2 primaryHouses with the digit in the same 2 secondaryHouses
                    check(primaryHouses.size == 2 && secondaryIndices.size == 2)
                    val primaryIndices =
                        primaryHouses.map { primaryHouse -> primaryHouse.first().position.primaryIndex() }.toSet()
                    check(primaryIndices.size == 2)
                    val actions = secondaryIndices.flatMap { secondaryIndex ->
                        secondaryHouses[secondaryIndex].filterNot { position -> position.primaryIndex() in primaryIndices }
                            .map { position -> grid.cellAt(position) }
                            .filter { cell -> cell.isEmpty() && digit in cell.candidates }
                            .map { cell -> Action.EraseCandidates(cell.position, digit) }
                    }
                    if (actions.isNotEmpty()) {
                        val reason = Reason(primaryHouses.flatMap { primaryHouse -> positionsOf(primaryHouse) }, digit)
                        Hint(actions, reason, this)
                    } else {
                        null
                    }
                }
        }

        override fun toString() = "X-Wing"
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
            return Grid.digits.asSequence().flatMap { digit ->
                lines.flatMap { line ->
                    boxes.mapNotNull { box ->
                        grid.analyze(line.toSet(), box.toSet(), digit)
                    }
                }
            }
        }

        private fun Grid.analyze(line: Set<Position>, box: Set<Position>, digit: Char): Hint? {
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

private fun Cell.containsMarksIn(tuple: Set<Char>) = candidates.any { mark -> mark in tuple }

private fun positionsOf(cells: Iterable<Cell>) = cells.map { cell -> cell.position }

private fun Iterable<Cell>.getMarks(): Set<Char> = fold(mutableSetOf()) { marks, cell ->
    marks.apply { addAll(cell.candidates) }
}

private fun <E> Iterable<E>.tuplesOfSize(n: Int): Iterable<List<E>> {
    require(n >= 1)

    return if (n == 1) {
        map { listOf(it) }
    } else {
        flatMapIndexed { i, v -> drop(i + 1).tuplesOfSize(n - 1).map { it + v } }
    }
}
