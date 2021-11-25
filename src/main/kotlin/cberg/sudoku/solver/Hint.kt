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

class HintSequence(private val source: Sequence<Hint>, private val game: Game) : Sequence<Hint> {
    override fun iterator(): Iterator<Hint> {
        return HintIterator(source.iterator(), game)
    }
}

class HintIterator(private val source: Iterator<Hint>, private var game: Game) : AbstractIterator<Hint>() {
    override fun computeNext() {
        while (source.hasNext()) {
            val next = source.next()

            val newGame = next.applyTo(game)
            if (newGame != game) {
                game = newGame
                setNext(next)
                return
            }
        }

        done()
    }

}
