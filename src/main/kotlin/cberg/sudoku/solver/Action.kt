package cberg.sudoku.solver

import cberg.sudoku.game.Game
import cberg.sudoku.game.Position
import cberg.sudoku.game.eraseMark
import cberg.sudoku.game.setValueAndEraseMarks

sealed class Action(val technique: String) {
    abstract fun applyTo(game: Game): Game

    class SetValue(val position: Position, val value: Char, t: String) : Action(t) {
        override fun applyTo(game: Game) = game.setValueAndEraseMarks(position, value)
        override fun toString() = "[$technique] $position => $value"
    }

    class EraseMarks(val position: Position, val marks: Set<Char>, t: String) : Action(t) {
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
    private val observed = mutableListOf<Action>()

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
        val relevant = observed.none { previousAction ->
            when (previousAction) {
                is Action.SetValue -> {
                    when (action) {
                        is Action.SetValue -> previousAction.position == action.position
                        is Action.EraseMarks -> previousAction.position == action.position
                    }

                }
                is Action.EraseMarks -> {
                    check(action is Action.EraseMarks)
                    previousAction.position == action.position && previousAction.marks.all { it in action.marks }
                }
            }
        }
        if (relevant) {
            observed += action
        }
        return relevant
    }
}
