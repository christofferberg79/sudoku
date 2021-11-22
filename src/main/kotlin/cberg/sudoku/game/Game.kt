package cberg.sudoku.game

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
)

fun Game.squareAt(position: Position) = squares[position.index]

fun Game(input: String): Game {
    require(input.length == 81)
    require(input.all { c -> c == '.' || c in '1'..'9' })

    val squares = input.mapIndexed { index, char ->
        val given = char in '1'..'9'
        val value = if (given) char else null
        Square(Position(index), value, given)
    }
    return Game(squares)
}

data class Position(val row: Int, val col: Int) {
    init {
        require(row in 0..8)
        require(col in 0..8)
    }

    val block = row / 3 * 3 + col / 3

    override fun toString() = "r${row + 1}c${col + 1}"
}

private val Position.index get() = row * 9 + col
private fun Position(index: Int) = positions[index]

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
        square.copy(marks = ('1'..'9').filterNot { it in excludedValues }.toSet())
    } else {
        square
    }
})

sealed class GameStatus {
    object NotDone : GameStatus()
    object CorrectSolution : GameStatus()
    object IncorrectSolution : GameStatus()
}

val Game.status: GameStatus
    get() = when {
        squares.any(Square::isEmpty) -> GameStatus.NotDone
        isCorrect() -> GameStatus.CorrectSolution
        else -> GameStatus.IncorrectSolution
    }

private fun Game.isCorrect(): Boolean {
    return groups.all { group ->
        group.map { position -> squareAt(position).value }.containsAll(('1'..'9').toList())
    }
}


private val positions = List(81) { i -> Position(i / 9, i % 9) }
private val rows = List(9) { row -> positions.filter { s -> s.row == row } }
private val cols = List(9) { col -> positions.filter { s -> s.col == col } }
private val blocks = List(9) { block -> positions.filter { s -> s.block == block } }
val groups = rows.asSequence() + cols.asSequence() + blocks.asSequence()

@OptIn(ExperimentalStdlibApi::class)
private fun affectedBy(position: Position) = buildSet {
    addAll(rows[position.row])
    addAll(cols[position.col])
    addAll(blocks[position.block])
}
