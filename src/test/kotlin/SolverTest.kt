package cberg.sudoku

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SolverTest {
    @Test
    fun completedPuzzle() {
        val input = "974236158638591742125487936316754289742918563589362417867125394253649871491873625"
        testPuzzleWithUniqueSolution(input, input)
    }

    @Test
    fun lastEmptySquare() {
        val input = "2564891733746159829817234565932748617128.6549468591327635147298127958634849362715"
        val solution = "256489173374615982981723456593274861712836549468591327635147298127958634849362715"
        testPuzzleWithUniqueSolution(input, solution)
    }

    @Test
    fun nakedSingles() {
        val input = "3.542.81.4879.15.6.29.5637485.793.416132.8957.74.6528.2413.9.655.867.192.965124.8"
        val solution = "365427819487931526129856374852793641613248957974165283241389765538674192796512438"
        testPuzzleWithUniqueSolution(input, solution)
    }

    @Test
    fun hiddenSingles() {
        val input = "..2.3...8.....8....31.2.....6..5.27..1.....5.2.4.6..31....8.6.5.......13..531.4.."
        val solution = "672435198549178362831629547368951274917243856254867931193784625486592713725316489"
        testPuzzleWithUniqueSolution(input, solution)
    }

    @Test
    fun empty() {
        val input = "................................................................................."
        testInvalidPuzzle(input)
    }

    @Test
    fun singleGiven() {
        val input = "........................................1........................................"
        testInvalidPuzzle(input)
    }

    @Test
    fun insufficientGivens() {
        val input = "...........5....9...4....1.2....3.5....7.....438...2......9.....1.4...6.........."
        testInvalidPuzzle(input)
    }

    @Test
    fun duplicateGivenInBox() {
        val input = "..9.7...5..21..9..1...28....7...5..1..851.....5....3.......3..68........21.....87"
        testInvalidPuzzle(input)
    }

    @Test
    fun duplicateGivenInColumn() {
        val input = "6.159.....9..1............4.7.314..6.24.....5..3....1...6.....3...9.2.4......16.."
        testInvalidPuzzle(input)
    }

    @Test
    fun duplicateGivenInRow() {
        val input = ".4.1..35.............2.5......4.89..26.....12.5.3....7..4...16.6....7....1..8..2."
        testInvalidPuzzle(input)
    }

    @Test
    fun hardestPuzzle() {
        val input = "........8..3...4...9..2..6.....79.......612...6.5.2.7...8...5...1.....2.4.5.....3"
        val result = solve(input)
        assertIs<TooHard>(result)
    }
}

private fun testPuzzleWithUniqueSolution(input: String, solution: String) {
    val result = solve(input)
    assertIs<UniqueSolution>(result)
    assertEquals(solution, result.solution)
}

private fun testInvalidPuzzle(input: String) {
    val result = solve(input)
    assertIs<InvalidPuzzle>(result)
}

private fun solve(input: String): Solution = Solver().solve(input)