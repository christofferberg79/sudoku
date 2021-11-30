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
        constructor(position: Position, mark: Char) : this(position, setOf(mark))

        init {
            require(marks.isNotEmpty())
        }

        override fun applyTo(game: Game) = marks.fold(game) { g, m -> g.eraseMark(position, m) }
        override fun toString() = "$position => erase marks ${marks.joinToString()}"
    }
}
