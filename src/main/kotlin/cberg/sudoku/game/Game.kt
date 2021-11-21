package cberg.sudoku.game

data class Square(
    val position: Position,
    val value: Char?,
    val given: Boolean,
    val marks: Set<Char> = emptySet()
)

data class Game(
    val squares: List<Square>
)

class Position(val row: Int, val col: Int) {
    init {
        require(row in 0..8)
        require(col in 0..8)
    }

    val block = row / 3 * 3 + col / 3
    val index = row * 9 + col
}

fun initialGame(input: String): Game {
    require(input.length == 81)
    require(input.all { c -> c == '.' || c in '1'..'9' })

    val squares = input.mapIndexed { index, char ->
        val given = char in '1'..'9'
        val value = if (given) char else null
        Square(positions[index], value, given)
    }
    return Game(squares)
}

fun Game.setValue(index: Int, char: Char) = updateSquare(index) {
    copy(value = char, marks = emptySet())
}

fun Game.eraseValue(index: Int) = updateSquare(index) {
    copy(value = null)
}

fun Game.toggleMark(index: Int, char: Char) = updateSquare(index) {
    copy(marks = if (char in marks) marks - char else marks + char)
}

fun Game.eraseMarks(index: Int, char: Char): Game {
    val affected = affectedBy(positions[index]).map { it.index }.toSet()
    return updateSquares(affected) { copy(marks = marks - char) }
}

private fun Game.updateSquare(index: Int, transform: Square.() -> Square) =
    copy(squares = squares.mapIndexed { otherIndex, item ->
        if (otherIndex == index) transform(item) else item
    })

private fun Game.updateSquares(indices: Iterable<Int>, transform: Square.() -> Square) =
    copy(squares = squares.mapIndexed { otherIndex, item ->
        if (otherIndex in indices) transform(item) else item
    })

fun Game.writePencilMarks() = copy(squares = squares.map { square ->
    square.copy(
        marks = if (square.value != null) {
            emptySet()
        } else {
            val values = affectedBy(square.position).mapNotNull { p -> squares[p.index].value }.toSet()
            ('1'..'9').filter { it !in values }.toSet()
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
        group.map { position -> squares[position.index].value }.containsAll(('1'..'9').toList())
    }
}

private val positions = List(81) { i -> Position(i / 9, i % 9) }
private val rows = List(9) { row -> positions.filter { s -> s.row == row } }
private val cols = List(9) { col -> positions.filter { s -> s.col == col } }
private val blocks = List(9) { block -> positions.filter { s -> s.block == block } }
private val groups = rows.asSequence() + cols.asSequence() + blocks.asSequence()
private fun affectedBy(position: Position): Sequence<Position> {
    return rows[position.row].asSequence() + cols[position.col].asSequence() + blocks[position.block].asSequence()
}
