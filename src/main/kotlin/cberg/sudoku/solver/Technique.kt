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

    object XWing : Technique {
        override fun analyze(game: Game) =
            analyze(game, rows, { row }, cols, { col }) + analyze(game, cols, { col }, rows, { row })

        private fun analyze(
            game: Game,
            primaryGroups: List<List<Position>>,
            primaryIndex: Position.() -> Int,
            secondaryGroups: List<List<Position>>,
            secondaryIndex: Position.() -> Int
        ): Sequence<Hint> = Game.symbols.asSequence().flatMap { symbol ->
            primaryGroups
                .map { primaryGroup -> game.emptySquaresOf(primaryGroup).filter { square -> symbol in square.marks } }
                .filter { primaryGroup -> primaryGroup.size == 2 }
                .groupBy { primaryGroup -> primaryGroup.map { square -> square.position.secondaryIndex() }.toSet() }
                .filterValues { primaryGroups -> primaryGroups.size == 2 }
                .mapNotNull { (secondaryIndices, primaryGroups) ->
                    // each entry has 2 primaryGroups with the symbol in the same 2 secondaryGroups
                    check(primaryGroups.size == 2 && secondaryIndices.size == 2)
                    val primaryIndices =
                        primaryGroups.map { primaryGroup -> primaryGroup.first().position.primaryIndex() }.toSet()
                    check(primaryIndices.size == 2)
                    val actions = secondaryIndices.flatMap { secondaryIndex ->
                        secondaryGroups[secondaryIndex].filterNot { position -> position.primaryIndex() in primaryIndices }
                            .map { position -> game.squareAt(position) }
                            .filter { square -> square.isEmpty() && symbol in square.marks }
                            .map { square -> Action.EraseMarks(square.position, symbol) }
                    }
                    if (actions.isNotEmpty()) {
                        val reason = Reason(primaryGroups.flatMap { primaryGroup -> positionsOf(primaryGroup) }, symbol)
                        Hint(actions, reason, this)
                    } else {
                        null
                    }
                }
        }

        override fun toString() = "X-Wing"
    }

    object GroupGroupInteraction : Technique {
        override fun analyze(game: Game): Sequence<Hint> =
            analyze(game, blocks, rows, Position::row) +
                    analyze(game, blocks, cols, Position::col) +
                    analyze(game, rows, blocks, Position::block) +
                    analyze(game, cols, blocks, Position::block)

        private fun analyze(
            game: Game,
            primaryGroups: List<List<Position>>,
            secondaryGroup: List<List<Position>>,
            secondaryIndexSelector: (Position) -> Int
        ) = Game.symbols.asSequence().flatMap { symbol ->
            primaryGroups.asSequence()
                .map { primaryGroup -> game.emptySquaresOf(primaryGroup).filter { square -> symbol in square.marks } }
                .filter { squares -> squares.size > 1 }
                .mapNotNull { squares ->
                    val positions = positionsOf(squares)
                    val secondaryIndicesInPrimaryGroupWithSymbol = positions.map(secondaryIndexSelector).toSet()
                    val actions = secondaryIndicesInPrimaryGroupWithSymbol.singleOrNull()?.let { singleIndex ->
                        game.emptySquaresOf(secondaryGroup[singleIndex])
                            .filter { square -> square.position !in positions }
                            .filter { square -> symbol in square.marks }
                            .map { square -> Action.EraseMarks(square.position, symbol) }
                    } ?: emptyList()
                    if (actions.isNotEmpty()) {
                        Hint(actions, Reason(positions, symbol), this)
                    } else {
                        null
                    }
                }
        }

        override fun toString() = "Group/Group Interaction"
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
