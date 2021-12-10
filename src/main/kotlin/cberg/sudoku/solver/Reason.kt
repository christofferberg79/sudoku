package cberg.sudoku.solver

import cberg.sudoku.game.Position

data class Reason(val positions: List<Position>, val candidates: Collection<Int>) {
    constructor(positions: List<Position>, candidate: Int) : this(positions, listOf(candidate))
    constructor(position: Position, candidate: Int) : this(listOf(position), listOf(candidate))

    override fun toString() = "$candidates at $positions"
}