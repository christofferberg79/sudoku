package cberg.sudoku.solver

import cberg.sudoku.game.Game
import cberg.sudoku.game.Position
import cberg.sudoku.game.eraseMark
import cberg.sudoku.game.setValueAndEraseMarks

sealed class Action(val position: Position, val technique: String) {
    abstract fun applyTo(game: Game): Game

    class SetValue(position: Position, val value: Char, t: String) : Action(position, t) {
        override fun applyTo(game: Game) = game.setValueAndEraseMarks(position, value)
        override fun toString() = "[$technique] $position => $value"
    }

    class EraseMarks(position: Position, val marks: Set<Char>, t: String) : Action(position, t) {
        override fun applyTo(game: Game) = marks.fold(game) { g, m -> g.eraseMark(position, m) }
        override fun toString() = "[$technique] $position => erase marks: ${marks.joinToString()}"
    }
}

class ActionSequence(private val source: Sequence<Action>) : Sequence<Action> {
    override fun iterator(): Iterator<Action> {
        return ActionIterator(source.iterator())
    }
}

class ActionIterator(private val source: Iterator<Action>) : AbstractIterator<Action>() {
    private val observed = mutableMapOf<Position, MutableList<Action>>()

    override fun computeNext() {
        while (source.hasNext()) {
            val next = source.next()

            if (add(next)) {
                setNext(next)
                return
            }
        }

        done()
    }

    private fun add(action: Action): Boolean {
        val observedAtPosition = observed.getOrPut(action.position, ::mutableListOf)
        val relevant = observedAtPosition.none { previousAction -> previousAction covers action }
        if (relevant) {
            observedAtPosition.add(action)
        }
        return relevant
    }

}

private infix fun Action.covers(action: Action): Boolean {
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
