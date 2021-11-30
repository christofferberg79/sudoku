package cberg.sudoku.game

import cberg.sudoku.game.Game.Companion.symbols

data class Square(
    val position: Position,
    val value: Char?,
    val given: Boolean,
    val marks: Set<Char> = emptySet()
)

fun Square.isEmpty() = value == null
fun Square.isNotEmpty() = value != null

data class Game(
    val squares: List<Square>
) {
    init {
        require(squares.size == 81)
    }

    override fun toString() = squares.joinToString(separator = "") { s -> "${s.value ?: '.'}" }

    companion object {
        val symbols = ('1'..'9').toSet()
    }
}

fun Game.squareAt(position: Position) = squares[position.index]
private val Position.index get() = row * 9 + col
private fun Position(index: Int) = Position(row = index / 9, col = index % 9)

fun Game(input: String): Game {
    val squares = List(81) { index ->
        val char = input.getOrNull(index)
        val given = char in symbols
        val value = if (given) char else null
        Square(Position(index), value, given)
    }

    return Game(squares)
}

fun Game.setValue(position: Position, char: Char): Game {
    val square = squareAt(position)
    if (square.given || square.value == char) {
        return this
    }

    return updateSquare(position) {
        copy(value = char, marks = emptySet())
    }
}

fun Game.eraseValue(position: Position): Game {
    val square = squareAt(position)
    if (square.given || square.isEmpty()) {
        return this
    }

    return updateSquare(position) {
        copy(value = null)
    }
}

fun Game.toggleMark(position: Position, char: Char): Game {
    if (squareAt(position).isNotEmpty()) {
        return this
    }

    return updateSquare(position) {
        copy(marks = if (char in marks) marks - char else marks + char)
    }
}

fun Game.setValueAndEraseMarks(position: Position, char: Char): Game {
    val newGame = setValue(position, char)
    if (newGame == this) {
        return this
    }

    val affected = affectedBy(position)
    return newGame.updateSquares(affected) { copy(marks = marks - char) }
}

fun Game.eraseMark(position: Position, char: Char): Game {
    val square = squareAt(position)
    if (char !in square.marks) {
        return this
    }
    return updateSquare(position) {
        copy(marks = marks - char)
    }
}

private fun Game.updateSquare(position: Position, transform: Square.() -> Square) =
    copy(squares = squares.map { square ->
        if (square.position == position) transform(square) else square
    })

private fun Game.updateSquares(positions: Set<Position>, transform: Square.() -> Square) =
    copy(squares = squares.map { square ->
        if (square.position in positions) transform(square) else square
    })

fun Game.writePencilMarks() = copy(squares = squares.map { square ->
    if (square.isEmpty()) {
        val excludedValues = affectedBy(square.position)
            .mapNotNull { position -> squareAt(position).value }.toSet()
        square.copy(marks = symbols - excludedValues)
    } else {
        square
    }
})

sealed class GameStatus {
    object NotDone : GameStatus()
    object CorrectSolution : GameStatus()
    object IncorrectSolution : GameStatus()
}

fun Game.getStatus(): GameStatus = when {
    squares.any(Square::isEmpty) -> GameStatus.NotDone
    isCorrect() -> GameStatus.CorrectSolution
    else -> GameStatus.IncorrectSolution
}

private fun Game.isCorrect(): Boolean {
    return groups.all { group ->
        group.map { position -> squareAt(position).value }.containsAll(symbols)
    }
}


private val positions = List(81) { index -> Position(index) }
val rows = List(9) { row -> positions.filter { position -> position.row == row } }
val cols = List(9) { col -> positions.filter { position -> position.col == col } }
val blocks = List(9) { block -> positions.filter { position -> position.block == block } }
val groups = (rows + cols + blocks).asSequence()

@OptIn(ExperimentalStdlibApi::class)
private fun affectedBy(position: Position) = buildSet {
    addAll(rows[position.row])
    addAll(cols[position.col])
    addAll(blocks[position.block])
    remove(position)
}
