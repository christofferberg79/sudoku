package cberg.sudoku.solver

import cberg.sudoku.game.Game
import cberg.sudoku.game.Position
import cberg.sudoku.game.eraseMark
import cberg.sudoku.game.setValueAndEraseMarks

sealed class Action(val position: Position) {
    abstract fun applyTo(game: Game): Game

    class SetValue(position: Position, val value: Char) : Action(position) {
        override fun applyTo(game: Game) = game.setValueAndEraseMarks(position, value)
        override fun toString() = "$position => set value $value"
    }

    class EraseMarks(position: Position, val marks: Set<Char>) : Action(position) {
        override fun applyTo(game: Game) = marks.fold(game) { g, m -> g.eraseMark(position, m) }
        override fun toString() = "$position => erase marks ${marks.joinToString()}"
    }
}

infix fun Action.covers(action: Action): Boolean {
    check(this.position == action.position)
    return when (this) {
        is Action.SetValue -> {
            when (action) {
                is Action.SetValue -> value == action.value
                is Action.EraseMarks -> true
            }
        }
        is Action.EraseMarks -> {
            check(action is Action.EraseMarks)
            marks.all { c -> c in action.marks }
        }
    }
}
