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

    class NakedPair(
        private val positions: Pair<Position, Position>,
        private val values: Pair<Char, Char>,
        val group: List<Position>
    ) : Action {
        override fun invoke(game: Game): Game {
            var newGame = game
            group.forEach { p -> newGame = newGame.eraseMark(p, values.first).eraseMark(p, values.second) }
            return newGame
        }

        override fun toString() = "Naked pair in $positions: $values ($group)"
    }
}

fun solve(game: Game) = generateSequence(game, ::next).last()

private fun next(game: Game) = game.actions().firstOrNull()?.let { action -> action(game) }

fun Game.actions() = nakedSingles() + hiddenSingles() + nakedPairs()

private fun Game.nakedSingles() = squares.asSequence()
    .filter { square -> square.marks.size == 1 }
    .map { square -> Action.SetValue(square.position, square.marks.single()) }

private fun Game.hiddenSingles() = groups.asSequence().flatMap { group ->
    symbols.asSequence().mapNotNull { symbol ->
        group.singleOrNull { position -> symbol in squareAt(position).marks }
            ?.let { position -> Action.SetValue(position, symbol) }
    }
}

private fun Game.nakedPairs() = groups.asSequence().flatMap { group ->
    val squares = group.asSequence().map { squareAt(it) }.filter { it.isEmpty() }
    pairsFrom(squares)
        .map { (s1, s2) -> Pair(s1.position, s2.position) to s1.marks + s2.marks }
        .filter { (_, m) -> m.size == 2 }
        .mapNotNull { (p, m) ->
            val filteredGroup = group.filter { it != p.first && it != p.second }
                .filter { pos-> m.any { c -> c in squareAt(pos).marks } }
            if (filteredGroup.isEmpty()) {
                null
            } else {
                Action.NakedPair(
                    positions = p,
                    values = m.toList().let { (m1, m2) -> Pair(m1, m2) },
                    group = filteredGroup
                )
            }
        }
}

private fun <E> pairsFrom(l: Sequence<E>): Sequence<Pair<E, E>> =
    l.flatMapIndexed { i, v1 ->
        l.drop(i + 1).map { v2 -> Pair(v1, v2) }
    }
