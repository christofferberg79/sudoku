package cberg.sudoku

val rows = List<Iterable<Int>>(9) { r -> (r * 9)..(r * 9 + 8) }
val cols = List<Iterable<Int>>(9) { c -> c..(c + 72) step 9 }
val blocks = listOf(0, 3, 6, 27, 30, 33, 54, 57, 60)
    .map { i -> listOf(0, 1, 2, 9, 10, 11, 18, 19, 20).map { o -> i + o } }

sealed class Solution
class UniqueSolution(val solution: String) : Solution()
object InvalidPuzzle : Solution()

class Solver {
    private val d = CharArray(81) { '.' }
    private val candidates = Array(81) { mutableSetOf('1', '2', '3', '4', '5', '6', '7', '8', '9') }

    fun solve(input: String): Solution {
        setInput(input)

        solve()

        return if ('.' in d) {
            InvalidPuzzle
        } else {
            UniqueSolution(getOutput())
        }
    }

    private fun setInput(input: String) = input.forEachIndexed { i, c -> set(i, c) }

    private fun getOutput() = d.joinToString(separator = "")

    private fun set(i: Int, c: Char) {
        if (c in '1'..'9') {
            d[i] = c
            affectedBy(i).forEach { candidates[it].remove(c) }
            candidates[i].clear()
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
        candidates.forEachIndexed { i, candidates ->
            if (candidates.size == 1) {
                set(i, candidates.single())
                return true
            }
        }
        return false
    }

    private fun hiddenSingles(): Boolean {
        for (group in groups()) {
            for (c in '1'..'9') {
                val i = group.singleOrNull { i -> c in candidates[i] }
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
