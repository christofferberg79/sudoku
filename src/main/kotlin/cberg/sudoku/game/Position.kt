package cberg.sudoku.game

private const val n = 3
const val N = n * n

data class Position(val row: Int, val col: Int) {
    init {
        require(row in 0 until N)
        require(col in 0 until N)
    }

    val box = row / n * n + col / n

    override fun toString() = "r${row + 1}c${col + 1}"
}

fun Position.up() = when (row) {
    0 -> Position(N - 1, col)
    else -> Position(row - 1, col)
}

fun Position.down() = when (row) {
    N - 1 -> Position(0, col)
    else -> Position(row + 1, col)
}

fun Position.left() = when (col) {
    0 -> Position(row, N - 1)
    else -> Position(row, col - 1)
}

fun Position.right() = when (col) {
    N - 1 -> Position(row, 0)
    else -> Position(row, col + 1)
}

val rows = List(N) { row -> List(N) { col -> Position(row, col) } }
val cols = rows.flatten().sortedBy { it.col }.chunked(N)
val boxes = rows.flatten().sortedBy { it.box }.chunked(N)
val lines = rows + cols
val houses = rows + cols + boxes

@OptIn(ExperimentalStdlibApi::class)
fun Position.peers() = buildSet {
    this.addAll(rows[row])
    this.addAll(cols[col])
    this.addAll(boxes[box])
    this.remove(this@peers)
}
