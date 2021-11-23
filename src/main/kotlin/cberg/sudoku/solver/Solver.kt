package cberg.sudoku.solver

import cberg.sudoku.game.*

class Solver {
    fun solve(input: String): Solution {
        val game = Game(input).writePencilMarks()

        if (game.isInvalidPuzzle()) {
            return InvalidPuzzle
        }

        val solvedGame = solve(game)

        return if (solvedGame.squares.any(Square::isEmpty)) {
            TooHard
        } else {
            UniqueSolution(solvedGame.getOutput())
        }
    }

    private fun Game.isInvalidPuzzle() = insufficientGivens()
            || duplicateGivens()
            || noCandidate()
            || missingCandidate()

    private fun Game.insufficientGivens() = squares.count(Square::isNotEmpty) < 17

    private fun Game.duplicateGivens() = groups.any { group ->
        group.map { position -> squareAt(position) }
            .filter(Square::isNotEmpty)
            .groupingBy(Square::value)
            .eachCount()
            .values.any { it > 1 }
    }

    private fun Game.noCandidate() = squares.any { square ->
        square.isEmpty() && square.marks.isEmpty()
    }

    private fun Game.missingCandidate() = symbols.any { symbol ->
        groups.any { group ->
            group.map { position -> squareAt(position) }
                .none { square -> symbol == square.value || symbol in square.marks }
        }
    }

    private fun Game.getOutput() = squares.joinToString(separator = "") { s -> "${s.value ?: '.'}" }

}

val symbols = '1'..'9'

sealed class Solution
class UniqueSolution(val solution: String) : Solution()
object InvalidPuzzle : Solution()
object TooHard : Solution()

sealed interface Action {
    operator fun invoke(game: Game): Game

    class SetValue(private val position: Position, private val value: Char) : Action {
        override fun invoke(game: Game) = game.setValueAndEraseMarks(position, value)
        override fun toString() = "$position => $value"
    }

    class EraseMarks(private val position: Position, private val marks: List<Char>) : Action {
        override fun invoke(game: Game) = marks.fold(game) { g, m -> g.eraseMark(position, m) }
        override fun toString() = "$position => erase marks: ${marks.joinToString()}"
    }
}

fun solve(game: Game) = generateSequence(game, ::next).last()

private fun next(game: Game) = game.actions().firstOrNull()?.let { action -> action(game) }

fun Game.actions(): Sequence<Action> = nakedSingles() +
        hiddenSingles() +
        nakedTuples(2) +
        nakedTuples(3)

private fun Game.nakedSingles(): Sequence<Action> = squares.asSequence()
    .filter { square -> square.marks.size == 1 }
    .map { square -> Action.SetValue(square.position, square.marks.single()) }

private fun Game.hiddenSingles(): Sequence<Action> = groups.flatMap { group ->
    symbols.asSequence().mapNotNull { symbol ->
        group.singleOrNull { position -> symbol in squareAt(position).marks }
            ?.let { position -> Action.SetValue(position, symbol) }
    }
}

private fun Game.nakedTuples(n: Int): Sequence<Action> = groups.flatMap { group ->
    group.asSequence()
        .map { position -> squareAt(position) }
        .filter { square -> square.isEmpty() }
        .let { squares -> tuplesFrom(squares, n) }
        .map { squares -> squares to squares.map { square -> square.marks }.reduce(Set<Char>::plus) }
        .filter { (_, marks) -> marks.size == n }
        .flatMap { (squares, marks) ->
            group.asSequence()
                .filterNot { position -> position in squares.map { square -> square.position } }
                .map { position -> position to marks.filter { c -> c in squareAt(position).marks } }
                .filterNot { (_, marks) -> marks.isEmpty() }
                .map { (position, marks) -> Action.EraseMarks(position, marks) }
        }
}

private fun <E> tuplesFrom(l: Sequence<E>, n: Int): Sequence<List<E>> = when (n) {
    2 -> pairsFrom(l)
    3 -> triplesFrom(l)
    else -> error("n is too large: $n")
}

private fun <E> pairsFrom(l: Sequence<E>): Sequence<List<E>> =
    l.flatMapIndexed { i, v1 ->
        l.drop(i + 1).map { v2 -> listOf(v1, v2) }
    }

private fun <E> triplesFrom(l: Sequence<E>): Sequence<List<E>> =
    l.flatMapIndexed { i1, v1 ->
        l.drop(i1 + 1).flatMapIndexed { i2, v2 ->
            l.drop(i1 + i2 + 2).map { v3 -> listOf(v1, v2, v3) }
        }
    }
