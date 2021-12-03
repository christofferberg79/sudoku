package cberg.sudoku.solver

import cberg.sudoku.game.Position

data class Reason(val positions: List<Position>, val candidates: Set<Int>) {
    constructor(position: Position, candidate: Int) : this(listOf(position), setOf(candidate))
    constructor(positions: List<Position>, candidate: Int) : this(positions, setOf(candidate))

    override fun toString() = "$candidates at $positions"
}