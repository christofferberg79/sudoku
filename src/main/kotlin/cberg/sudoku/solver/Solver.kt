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

    private fun set(position: Position, symbol: Char) {
        game = game.setValueAndEraseMarks(position, symbol)
    }

    private fun solve() {
        var progress = true
        while (progress) {
            progress = nakedSingles()
                    || hiddenSingles()
        }
    }

    private fun nakedSingles(): Boolean {
        game.squares.firstOrNull { square -> square.marks.size == 1 }
            ?.let { square ->
                set(square.position, square.marks.single())
                return true
            }
        return false
    }

    private fun hiddenSingles(): Boolean {
        for (group in groups) {
            for (symbol in symbols) {
                group.singleOrNull { position -> symbol in game[position].marks }
                    ?.let { position ->
                        set(position, symbol)
                        return true
                    }
            }
        }
        return false
    }
}

private fun Square.isSet() = value != null
private fun Square.isNotSet() = value == null
