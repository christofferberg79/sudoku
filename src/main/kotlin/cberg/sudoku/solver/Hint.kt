package cberg.sudoku.solver

import cberg.sudoku.game.Grid

data class Hint(val actions: List<Action>, val reason: Reason, val technique: Technique) {
    constructor(action: Action, reason: Reason, technique: Technique) : this(listOf(action), reason, technique)

    init {
        require(actions.isNotEmpty())
    }

    fun applyTo(grid: Grid) = actions.fold(grid) { nextGame, action ->
        action.applyTo(nextGame)
    }
}

class HintSequence(private val source: Sequence<Hint>, private val grid: Grid) : Sequence<Hint> {
    override fun iterator(): Iterator<Hint> {
        return HintIterator(source.iterator(), grid)
    }
}

class HintIterator(private val source: Iterator<Hint>, private var grid: Grid) : AbstractIterator<Hint>() {
    override fun computeNext() {
        while (source.hasNext()) {
            val next = source.next()

            val newGame = next.applyTo(grid)
            if (newGame != grid) {
                grid = newGame
                setNext(next)
                return
            }
        }

        done()
    }

}
