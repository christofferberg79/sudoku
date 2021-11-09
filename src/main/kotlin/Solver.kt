package cberg.sudoku

sealed class Solution
class UniqueSolution(val solution: String) : Solution()
object InvalidPuzzle : Solution()
object TooHard : Solution()

class Square(val row: Int, val col: Int) {
    val block: Int = row / 3 * 3 + col / 3

    var value: Char? = null
        private set
    val candidates = mutableSetOf('1', '2', '3', '4', '5', '6', '7', '8', '9')

    fun isSet() = value != null
    fun isNotSet() = value == null
    fun set(c: Char) {
        value = c
        candidates.clear()
    }

    override fun toString(): String {
        return value?.toString() ?: "."
    }
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

    private fun duplicateGivens() = groups().any { group ->
        group.filter { it.isSet() }
            .groupingBy { it.value }
            .eachCount()
            .values.any { it > 1 }
    }

    private fun insufficientGivens() = squares.count(Square::isSet) < 17

    private fun noCandidate() = squares.any {
        it.isNotSet() && it.candidates.isEmpty()
    }

    private fun missingCandidate() = ('1'..'9').any { c ->
        groups().any { group ->
            group.none { s -> c == s.value || c in s.candidates }
        }
    }

    private fun setInput(input: String) = input.forEachIndexed { i, c -> set(squares[i], c) }

    private fun getOutput() = squares.joinToString(separator = "")

    private fun set(s: Square, c: Char) {
        if (c in '1'..'9') {
            s.set(c)
            affectedBy(s).forEach { it.candidates.remove(c) }
        }
    }

    private fun solve() {
        var progress = true
        while (progress) {
            progress = nakedSingles()
                    || hiddenSingles()
        }
    }

    private fun nakedSingles(): Boolean {
        squares.forEach { s ->
            if (s.candidates.size == 1) {
                set(s, s.candidates.single())
                return true
            }
        }
        return false
    }

    private fun hiddenSingles(): Boolean {
        for (group in groups()) {
            for (c in '1'..'9') {
                val s = group.singleOrNull { s -> c in s.candidates }
                if (s != null) {
                    set(s, c)
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
