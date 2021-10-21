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
        d[i] = c
        rowIndicesOf(i).forEach { possibles[it].remove(c) }
        colIndicesOf(i).forEach { possibles[it].remove(c) }
        blockIndicesOf(i).forEach { possibles[it].remove(c) }
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
            progress = false
            d.forEachIndexed { i, c ->
                if (c == '.' && possibles[i].size == 1) {
                    set(i, possibles[i].single())
                    progress = true
                }
            }
        }
    }
}
