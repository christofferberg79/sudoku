package cberg.sudoku.solver

sealed interface Technique {
    object NakedSingle : Technique {
        override fun toString() = "Naked Single"
    }

    object HiddenSingle : Technique {
        override fun toString() = "Hidden Single"
    }

    data class NakedTuple(private val n: Int) : Technique {
        override fun toString() = "Naked ${tupleString(n)}"
    }

    data class HiddenTuple(private val n: Int) : Technique {
        override fun toString() = "Hidden ${tupleString(n)}"
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