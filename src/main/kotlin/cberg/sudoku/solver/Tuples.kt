package cberg.sudoku.solver

fun tupleString(n: Int): String {
    require(n >= 1)
    return when (n) {
        1 -> "Single"
        2 -> "Pair"
        3 -> "Triple"
        4 -> "Quadruple"
        5 -> "Quintuple"
        else -> "$n-tuple"
    }
}

fun <E> List<E>.tuplesOfSize(n: Int): List<List<E>> {
    require(n >= 1) { "n must be >= 1 but is $n" }

    return combinations(size, n).map { it.map { i -> this[i] } }
}

private fun combinations(n: Int, k: Int): List<List<Int>> {
    var combinations = (0..n - k).map { listOf(it) }
    for (i in 1 until k) {
        combinations = combinations.flatMap { partial ->
            (partial.last() + 1..n - k + i).map { next -> partial + next }
        }
    }
    return combinations
}

fun <E> Sequence<E>.tuplesOfSize(n: Int) = object : Sequence<List<E>> {
    override fun iterator() = TupleIterator(n, this@tuplesOfSize.iterator())
}

class TupleIterator<E>(private val n: Int, private val source: Iterator<E>) : AbstractIterator<List<E>>() {
    private val computedTuples = mutableListOf<List<E>>()
    private val elements = mutableListOf<E>()

    override fun computeNext() {
        if (computedTuples.isEmpty()) {
            if (elements.isEmpty()) {
                computeFirstTuple()
            } else if (source.hasNext()) {
                computeNewTuples()
            }
        }

        if (computedTuples.isEmpty()) {
            done()
        } else {
            setNext(computedTuples.removeFirst())
        }
    }

    private fun computeFirstTuple() {
        val tuple = generateSequence { if (source.hasNext()) source.next() else null }
            .take(n).toList()
        if (tuple.size == n) {
            elements += tuple
            computedTuples += tuple
        }
    }

    private fun computeNewTuples() {
        val e = source.next()
        for (small in elements.tuplesOfSize(n - 1)) {
            computedTuples += small + e
        }
        elements += e
    }
}
