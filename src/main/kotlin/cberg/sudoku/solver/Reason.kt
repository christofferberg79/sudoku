package cberg.sudoku.solver

import cberg.sudoku.game.Position

data class Reason(val positions: List<Position>, val marks: Set<Char>) {
    constructor(position: Position, mark: Char) : this(listOf(position), setOf(mark))

    override fun toString() = "$marks at $positions"
}