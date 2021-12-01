package cberg.sudoku.solver

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
    fun xWing() {
        val input = ".........76...3..2..264...94.39...7......49.3..5....2..1.56....37..9..41.......6."
        val solution = "841729635769153482532648719423985176687214953195376824214567398376892541958431267"
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
    fun unsolvableSquare() {
        val input = "..9.287..8.6..4..5..3.....46.........2.71345.........23.....5..9..4..8.7..125.3.."
        testInvalidPuzzle(input)
    }

    @Test
    fun unsolvableBox() {
        val input = ".9.3....1....8..46......8..4.5.6..3...32756...6..1.9.4..1......58..2....2....7.6."
        testInvalidPuzzle(input)
    }

    @Test
    fun unsolvableColumn() {
        val input = "....41....6.....2...2......32.6.........5..417.......2......23..48......5.1..2..."
        testInvalidPuzzle(input)
    }

    @Test
    fun unsolvableRow() {
        val input = "9..1....4.14.3.8....3....9....7.8..18....3..........3..21....7...9.4.5..5...16..3"
        testInvalidPuzzle(input)
    }

    @Test
    fun hardestPuzzle() {
        val input = "........8..3...4...9..2..6.....79.......612...6.5.2.7...8...5...1.....2.4.5.....3"
        testTooHardPuzzle(input)
    }

    @Test
    fun sudokuDotComExpert1() {
        val input = "..4..1..8........73..4.....1..2.6..9....387...2.....1..8.3...2..6..1.....7.....65"
        val solution = "694751238812693547357482691135276489946138752728945316581367924269514873473829165"
        testPuzzleWithUniqueSolution(input, solution)
    }

    @Test
    fun sudokuDotComExpert2() {
        val input = ".......9..7...5...9.1..7..8.8...4.1..2....7.4....3....3.48.1.2....3...5...9......"
        val solution = "245683197678915342931427568583764219126598734497132685354871926762349851819256473"
        testPuzzleWithUniqueSolution(input, solution)
    }

    @Test
    fun sudokuDotComExpert3() {
        val input = ".7...2.8....76......259....6........9.3.2..6..5....7.....3..1...4.8.1..9......37."
        testTooHardPuzzle(input)

        // TODO when solver is improved:
//        val solution = "479132586538764912162598437687915243913427865254683791726349158345871629891256374"
//        testPuzzleWithUniqueSolution(input, solution)
    }

    @Test
    fun sudokuDotComExpert4() {
        val input = "6725..................49....1.....7...3.854..9.....56....2.....2..7.3.9..5....83."
        val solution = "672531984549827613831649257415962378763185429928374561396218745284753196157496832"
        testPuzzleWithUniqueSolution(input, solution)
    }

    @Test
    fun sudokuDotComExpert5() {
        val input = ".....85....1......4736.....6..2....4.....7.3.....95.6.........61.7.8......87.4..."
        val solution = "926178543851943627473652198685231974219467835734895261342519786197386452568724319"
        testPuzzleWithUniqueSolution(input, solution)
    }

    @Test
    fun sudokuDotComExpert6() {
        val input = "...........5...4.93..8...6..3...6....49...2.7..7..5.1.4.6.5.....1..9.7....3..2..."
        testTooHardPuzzle(input)

        // TODO when solver is improved:
//        val solution = "962514378185367429374829561531276984649138257827945613496751832218693745753482196"
//        testPuzzleWithUniqueSolution(input, solution)
    }

    @Test
    fun sudokuDotComExpert7() {
        val input = "5..9...7..6....9.48.......5751.....86..2..5...8......19.....3......4.......5.1..."
        testTooHardPuzzle(input)
    }

    @Test
    fun sudokuDotComExpert8() {
        val input = ".13..7..6.......24.5.8...7....9.87.........5....67..........9.27.6.3.8....1.2...."
        val solution = "913247586687195324254863179162958743479312658538674291345781962726439815891526437"
        testPuzzleWithUniqueSolution(input, solution)
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

private fun testTooHardPuzzle(input: String) {
    val result = solve(input)
    assertIs<TooHard>(result)
}

private fun solve(input: String): Solution = Solver().solve(input)
