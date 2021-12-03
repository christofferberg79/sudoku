package cberg.sudoku.solver

import cberg.sudoku.game.*
import cberg.sudoku.solver.Technique.HiddenSingle
import cberg.sudoku.solver.Technique.NakedSingle
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class TechniqueTest {
    private val r3c7 = Position(2, 6)
    private val r5c4 = Position(4, 3)
    private val r6c5 = Position(5, 4)
    private val r7c3 = Position(6, 2)
    private val r8c3 = Position(7, 2)

    @Test
    fun nakedSingle() {
        var grid = Grid("").setAllCandidates()

        val digit = 8
        val position = r7c3
        grid = grid.eraseCandidates(position, Grid.digits - digit)

        val hints = NakedSingle.analyze(grid).toList()
        assertEquals(1, hints.size)
        assertContains(hints, Hint(Action.SetDigit(position, digit), Reason(position, digit), NakedSingle))
    }

    @Test
    fun nakedSinglesInRealGame() {
        val grid = Grid("1.2...3.......4.5.5.6.2...7...1...4...5...8...3...6...7...9.1.8.8.3.......4...9.6")

        val hints = NakedSingle.analyze(grid.setAllCandidates()).toList()
        assertEquals(2, hints.size)
        assertContains(hints, Hint(Action.SetDigit(r3c7, 4), Reason(r3c7, 4), NakedSingle))
        assertContains(hints, Hint(Action.SetDigit(r7c3, 3), Reason(r7c3, 3), NakedSingle))
    }

    @Test
    fun hiddenSingleInColumn() {
        var grid = Grid("").setAllCandidates()

        val digit = 6
        val position = r5c4
        for (otherPosition in cols[position.col] - position) {
            grid = grid.eraseCandidate(otherPosition, digit)
        }

        val hints = HiddenSingle.analyze(grid).toList()
        assertEquals(1, hints.size)
        assertContains(hints, Hint(Action.SetDigit(position, digit), Reason(position, digit), HiddenSingle))
    }

    @Test
    fun hiddenSingleInRow() {
        var grid = Grid("").setAllCandidates()

        val digit = 4
        val position = r3c7
        for (otherPosition in rows[position.row] - position) {
            grid = grid.eraseCandidate(otherPosition, digit)
        }

        val hints = HiddenSingle.analyze(grid).toList()
        assertEquals(1, hints.size)
        assertContains(hints, Hint(Action.SetDigit(position, digit), Reason(position, digit), HiddenSingle))
    }

    @Test
    fun hiddenSingleInBox() {
        var grid = Grid("").setAllCandidates()

        val digit = 8
        val position = r8c3
        for (otherPosition in boxes[position.box] - position) {
            grid = grid.eraseCandidate(otherPosition, digit)
        }

        val hints = HiddenSingle.analyze(grid).toList()
        assertEquals(1, hints.size)
        assertContains(hints, Hint(Action.SetDigit(position, digit), Reason(position, digit), HiddenSingle))
    }

    @Test
    fun hiddenSinglesInRealGame() {
        val grid = Grid("1..2..3...2..1..4...3..5..67..6..5...5..8..7...8..4..18..7..4...3..6..2...9..2..7")

        val hints = HiddenSingle.analyze(grid.setAllCandidates()).toSet()
        assertEquals(4, hints.size)
        assertContains(hints, Hint(Action.SetDigit(r3c7, 2), Reason(r3c7, 2), HiddenSingle))
        assertContains(hints, Hint(Action.SetDigit(r7c3, 2), Reason(r7c3, 2), HiddenSingle))
        assertContains(hints, Hint(Action.SetDigit(r6c5, 7), Reason(r6c5, 7), HiddenSingle))
        assertContains(hints, Hint(Action.SetDigit(r8c3, 7), Reason(r8c3, 7 ), HiddenSingle))
    }
}