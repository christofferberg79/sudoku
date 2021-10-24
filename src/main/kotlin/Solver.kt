package cberg.sudoku

class Solver {
    private val d = CharArray(81) { '.' }
    private val possibles = Array(81) { mutableSetOf('1', '2', '3', '4', '5', '6', '7', '8', '9') }

    fun solve(input: String): String {
        setInput(input)
        solve()
        return getOutput()
    }

    private fun setInput(input: String) = input.forEachIndexed { i, c -> set(i, c) }

    private fun getOutput() = d.joinToString(separator = "")

    private fun set(i: Int, c: Char) {
        if (c in '1'..'9') {
            d[i] = c
            rowIndicesOf(i).forEach { possibles[it].remove(c) }
            colIndicesOf(i).forEach { possibles[it].remove(c) }
            blockIndicesOf(i).forEach { possibles[it].remove(c) }
            possibles[i].clear()
            possibles[i].add(c)
        }
    }

    private fun rowIndicesOf(i: Int): Iterable<Int> {
        val first = i - i % 9
        val last = first + 8
        return first..last
    }

    private fun colIndicesOf(i: Int): Iterable<Int> {
        val first = i % 9
        val last = first + 72
        return first..last step 9
    }

    private fun blockIndicesOf(i: Int): Iterable<Int> {
        val first = i - i % 27 + i % 9 - i % 3
        return listOf(0, 1, 2, 9, 10, 11, 18, 19, 20).map { first + it }
    }

    private fun solve() {
        var progress = true
        while (progress) {
            progress = nakedSingles()
                    || hiddenSingles()
        }
    }

    private fun nakedSingles(): Boolean {
        d.forEachIndexed { i, c ->
            if (c == '.' && possibles[i].size == 1) {
                set(i, possibles[i].single())
                return true
            }
        }
        return false
    }

    private fun hiddenSingles(): Boolean {
        for (group in groups()) {
            for (c in '1'..'9') {
                group.singleOrNull { i -> d[i] == '.' && c in possibles[i] }
                    ?.let { i ->
                        set(i, c)
                        return true
                    }
            }
        }
        return false
    }

    private fun groups(): List<Iterable<Int>> {
        val rows = (0..72 step 9).map { rowIndicesOf(it) }
        val cols = (0..8).map { colIndicesOf(it) }
        val blocks = listOf(0, 3, 6, 27, 30, 33, 54, 57, 60).map { blockIndicesOf(it) }
        return rows + cols + blocks
    }
}
