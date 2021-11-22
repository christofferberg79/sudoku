package cberg.sudoku.solver

import cberg.sudoku.game.*

class Solver {
    fun solve(input: String): Solution {
        val game = Game(input).writePencilMarks()

        if (game.isInvalidPuzzle()) {
            return InvalidPuzzle
        }

        val solvedGame = solve(game)

        return if (solvedGame.squares.any(Square::isNotSet)) {
            TooHard
        } else {
            UniqueSolution(solvedGame.getOutput())
        }
    }

    private fun Game.isInvalidPuzzle() = insufficientGivens()
            || duplicateGivens()
            || noCandidate()
            || missingCandidate()

    private fun Game.insufficientGivens() = squares.count(Square::isSet) < 17

    private fun Game.duplicateGivens() = groups.any { group ->
        group.map { position -> get(position) }
            .filter(Square::isSet)
            .groupingBy(Square::value)
            .eachCount()
            .values.any { it > 1 }
    }

    private fun Game.noCandidate() = squares.any { square ->
        square.isNotSet() && square.marks.isEmpty()
    }

    private fun Game.missingCandidate() = symbols.any { symbol ->
        groups.any { group ->
            group.map { position -> get(position) }
                .none { square -> symbol == square.value || symbol in square.marks }
        }
    }

    private fun Game.getOutput() = squares.joinToString(separator = "") { s -> "${s.value ?: '.'}" }

    private fun solve(game: Game) = generateSequence(game, this::next).last()

    private fun next(game: Game) = game.actions().firstOrNull()?.let { action -> action(game) }

    private fun Game.actions() = nakedSingles() + hiddenSingles()

    private fun Game.nakedSingles() = squares.asSequence()
        .filter { square -> square.marks.size == 1 }
        .map { square -> Action.SetValue(square.position, square.marks.single()) }

    private fun Game.hiddenSingles() = groups.asSequence().flatMap { group ->
        symbols.asSequence().mapNotNull { symbol ->
            group.singleOrNull { position -> symbol in get(position).marks }
                ?.let { position -> Action.SetValue(position, symbol) }
        }
    }
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
    }
}

private fun Square.isSet() = value != null
private fun Square.isNotSet() = value == null
