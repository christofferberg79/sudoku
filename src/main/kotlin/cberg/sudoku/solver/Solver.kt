package cberg.sudoku.solver

import cberg.sudoku.game.*

sealed class Solution
class UniqueSolution(val solution: String) : Solution()
object InvalidPuzzle : Solution()
object TooHard : Solution()

val symbols = '1'..'9'

class Solver {
    private lateinit var game: Game

    fun solve(input: String): Solution {
        setInput(input)

        if (!isValidPuzzle()) {
            return InvalidPuzzle
        }

        solve()

        return if (game.squares.any(Square::isNotSet)) {
            TooHard
        } else {
            UniqueSolution(getOutput())
        }
    }

    private fun isValidPuzzle(): Boolean {
        if (insufficientGivens()) {
            return false
        }
        if (duplicateGivens()) {
            return false
        }
        if (noCandidate()) {
            return false
        }
        if (missingCandidate()) {
            return false
        }
        return true
    }

    private fun insufficientGivens() = game.squares.count(Square::isSet) < 17

    private fun duplicateGivens() = groups.any { group ->
        group.map { game[it] }
            .filter(Square::isSet)
            .groupingBy(Square::value)
            .eachCount()
            .values.any { it > 1 }
    }

    private fun noCandidate() = game.squares.any {
        it.isNotSet() && it.marks.isEmpty()
    }

    private fun missingCandidate() = symbols.any { symbol ->
        groups.any { group ->
            group.map { game[it] }.none { square -> symbol == square.value || symbol in square.marks }
        }
    }

    private fun setInput(input: String) {
        game = Game(input).writePencilMarks()
    }

    private fun getOutput() = game.squares.joinToString(separator = "") { s -> "${s.value ?: '.'}" }

    private fun solve() {
        generateSequence { actions().firstOrNull() }
            .forEach { action -> game = action(game) }
    }

    private fun actions() = nakedSingles() + hiddenSingles()

    private fun nakedSingles() = game.squares.asSequence()
        .filter { square -> square.marks.size == 1 }
        .map { square -> Action.SetValue(square.position, square.marks.single()) }

    private fun hiddenSingles() = groups.asSequence().flatMap { group ->
        symbols.asSequence().mapNotNull { symbol ->
            group.singleOrNull { position -> symbol in game[position].marks }
                ?.let { position -> Action.SetValue(position, symbol) }
        }
    }
}

sealed interface Action {
    operator fun invoke(game: Game): Game

    class SetValue(private val position: Position, private val value: Char) : Action {
        override fun invoke(game: Game) = game.setValueAndEraseMarks(position, value)
    }
}

private fun Square.isSet() = value != null
private fun Square.isNotSet() = value == null
