package cberg.sudoku.solver

import kotlin.test.Test
import kotlin.test.assertEquals

class SequenceTupleTest {
    @Test
    fun test() {
        val tupleSize = 3
        val source = 1..5

        val actual = source.asSequence()
            .tuplesOfSize(tupleSize)
            .toSet()

        val expected = setOf(
            listOf(1, 2, 3),
            listOf(1, 2, 4),
            listOf(1, 2, 5),
            listOf(1, 3, 4),
            listOf(1, 3, 5),
            listOf(1, 4, 5),
            listOf(2, 3, 4),
            listOf(2, 3, 5),
            listOf(2, 4, 5),
            listOf(3, 4, 5),
        )

        assertEquals(expected, actual)
    }
}

