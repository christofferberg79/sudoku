package cberg.sudoku.solver

import cberg.sudoku.game.Grid
import cberg.sudoku.game.Position
import cberg.sudoku.game.eraseCandidates
import cberg.sudoku.game.setDigitAndEraseCandidates

sealed interface Action {
    fun applyTo(grid: Grid): Grid
    val position: Position

    data class SetDigit(override val position: Position, val digit: Char) : Action {
        override fun applyTo(grid: Grid) = grid.setDigitAndEraseCandidates(position, digit)
        override fun toString() = "$position => set digit $digit"
    }

    data class EraseCandidates(override val position: Position, val candidates: Set<Char>) : Action {
        constructor(position: Position, mark: Char) : this(position, setOf(mark))

        init {
            require(candidates.isNotEmpty())
        }

        override fun applyTo(grid: Grid) = candidates.fold(grid) { g, m -> g.eraseCandidates(position, m) }
        override fun toString() = "$position => erase candidates ${candidates.joinToString()}"
    }
}
