package cberg.sudoku.solver

import cberg.sudoku.game.*

/*
    TODO: implement Block and Column/Row Interaction
    TODO: implement Block and Block Interaction
    TODO: implement Swordfish
    https://www.kristanix.com/sudokuepic/sudoku-solving-techniques.php

    Example from sudoku.com Expert #7:
        - erase 7 from r3c2 because all 7s in b7 are in c2
        - erase 7 from b8r8 and b8r9 because all 7s in r7 are in b8
        - erase 8 from b8r8 and b8r9 because all 8s in r7 are in b8
        - erase 2 from b6r6 because all 2s in r4 are in b6
 */


sealed interface Technique {
    fun analyze(game: Game): Sequence<Hint>

    object NakedSingle : Technique {
        override fun analyze(game: Game): Sequence<Hint> = game.squares.asSequence()
            .filter { square -> square.marks.size == 1 }
            .map { square ->
                val position = square.position
                val value = square.marks.single()
                Hint(Action.SetValue(position, value), Reason(position, value), this)
            }

        override fun toString() = "Naked Single"
    }

    object HiddenSingle : Technique {
        override fun analyze(game: Game): Sequence<Hint> = groups.flatMap { group ->
            Game.symbols.asSequence().mapNotNull { symbol ->
                group.singleOrNull { position -> symbol in game.squareAt(position).marks }
                    ?.let { position ->
                        Hint(Action.SetValue(position, symbol), Reason(position, symbol), this)
                    }
            }
        }

        override fun toString() = "Hidden Single"
    }

    data class NakedTuple(private val n: Int) : Technique {
        override fun analyze(game: Game): Sequence<Hint> = groups.flatMap { group ->
            game.emptySquaresOf(group)
                .tuplesOfSize(n)
                .associateWith { squares -> squares.getMarks() }
                .filterValues { marks -> marks.size == n }
                .mapNotNull { (squares, marks) ->
                    val positions = positionsOf(squares)
                    val actions = group
                        .filterNot { position -> position in positions }
                        .associateWith { position -> marks.intersect(game.squareAt(position).marks) }
                        .filterValues { marksToErase -> marksToErase.isNotEmpty() }
                        .map { (position, marksToErase) -> Action.EraseMarks(position, marksToErase.toSet()) }
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
        override fun analyze(game: Game): Sequence<Hint> = groups.flatMap { group ->
            val emptySquares = game.emptySquaresOf(group)

            emptySquares
                .getMarks()
                .tuplesOfSize(n)
                .map { tuple -> tuple.toSet() }
                .associateWith { tuple -> emptySquares.filter { square -> square.containsMarksIn(tuple) } }
                .filterValues { squares -> squares.size == n }
                .mapNotNull { (tuple, squares) ->
                    val actions = squares
                        .associate { square -> square.position to square.marks - tuple }
                        .filterValues { marksToErase -> marksToErase.isNotEmpty() }
                        .map { (position, marksToErase) -> Action.EraseMarks(position, marksToErase) }
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

    data class XWing(private val n: Int) : Technique {
        override fun analyze(game: Game) = xWingByRow(game) + xWingByCol(game)

        private fun xWingByRow(game: Game) = Game.symbols.asSequence().flatMap { symbol ->
            rows
                .map { row -> game.emptySquaresOf(row).filter { square -> symbol in square.marks } }
                .filter { row -> row.size == n }
                .groupBy { row -> row.map { square -> square.position.col }.toSet() }
                .filterValues { rows -> rows.size == n }
                .mapNotNull { (colIndices, rows) ->
                    // each entry has n rows with the symbol in the same n columns
                    check(rows.size == n && colIndices.size == n)
                    val rowIndices = rows.map { row -> row.first().position.row }.toSet()
                    check(rowIndices.size == n)
                    val actions = colIndices.flatMap { colIndex ->
                        cols[colIndex].filterNot { position -> position.row in rowIndices }
                            .map { position -> game.squareAt(position) }
                            .filter { square -> square.isEmpty() && symbol in square.marks }
                            .map { square -> Action.EraseMarks(square.position, symbol) }
                    }
                    if (actions.isNotEmpty()) {
                        val reason = Reason(rows.flatMap { row -> positionsOf(row) }, symbol)
                        Hint(actions, reason, this)
                    } else {
                        null
                    }
                }
        }

        private fun xWingByCol(game: Game): Sequence<Hint> = Game.symbols.asSequence().flatMap { symbol ->
            cols
                .map { col -> game.emptySquaresOf(col).filter { square -> symbol in square.marks } }
                .filter { col -> col.size == n }
                .groupBy { col -> col.map { square -> square.position.row }.toSet() }
                .filterValues { cols -> cols.size == n }
                .mapNotNull { (rowIndices, cols) ->
                    // each entry has n cols with the symbol in the same n rows
                    check(cols.size == n && rowIndices.size == n)
                    val colIndices = cols.map { col -> col.first().position.col }.toSet()
                    check(colIndices.size == n)
                    val actions = rowIndices.flatMap { rowIndex ->
                        rows[rowIndex].filterNot { position -> position.col in colIndices }
                            .map { position -> game.squareAt(position) }
                            .filter { square -> square.isEmpty() && symbol in square.marks }
                            .map { square -> Action.EraseMarks(square.position, symbol) }
                    }
                    if (actions.isNotEmpty()) {
                        val reason = Reason(cols.flatMap { col -> positionsOf(col) }, symbol)
                        Hint(actions, reason, this)
                    } else {
                        null
                    }
                }
        }

        override fun toString() = "X-Wing ($n)"
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

private fun Game.emptySquaresOf(group: List<Position>) =
    group.map { position -> squareAt(position) }
        .filter { square -> square.isEmpty() }

private fun Square.containsMarksIn(tuple: Set<Char>) = marks.any { mark -> mark in tuple }

private fun positionsOf(squares: Iterable<Square>) = squares.map { square -> square.position }

private fun Iterable<Square>.getMarks(): Set<Char> = fold(mutableSetOf()) { marks, square ->
    marks.apply { addAll(square.marks) }
}

private fun <E> Iterable<E>.tuplesOfSize(n: Int): Iterable<List<E>> {
    require(n >= 1)

    return if (n == 1) {
        map { listOf(it) }
    } else {
        flatMapIndexed { i, v -> drop(i + 1).tuplesOfSize(n - 1).map { it + v } }
    }
}
