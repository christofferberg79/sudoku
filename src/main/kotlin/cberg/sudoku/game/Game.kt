@file:OptIn(ExperimentalStdlibApi::class)

package cberg.sudoku.game

data class Square(
    val position: Position,
    val value: Char?,
    val given: Boolean,
    val marks: Set<Char> = emptySet()
)

data class Game(
    val squares: List<Square>
) {
    operator fun get(position: Position) = squares[position.index]
}

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
}

private val Position.index get() = row * 9 + col
private fun Position(index: Int) = positions[index]

fun Game.setValue(position: Position, char: Char) = updateSquare(position) {
    copy(value = char, marks = emptySet())
}

fun Game.eraseValue(position: Position) = updateSquare(position) {
    copy(value = null)
}

fun Game.toggleMark(position: Position, char: Char) = updateSquare(position) {
    copy(marks = if (char in marks) marks - char else marks + char)
}

fun Game.eraseMarks(position: Position): Game {
    val value = get(position).value
    checkNotNull(value)

    val affected = affectedBy(position)
    return updateSquares(affected) { copy(marks = marks - value) }
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
    square.copy(
        marks = if (square.value != null) {
            emptySet()
        } else {
            val values = affectedBy(square.position).mapNotNull { p -> get(p).value }.toSet()
            ('1'..'9').filterNot { it in values }.toSet()
        }
    )
})

sealed class GameStatus {
    object NotDone : GameStatus()
    object CorrectSolution : GameStatus()
    object IncorrectSolution : GameStatus()
}

val Game.status: GameStatus
    get() = when {
        squares.any { it.value == null } -> GameStatus.NotDone
        isCorrect() -> GameStatus.CorrectSolution
        else -> GameStatus.IncorrectSolution
    }

private fun Game.isCorrect(): Boolean {
    return groups.all { group ->
        group.map { position -> get(position).value }.containsAll(('1'..'9').toList())
    }
}


private val positions = List(81) { i -> Position(i / 9, i % 9) }
private val rows = List(9) { row -> positions.filter { s -> s.row == row } }
private val cols = List(9) { col -> positions.filter { s -> s.col == col } }
private val blocks = List(9) { block -> positions.filter { s -> s.block == block } }
private val groups = rows.asSequence() + cols.asSequence() + blocks.asSequence()
private fun affectedBy(position: Position) = buildSet {
    addAll(rows[position.row])
    addAll(cols[position.col])
    addAll(blocks[position.block])
}
