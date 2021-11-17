package cberg.sudoku.gui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class Model(input: String) {

    var state by mutableStateOf(initialState(input))
        private set

    data class State(
        val squares: List<Square>,
        val pencil: Boolean = false
    )

    data class Square(
        val value: Char?,
        val given: Boolean,
        val marks: Set<Char> = emptySet()
    )

    private fun initialState(input: String) = State(List(81) { i ->
        require(input.length == 81)
        require(input.all { c -> c == '.' || c in '1'..'9' })
        val given = input[i] in '1'..'9'
        val value = if (given) input[i] else null
        Square(value, given)
    })

    private inline fun setState(update: State.() -> State) {
        state = state.update()
    }

    fun writeChar(i: Int, char: Char) = setState {
        when {
            squares[i].given -> this
            squares[i].value == char -> this
            !pencil -> updateSquare(i) { copy(value = char) }
            squares[i].value == null -> updateSquare(i) { copy(marks = toggleMark(char, marks)) }
            else -> this
        }
    }

    fun deleteChar(i: Int) = setState {
        when {
            squares[i].given -> this
            squares[i].value == null -> this
            !pencil -> updateSquare(i) { copy(value = null) }
            else -> this
        }
    }

    fun togglePencil() = setState {
        copy(pencil = !pencil)
    }

    private fun toggleMark(char: Char, marks: Set<Char>) =
        if (char in marks) marks - char else marks + char

    private fun State.updateSquare(i: Int, transform: Square.() -> Square) =
        copy(squares = squares.updateByIndex(i, transform))

    private fun <E> List<E>.updateByIndex(index: Int, transform: (E) -> E): List<E> {
        return mapIndexed { otherIndex, item -> if (otherIndex == index) transform(item) else item }
    }
}
