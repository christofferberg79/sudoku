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

fun <E> Sequence<E>.tuplesOfSize(n: Int): Sequence<List<E>> {
    require(n >= 1)

    return if (n == 1) {
        map { listOf(it) }
    } else {
        flatMapIndexed { i, v -> drop(i + 1).tuplesOfSize(n - 1).map { it + v } }
    }
}
