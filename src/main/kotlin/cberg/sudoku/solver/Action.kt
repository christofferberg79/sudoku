package cberg.sudoku.solver

import cberg.sudoku.game.Game
import cberg.sudoku.game.Position
import cberg.sudoku.game.eraseMark
import cberg.sudoku.game.setValueAndEraseMarks

sealed interface Action {
    fun applyTo(game: Game): Game
    val position: Position

    data class SetValue(override val position: Position, val value: Char) : Action {
        override fun applyTo(game: Game) = game.setValueAndEraseMarks(position, value)
        override fun toString() = "$position => set value $value"
    }

    data class EraseMarks(override val position: Position, val marks: Set<Char>) : Action {
        init {
            require(marks.isNotEmpty())
        }

        override fun applyTo(game: Game) = marks.fold(game) { g, m -> g.eraseMark(position, m) }
        override fun toString() = "$position => erase marks ${marks.joinToString()}"
    }
}

infix fun Action.covers(action: Action): Boolean {
    if (this.position != action.position) {
        return false
    }

    return when (this) {
        is Action.SetValue -> {
            when (action) {
                is Action.SetValue -> value == action.value
                is Action.EraseMarks -> true
            }
        }
        is Action.EraseMarks -> {
            when (action) {
                is Action.EraseMarks -> marks.all { c -> c in action.marks }
                is Action.SetValue -> false
            }
        }
    }
}
