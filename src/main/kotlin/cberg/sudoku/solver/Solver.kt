package cberg.sudoku.solver

import cberg.sudoku.game.*
import cberg.sudoku.solver.Technique.*

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
            UniqueSolution(solvedGame.toString())
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

    private fun Game.missingCandidate() = groups.any { group ->
        Game.symbols.any { symbol ->
            group.map { position -> squareAt(position) }
                .none { square -> symbol == square.value || symbol in square.marks }
        }
    }

}

sealed class Solution
class UniqueSolution(val solution: String) : Solution()
object InvalidPuzzle : Solution()
object TooHard : Solution()

private val techniques = listOf(
    NakedSingle,
    HiddenSingle,
    NakedTuple(2),
    HiddenTuple(2),
    NakedTuple(3),
    HiddenTuple(3),
    XWing(2),
    XWing(3)
)

fun solve(game: Game) = generateSequence(game, ::applyFirstHint).last()

private fun applyFirstHint(game: Game) = game.hints().firstOrNull()?.applyTo(game)

private fun Game.hints(): Sequence<Hint> = techniques.fold(emptySequence()) { hints, technique ->
    hints + technique.analyze(this)
}

fun Game.filteredHints(): Sequence<Hint> {
    return HintSequence(hints(), this)
}
