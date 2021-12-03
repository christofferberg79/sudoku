package cberg.sudoku.game

import cberg.sudoku.game.Grid.Companion.digits

data class Cell(
    val position: Position,
    val digit: Char?,
    val candidates: Set<Char> = emptySet()
)

fun Cell.isEmpty() = digit == null
fun Cell.isNotEmpty() = digit != null

data class Grid(
    val cells: List<Cell>
) {
    init {
        require(cells.size == 81)
    }

    override fun toString() = cells.joinToString(separator = "") { s -> "${s.digit ?: '.'}" }

    companion object {
        val digits = ('1'..'9').toSet()
    }

    fun cellAt(position: Position) = cells[position.index]

    fun Position.hasCandidate(digit: Char) = cells[index].run { isEmpty() && digit in candidates }
}

fun Grid(input: String): Grid {
    val cells = List(81) { index ->
        val char = input.getOrNull(index)
        val digit = if (char in digits) char else null
        Cell(Position(index), digit)
    }

    return Grid(cells)
}

fun Grid.setDigit(position: Position, char: Char): Grid {
    val cell = cellAt(position)
    if (cell.digit == char) {
        return this
    }

    return updateCell(position) {
        copy(digit = char, candidates = emptySet())
    }
}

fun Grid.erase(position: Position): Grid {
    val cell = cellAt(position)
    if (cell.isEmpty() && cell.candidates.isEmpty()) {
        return this
    }

    return updateCell(position) {
        copy(digit = null, candidates = emptySet())
    }
}

fun Grid.toggleCandidate(position: Position, char: Char): Grid {
    if (cellAt(position).isNotEmpty()) {
        return this
    }

    return updateCell(position) {
        copy(candidates = if (char in candidates) candidates - char else candidates + char)
    }
}

fun Grid.setDigitAndEraseCandidates(position: Position, char: Char): Grid {
    val newGame = setDigit(position, char)
    if (newGame == this) {
        return this
    }

    return newGame.updateCells(position.peers()) { copy(candidates = candidates - char) }
}

fun Grid.eraseCandidates(position: Position, char: Char): Grid {
    val cell = cellAt(position)
    if (char !in cell.candidates) {
        return this
    }
    return updateCell(position) {
        copy(candidates = candidates - char)
    }
}

private fun Grid.updateCell(position: Position, transform: Cell.() -> Cell) =
    copy(cells = cells.map { cell ->
        if (cell.position == position) transform(cell) else cell
    })

private fun Grid.updateCells(positions: Set<Position>, transform: Cell.() -> Cell) =
    copy(cells = cells.map { cell ->
        if (cell.position in positions) transform(cell) else cell
    })

fun Grid.setAllCandidates() = copy(cells = cells.map { cell ->
    if (cell.isEmpty()) {
        val digitsOfPeers = cell.position.peers()
            .mapNotNull { position -> cellAt(position).digit }.toSet()
        cell.copy(candidates = digits - digitsOfPeers)
    } else {
        cell
    }
})

sealed class GameStatus {
    object NotDone : GameStatus()
    object CorrectSolution : GameStatus()
    object IncorrectSolution : GameStatus()
}

fun Grid.getStatus(): GameStatus = when {
    cells.any(Cell::isEmpty) -> GameStatus.NotDone
    isCorrect() -> GameStatus.CorrectSolution
    else -> GameStatus.IncorrectSolution
}

private fun Grid.isCorrect(): Boolean {
    return houses.all { group ->
        group.map { position -> cellAt(position).digit }.containsAll(digits)
    }
}

private const val n = 9
private val Position.index get() = row * n + col
private fun Position(index: Int) = Position(row = index / n, col = index % n)

private val positions = List(n * n) { index -> Position(index) }
val rows = List(n) { row -> positions.filter { it.row == row } }
val cols = List(n) { col -> positions.filter { it.col == col } }
val boxes = List(n) { block -> positions.filter { it.block == block } }
val lines = (rows + cols).asSequence()
val houses = (rows + cols + boxes).asSequence()

@OptIn(ExperimentalStdlibApi::class)
private fun Position.peers() = buildSet {
    this.addAll(rows[row])
    this.addAll(cols[col])
    this.addAll(boxes[block])
    this.remove(this@peers)
}
