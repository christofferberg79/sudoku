package cberg.sudoku.solver

sealed class Solution
class UniqueSolution(val solution: String) : Solution()
object InvalidPuzzle : Solution()
object TooHard : Solution()

val symbols = '1'..'9'

class Square(val row: Int, val col: Int) {
    val block: Int = row / 3 * 3 + col / 3
    var value: Char? = null
    val candidates = symbols.toMutableSet()

    fun isSet() = value != null
    fun isNotSet() = value == null
}

class Solver {
    private val squares = List(81) { i -> Square(i / 9, i % 9) }
    private val rows = List(9) { row -> squares.filter { s -> s.row == row } }
    private val cols = List(9) { col -> squares.filter { s -> s.col == col } }
    private val blocks = List(9) { block -> squares.filter { s -> s.block == block } }

    fun solve(input: String): Solution {
        setInput(input)

        if (!isValidPuzzle()) {
            return InvalidPuzzle
        }

        solve()

        return if (squares.any(Square::isNotSet)) {
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

    private fun insufficientGivens() = squares.count(Square::isSet) < 17

    private fun duplicateGivens() = groups().any { group ->
        group.filter(Square::isSet)
            .groupingBy(Square::value)
            .eachCount()
            .values.any { it > 1 }
    }

    private fun noCandidate() = squares.any {
        it.isNotSet() && it.candidates.isEmpty()
    }

    private fun missingCandidate() = symbols.any { symbol ->
        groups().any { group ->
            group.none { square -> symbol == square.value || symbol in square.candidates }
        }
    }

    private fun setInput(input: String) = input.mapIndexed { i, c -> squares[i] to c }
        .filter { (_, c) -> c in symbols }
        .forEach { (s, c) -> set(s, c) }

    private fun getOutput() = squares.joinToString(separator = "") { s -> "${s.value ?: '.'}" }

    private fun set(square: Square, symbol: Char) {
        square.value = symbol
        square.candidates.clear()
        affectedBy(square).forEach { it.candidates.remove(symbol) }
    }

    private fun solve() {
        var progress = true
        while (progress) {
            progress = nakedSingles()
                    || hiddenSingles()
        }
    }

    private fun nakedSingles(): Boolean {
        squares.firstOrNull { square -> square.candidates.size == 1 }
            ?.let { square ->
                set(square, square.candidates.single())
                return true
            }
        return false
    }

    private fun hiddenSingles(): Boolean {
        for (group in groups()) {
            for (symbol in symbols) {
                group.singleOrNull { square -> symbol in square.candidates }
                    ?.let { square ->
                        set(square, symbol)
                        return true
                    }
            }
        }
        return false
    }

    private fun groups(): Sequence<Iterable<Square>> {
        return rows.asSequence() + cols.asSequence() + blocks.asSequence()
    }

    private fun affectedBy(s: Square): Sequence<Square> {
        return rows[s.row].asSequence() + cols[s.col].asSequence() + blocks[s.block].asSequence()
    }
}
