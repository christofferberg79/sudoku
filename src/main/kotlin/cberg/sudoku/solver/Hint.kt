package cberg.sudoku.solver

import cberg.sudoku.game.Game

data class Hint(val actions: List<Action>, val reason: Reason, val technique: Technique) {
    constructor(action: Action, reason: Reason, technique: Technique) : this(listOf(action), reason, technique)

    init {
        require(actions.isNotEmpty())
    }

    fun applyTo(game: Game) = actions.fold(game) { nextGame, action ->
        action.applyTo(nextGame)
    }
}

class HintSequence(private val source: Sequence<Hint>) : Sequence<Hint> {
    override fun iterator(): Iterator<Hint> {
        return HintIterator(source.iterator())
    }
}

class HintIterator(private val source: Iterator<Hint>) : AbstractIterator<Hint>() {
    private val observed = mutableListOf<Hint>()

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

    private fun add(hint: Hint): Boolean {
        val relevant = hint.actions.any { action ->
            observed.none { otherHint->
                otherHint.actions.any { otherAction -> otherAction covers action }
            }
        }
        if (relevant) {
            observed += hint
        }
        return relevant
    }
}
