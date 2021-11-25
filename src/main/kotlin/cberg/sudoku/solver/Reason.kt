package cberg.sudoku.solver

import cberg.sudoku.game.Position

class Reason(private val positions: List<Position>, private val marks: Set<Char>) {
    constructor(position: Position, mark: Char) : this(listOf(position), setOf(mark))

    override fun toString() = "$marks at $positions"
}