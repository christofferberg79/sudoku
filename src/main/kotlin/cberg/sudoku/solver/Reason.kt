package cberg.sudoku.solver

import cberg.sudoku.game.Position

data class Reason(val positions: List<Position>, val candidates: Set<Char>) {
    constructor(position: Position, candidate: Char) : this(listOf(position), setOf(candidate))
    constructor(positions: List<Position>, candidate: Char) : this(positions, setOf(candidate))

    override fun toString() = "$candidates at $positions"
}