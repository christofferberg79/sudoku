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
        override fun analyze(grid: Grid): Sequence<Hint> = grid.cells.asSequence()
            .filter { square -> square.candidates.size == 1 }
            .map { square ->
                val position = square.position
                val value = square.candidates.single()
                Hint(Action.SetDigit(position, value), Reason(position, value), this)
            }

        override fun toString() = "Naked Single"
    }

    object HiddenSingle : Technique {
        override fun analyze(grid: Grid): Sequence<Hint> = houses.flatMap { group ->
            Grid.digits.asSequence().mapNotNull { digit ->
                group.singleOrNull { position -> digit in grid.cellAt(position).candidates }
                    ?.let { position ->
                        Hint(Action.SetDigit(position, digit), Reason(position, digit), this)
                    }
            }
        }

        override fun toString() = "Hidden Single"
    }

    data class NakedTuple(private val n: Int) : Technique {
        override fun analyze(grid: Grid): Sequence<Hint> = houses.flatMap { group ->
            grid.emptySquaresOf(group)
                .tuplesOfSize(n)
                .associateWith { squares -> squares.getMarks() }
                .filterValues { marks -> marks.size == n }
                .mapNotNull { (squares, marks) ->
                    val positions = positionsOf(squares)
                    val actions = group
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
        override fun analyze(grid: Grid): Sequence<Hint> = houses.flatMap { group ->
            val emptySquares = grid.emptySquaresOf(group)

            emptySquares
                .getMarks()
                .tuplesOfSize(n)
                .map { tuple -> tuple.toSet() }
                .associateWith { tuple -> emptySquares.filter { square -> square.containsMarksIn(tuple) } }
                .filterValues { squares -> squares.size == n }
                .mapNotNull { (tuple, squares) ->
                    val actions = squares
                        .associate { square -> square.position to square.candidates - tuple }
                        .filterValues { marksToErase -> marksToErase.isNotEmpty() }
                        .map { (position, marksToErase) -> Action.EraseCandidates(position, marksToErase) }
                    if (actions.isNotEmpty()) {
                        val reason = Reason(positionsOf(squares), tuple)
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
            primaryGroups: List<List<Position>>,
            primaryIndex: Position.() -> Int,
            secondaryGroups: List<List<Position>>,
            secondaryIndex: Position.() -> Int
        ): Sequence<Hint> = Grid.digits.asSequence().flatMap { digit ->
            primaryGroups
                .map { primaryGroup ->
                    grid.emptySquaresOf(primaryGroup).filter { square -> digit in square.candidates }
                }
                .filter { primaryGroup -> primaryGroup.size == 2 }
                .groupBy { primaryGroup -> primaryGroup.map { square -> square.position.secondaryIndex() }.toSet() }
                .filterValues { primaryGroups -> primaryGroups.size == 2 }
                .mapNotNull { (secondaryIndices, primaryGroups) ->
                    // each entry has 2 primaryGroups with the digit in the same 2 secondaryGroups
                    check(primaryGroups.size == 2 && secondaryIndices.size == 2)
                    val primaryIndices =
                        primaryGroups.map { primaryGroup -> primaryGroup.first().position.primaryIndex() }.toSet()
                    check(primaryIndices.size == 2)
                    val actions = secondaryIndices.flatMap { secondaryIndex ->
                        secondaryGroups[secondaryIndex].filterNot { position -> position.primaryIndex() in primaryIndices }
                            .map { position -> grid.cellAt(position) }
                            .filter { square -> square.isEmpty() && digit in square.candidates }
                            .map { square -> Action.EraseCandidates(square.position, digit) }
                    }
                    if (actions.isNotEmpty()) {
                        val reason = Reason(primaryGroups.flatMap { primaryGroup -> positionsOf(primaryGroup) }, digit)
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

        private fun analyze(grid: Grid, lines: Sequence<List<Position>>, boxes: Sequence<List<Position>>): Sequence<Hint> {
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

private fun Grid.emptySquaresOf(group: List<Position>) =
    group.map { position -> cellAt(position) }
        .filter { square -> square.isEmpty() }

private fun Cell.containsMarksIn(tuple: Set<Char>) = candidates.any { mark -> mark in tuple }

private fun positionsOf(squares: Iterable<Cell>) = squares.map { square -> square.position }

private fun Iterable<Cell>.getMarks(): Set<Char> = fold(mutableSetOf()) { marks, square ->
    marks.apply { addAll(square.candidates) }
}

private fun <E> Iterable<E>.tuplesOfSize(n: Int): Iterable<List<E>> {
    require(n >= 1)

    return if (n == 1) {
        map { listOf(it) }
    } else {
        flatMapIndexed { i, v -> drop(i + 1).tuplesOfSize(n - 1).map { it + v } }
    }
}
