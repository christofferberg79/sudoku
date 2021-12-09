package cberg.sudoku.game

import cberg.sudoku.game.Grid.Companion.digits

data class Cell(
    val position: Position,
    val digit: Int?,
    val candidates: Set<Int> = emptySet()
) {
    init {
        require(digit == null || candidates.isEmpty()) { "A cell cannot have both a digit and candidates" }
    }

    fun isEmpty() = digit == null
    fun isNotEmpty() = digit != null
}

data class Grid(
    val cells: List<Cell>
) {
    init {
        require(cells.size == N * N)
    }

    override fun toString() = cells.joinToString(separator = "") { s -> "${s.digit ?: '.'}" }

    companion object {
        val digits = (1..N).toSet()
    }

    fun houses() = houses.asSequence()
    fun rows() = rows.asSequence()
    fun cols() = cols.asSequence()
    fun boxes() = boxes.asSequence()
    fun lines() = lines.asSequence()
    fun commonPeers(positions: List<Position>) = positions.map { position -> position.peers() }
        .reduce { peers1, peers2 -> peers1 intersect peers2 }

    val Position.cell: Cell get() = cells[index]
    val Position.digit: Int? get() = cell.digit
    val Position.candidates: Set<Int> get() = cell.candidates
    fun Position.isEmpty() = cell.isEmpty()
    fun Position.isNotEmpty() = cell.isNotEmpty()

    @OptIn(ExperimentalStdlibApi::class)
    val Collection<Position>.candidates: Set<Int>
        get() = buildSet {
            this@candidates.forEach { position -> addAll(position.candidates) }
        }

    fun Position.containsCandidatesIn(tuple: Set<Int>) = candidates.any { candidate -> candidate in tuple }
}

fun Grid(input: String): Grid {
    val cells = List(81) { index ->
        val digit = input.getOrNull(index)?.digitToIntOrNull()?.let {
            if (it in digits) it else null
        }
        Cell(Position(index), digit)
    }

    return Grid(cells)
}

private val Position.index get() = row * N + col
private fun Position(index: Int) = Position(row = index / N, col = index % N)

fun Grid.setDigit(position: Position, digit: Int): Grid {
    if (position.digit == digit) {
        return this
    }

    return updateCell(position) {
        copy(digit = digit, candidates = emptySet())
    }
}

fun Grid.erase(position: Position): Grid {
    if (position.isEmpty() && position.candidates.isEmpty()) {
        return this
    }

    return updateCell(position) {
        copy(digit = null, candidates = emptySet())
    }
}

fun Grid.toggleCandidate(position: Position, digit: Int): Grid {
    if (position.isNotEmpty()) {
        return this
    }

    return updateCell(position) {
        copy(candidates = if (digit in candidates) candidates - digit else candidates + digit)
    }
}

fun Grid.setDigitAndEraseCandidates(position: Position, digit: Int): Grid {
    val newGrid = setDigit(position, digit)
    if (newGrid == this) {
        return this
    }

    return newGrid.updateCells(position.peers()) { copy(candidates = candidates - digit) }
}

fun Grid.eraseCandidate(position: Position, digit: Int): Grid {
    if (digit !in position.candidates) {
        return this
    }
    return updateCell(position) {
        copy(candidates = candidates - digit)
    }
}

fun Grid.eraseCandidates(position: Position, digits: Set<Int>): Grid {
    if (digits.none { it in position.candidates }) {
        return this
    }
    return updateCell(position) {
        copy(candidates = candidates - digits)
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
            .mapNotNull { position -> position.digit }.toSet()
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
        group.map { position -> position.digit }.containsAll(digits)
    }
}
