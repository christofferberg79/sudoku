package cberg.sudoku

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SolverTest {
    @Test
    fun completedPuzzle() {
        val input = "974236158638591742125487936316754289742918563589362417867125394253649871491873625"
        val solver = Solver()
        val result = solver.solve(input)
        assertEquals(input, result)
    }

    @Test
    fun lastEmptySquare() {
        val input = "2564891733746159829817234565932748617128.6549468591327635147298127958634849362715"
        val solver = Solver()
        val result = solver.solve(input)
        assertEquals("256489173374615982981723456593274861712836549468591327635147298127958634849362715", result)
    }
}