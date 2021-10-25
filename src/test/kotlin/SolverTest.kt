package cberg.sudoku

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.fail

class SolverTest {
    @Test
    fun completedPuzzle() {
        val input = "974236158638591742125487936316754289742918563589362417867125394253649871491873625"
        val solver = Solver()
        val result = solver.solve(input)
        assertUniqueSolution(input, result)
    }

    @Test
    fun lastEmptySquare() {
        val input = "2564891733746159829817234565932748617128.6549468591327635147298127958634849362715"
        val solver = Solver()
        val result = solver.solve(input)
        assertUniqueSolution(
            "256489173374615982981723456593274861712836549468591327635147298127958634849362715",
            result
        )
    }

    @Test
    fun nakedSingles() {
        val input = "3.542.81.4879.15.6.29.5637485.793.416132.8957.74.6528.2413.9.655.867.192.965124.8"
        val solver = Solver()
        val result = solver.solve(input)
        assertUniqueSolution(
            "365427819487931526129856374852793641613248957974165283241389765538674192796512438",
            result
        )
    }

    @Test
    fun hiddenSingles() {
        val input = "..2.3...8.....8....31.2.....6..5.27..1.....5.2.4.6..31....8.6.5.......13..531.4.."
        val solver = Solver()
        val result = solver.solve(input)
        assertUniqueSolution(
            "672435198549178362831629547368951274917243856254867931193784625486592713725316489",
            result
        )
    }

    @Test
    fun empty() {
        val input = "................................................................................."
        val solver = Solver()
        val result = solver.solve(input)
        assertIs<InvalidPuzzle>(result)
    }

    @Test
    fun singleGiven() {
        val input = "........................................1........................................"
        val solver = Solver()
        val result = solver.solve(input)
        assertIs<InvalidPuzzle>(result)
    }

    @Test
    fun insufficientGivens() {
        val input = "...........5....9...4....1.2....3.5....7.....438...2......9.....1.4...6.........."
        val solver = Solver()
        val result = solver.solve(input)
        assertIs<InvalidPuzzle>(result)
    }

    @Test
    fun duplicateGivenInBox() {
        val input = "..9.7...5..21..9..1...28....7...5..1..851.....5....3.......3..68........21.....87"
        val solver = Solver()
        val result = solver.solve(input)
        assertIs<InvalidPuzzle>(result)
    }

    @Test
    fun duplicateGivenInColumn() {
        val input = "6.159.....9..1............4.7.314..6.24.....5..3....1...6.....3...9.2.4......16.."
        val solver = Solver()
        val result = solver.solve(input)
        assertIs<InvalidPuzzle>(result)
    }

    @Test
    fun duplicateGivenInRow() {
        val input = ".4.1..35.............2.5......4.89..26.....12.5.3....7..4...16.6....7....1..8..2."
        val solver = Solver()
        val result = solver.solve(input)
        assertIs<InvalidPuzzle>(result)
    }
}

private fun assertUniqueSolution(expected: String, result: Solution) {
    when (result) {
        is UniqueSolution -> {
            assertEquals(
                expected,
                result.solution
            )
        }
        else -> fail()
    }
}
