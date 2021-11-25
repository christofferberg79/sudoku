package cberg.sudoku.solver

import cberg.sudoku.game.Position

sealed interface Technique {
    class NakedSingle(private val position: Position, private val value: Char) : Technique {
        override fun toString() = "Naked Single $value at $position"
    }

    class HiddenSingle(private val position: Position, private val value: Char) : Technique {
        override fun toString() = "Hidden Single $value at $position"
    }

    class NakedTuple(private val n: Int, private val positions: List<Position>, private val values: Set<Char>) :
        Technique {
        override fun toString(): String {
            return "Naked ${tupleString(n)} $values at $positions"
        }
    }

    class HiddenTuple(private val n: Int, private val positions: List<Position>, private val values: Set<Char>) :
        Technique {
        override fun toString(): String {
            return "Naked ${tupleString(n)} $values at $positions"
        }
    }
}

private fun tupleString(n: Int): String {
    require(n >= 2)
    return when (n) {
        2 -> "Pair"
        3 -> "Triple"
        else -> "$n-tuple"
    }
}