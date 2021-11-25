package cberg.sudoku.solver

import cberg.sudoku.game.Position

class Hint(val action: Action, val reason: Reason, val technique: Technique)

class HintSequence(private val source: Sequence<Hint>) : Sequence<Hint> {
    override fun iterator(): Iterator<Hint> {
        return HintIterator(source.iterator())
    }
}

class HintIterator(private val source: Iterator<Hint>) : AbstractIterator<Hint>() {
    private val observed = mutableMapOf<Position, MutableList<Hint>>()

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
        val observedAtPosition = observed.getOrPut(hint.action.position, ::mutableListOf)
        val relevant = observedAtPosition.none { previousHint -> previousHint covers hint }
        if (relevant) {
            observedAtPosition.add(hint)
        }
        return relevant
    }

}

private infix fun Hint.covers(hint: Hint) = action covers hint.action
