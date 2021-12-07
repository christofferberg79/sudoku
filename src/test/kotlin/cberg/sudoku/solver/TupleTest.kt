package cberg.sudoku.solver

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TupleTest {
    @Test
    fun tuplesOfEmptyList() {
        val list = emptyList<Int>()
        assertTrue(list.tuplesOfSize(1).isEmpty())
        assertTrue(list.tuplesOfSize(3).isEmpty())
    }

    @Test
    fun tuplesOfSingletonList() {
        val list = listOf(1)
        assertEquals(listOf(list), list.tuplesOfSize(1))
        assertTrue(list.tuplesOfSize(2).isEmpty())
    }

    @Test
    fun tuplesOfListOfFour() {
        val list = listOf(1, 2, 3, 4)
        assertEquals(listOf(listOf(1), listOf(2), listOf(3), listOf(4)), list.tuplesOfSize(1))
        assertEquals(
            listOf(listOf(1, 2), listOf(1, 3), listOf(1, 4), listOf(2, 3), listOf(2, 4), listOf(3, 4)),
            list.tuplesOfSize(2)
        )
        assertEquals(
            listOf(listOf(1, 2, 3), listOf(1, 2, 4), listOf(1, 3, 4), listOf(2, 3, 4)),
            list.tuplesOfSize(3)
        )
        assertEquals(listOf(list), list.tuplesOfSize(4))
        assertTrue(list.tuplesOfSize(5).isEmpty())
    }

    @Test
    @OptIn(ExperimentalStdlibApi::class)
    fun tuplesOfLongerList() {
        val list = listOf(1, 2, 3, 4, 5, 6, 7)
        val expected = buildList {
            for (a in 1..4) for (b in a + 1..5) for (c in b + 1..6) for (d in c + 1..7) {
                add(listOf(a, b, c, d))
            }
        }
        assertEquals(
            expected,
            list.tuplesOfSize(4)
        )
    }
}