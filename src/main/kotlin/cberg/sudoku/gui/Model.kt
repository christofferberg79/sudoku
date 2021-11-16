package cberg.sudoku.gui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class Model(input: String) {

    var state by mutableStateOf(initialState(input))
        private set

    data class State(
        val squares: List<Square>
    )

    data class Square(
        val value: Char?,
        val given: Boolean
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
        updateSquare(i) { it.copy(value = char) }
    }

    fun deleteChar(i: Int) = setState {
        updateSquare(i) { it.copy(value = null) }
    }

    private fun State.updateSquare(i: Int, transform: (Square) -> Square) =
        copy(squares = squares.update(i, transform))

    private fun <E> List<E>.update(i: Int, transform: (E) -> E): List<E> {
        return mapIndexed { index, square -> if (index == i) transform(square) else square }
    }
}
