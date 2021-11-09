package cberg.sudoku

sealed class Solution
class UniqueSolution(val solution: String) : Solution()
object InvalidPuzzle : Solution()
object TooHard : Solution()

class Square(val row: Int, val col: Int) {
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
    private val squares = Array(81) { i -> Square(i / 9, i % 9) }

    private val rows = List<Iterable<Square>>(9) { r -> ((r * 9)..(r * 9 + 8)).map { squares[it] } }
    private val cols = List<Iterable<Square>>(9) { c -> (c..(c + 72) step 9).map { squares[it] } }
    private val blocks = listOf(0, 3, 6, 27, 30, 33, 54, 57, 60).map { i ->
        listOf(0, 1, 2, 9, 10, 11, 18, 19, 20).map { o -> i + o }
            .map { squares[it] }
    }

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
        return rowOf(s).asSequence() + colOf(s).asSequence() + blockOf(s).asSequence()
    }

    private fun rowOf(s: Square): Iterable<Square> {
        return rows[s.row]
    }

    private fun colOf(s: Square): Iterable<Square> {
        return cols[s.col]
    }

    private fun blockOf(s: Square): Iterable<Square> {
        val blockRow = s.row / 3
        val blockCol = s.col / 3
        return blocks[blockRow * 3 + blockCol]
    }
}
