package cberg.sudoku.game

data class Position(val row: Int, val col: Int) {
    init {
        require(row in 0..8)
        require(col in 0..8)
    }

    val box = row / 3 * 3 + col / 3

    override fun toString() = "r${row + 1}c${col + 1}"
}

fun Position.up() = when (row) {
    0 -> Position(8, col)
    else -> Position(row - 1, col)
}

fun Position.down() = when (row) {
    8 -> Position(0, col)
    else -> Position(row + 1, col)
}

fun Position.left() = when (col) {
    0 -> Position(row, 8)
    else -> Position(row, col - 1)
}

fun Position.right() = when (col) {
    8 -> Position(row, 0)
    else -> Position(row, col + 1)
}
