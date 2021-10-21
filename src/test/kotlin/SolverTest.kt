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
}