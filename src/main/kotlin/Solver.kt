package cberg.sudoku

val rows = List<Iterable<Int>>(9) { r -> (r * 9)..(r * 9 + 8) }
val cols = List<Iterable<Int>>(9) { c -> c..(c + 72) step 9 }
val blocks = listOf(0, 3, 6, 27, 30, 33, 54, 57, 60)
    .map { i -> listOf(0, 1, 2, 9, 10, 11, 18, 19, 20).map { o -> i + o } }

sealed class Solution
class UniqueSolution(val solution: String) : Solution()
object InvalidPuzzle : Solution()
object TooHard : Solution()

class Square {
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
    private val squares = Array(81) { Square() }

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
        group.filter { squares[it].isSet() }
            .groupingBy { squares[it].value }
            .eachCount()
            .values.any { it > 1 }
    }

    private fun insufficientGivens() = squares.count(Square::isSet) < 17

    private fun noCandidate() = squares.any {
        it.isNotSet() && it.candidates.isEmpty()
    }

    private fun missingCandidate() = ('1'..'9').any { c ->
        groups().any { group ->
            group.map { i -> squares[i] }
                .none { s -> c == s.value || c in s.candidates }
        }
    }

    private fun setInput(input: String) = input.forEachIndexed { i, c -> set(i, c) }

    private fun getOutput() = squares.joinToString(separator = "")

    private fun set(i: Int, c: Char) {
        if (c in '1'..'9') {
            squares[i].set(c)
            affectedBy(i).forEach { squares[it].candidates.remove(c) }
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
        squares.forEachIndexed { i, s ->
            if (s.candidates.size == 1) {
                set(i, s.candidates.single())
                return true
            }
        }
        return false
    }

    private fun hiddenSingles(): Boolean {
        for (group in groups()) {
            for (c in '1'..'9') {
                val i = group.singleOrNull { i -> c in squares[i].candidates }
                if (i != null) {
                    set(i, c)
                    return true
                }
            }
        }
        return false
    }

    private fun groups(): Sequence<Iterable<Int>> {
        return rows.asSequence() + cols.asSequence() + blocks.asSequence()
    }

    private fun affectedBy(i: Int): Sequence<Int> {
        return rowOf(i).asSequence() + colOf(i).asSequence() + blockOf(i).asSequence()
    }

    private fun rowOf(i: Int): Iterable<Int> {
        return rows[i / 9]
    }

    private fun colOf(i: Int): Iterable<Int> {
        return cols[i % 9]
    }

    private fun blockOf(i: Int): Iterable<Int> {
        val blockRow = i / 27
        val blockCol = i / 3 % 3
        return blocks[blockRow * 3 + blockCol]
    }
}
