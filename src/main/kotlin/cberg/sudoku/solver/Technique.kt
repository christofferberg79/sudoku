package cberg.sudoku.solver

import cberg.sudoku.game.*

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

private fun <E> Iterable<E>.tuplesOfSize(n: Int): Iterable<Set<E>> {
    require(n >= 1)

    return if (n == 1) {
        map { setOf(it) }
    } else {
        flatMapIndexed { i, v -> drop(i + 1).tuplesOfSize(n - 1).map { it + v } }
    }
}
