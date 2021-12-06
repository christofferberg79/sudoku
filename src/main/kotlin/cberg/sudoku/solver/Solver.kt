package cberg.sudoku.solver

import cberg.sudoku.game.*
import cberg.sudoku.solver.Technique.*

class Solver {
    fun solve(input: String): Solution {
        val game = Grid(input).setAllCandidates()

        if (game.isInvalidPuzzle()) {
            return InvalidPuzzle
        }

        val solvedGame = solve(game)

        return if (solvedGame.cells.any(Cell::isEmpty)) {
            TooHard
        } else {
            UniqueSolution(solvedGame.toString())
        }
    }

    private fun Grid.isInvalidPuzzle() = insufficientGivens()
            || duplicateGivens()
            || noCandidate()
            || missingCandidate()

    private fun Grid.insufficientGivens() = cells.count(Cell::isNotEmpty) < 17

    private fun Grid.duplicateGivens() = houses.any { group ->
        group.map { position -> cellAt(position) }
            .filter(Cell::isNotEmpty)
            .groupingBy(Cell::digit)
            .eachCount()
            .values.any { it > 1 }
    }

    private fun Grid.noCandidate() = cells.any { square ->
        square.isEmpty() && square.candidates.isEmpty()
    }

    private fun Grid.missingCandidate() = houses.any { group ->
        Grid.digits.any { digit ->
            group.map { position -> cellAt(position) }
                .none { square -> digit == square.digit || digit in square.candidates }
        }
    }

}

sealed class Solution
class UniqueSolution(val solution: String) : Solution()
object InvalidPuzzle : Solution()
object TooHard : Solution()

private val techniques = sequenceOf(
    NakedSingle,
    HiddenSingle,
    LockedCandidates,
    NakedTuple(2),
    HiddenTuple(2),
    NakedTuple(3),
    HiddenTuple(3),
    XWing,
    SashimiXWing
)

fun solve(grid: Grid) = generateSequence(grid, ::applyFirstHint).last()

private fun applyFirstHint(grid: Grid) = hints(grid).firstOrNull()?.applyTo(grid)

private fun hints(grid: Grid): Sequence<Hint> = techniques.flatMap { technique -> technique.analyze(grid) }

fun filteredHints(grid: Grid): Sequence<Hint> {
    return HintSequence(hints(grid), grid)
}
