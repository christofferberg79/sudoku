package cberg.sudoku.solver

import cberg.sudoku.game.Grid
import cberg.sudoku.game.Position
import cberg.sudoku.game.setAllCandidates
import cberg.sudoku.solver.Technique.HiddenSingle
import cberg.sudoku.solver.Technique.NakedSingle
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class TechniqueTest {
    private val r3c7 = Position(2, 6)
    private val r7c3 = Position(6, 2)
    private val r6c5 = Position(5, 4)
    private val r8c3 = Position(7, 2)

    @Test
    fun nakedSingle() {
        val grid = Grid("1.2...3.......4.5.5.6.2...7...1...4...5...8...3...6...7...9.1.8.8.3.......4...9.6")

        val hints = NakedSingle.analyze(grid.setAllCandidates()).toList()
        assertEquals(2, hints.size)
        val r3c7 = Position(2, 6)
        val r7c3 = Position(6, 2)
        assertContains(hints, Hint(Action.SetDigit(r3c7, '4'), Reason(r3c7, '4'), NakedSingle))
        assertContains(hints, Hint(Action.SetDigit(r7c3, '3'), Reason(r7c3, '3'), NakedSingle))
    }

    @Test
    fun hiddenSingle() {
        val grid = Grid("1..2..3...2..1..4...3..5..67..6..5...5..8..7...8..4..18..7..4...3..6..2...9..2..7")

        val hints = HiddenSingle.analyze(grid.setAllCandidates()).toSet()
        assertEquals(4, hints.size)
        assertContains(hints, Hint(Action.SetDigit(r3c7, '2'), Reason(r3c7, '2'), HiddenSingle))
        assertContains(hints, Hint(Action.SetDigit(r7c3, '2'), Reason(r7c3, '2'), HiddenSingle))
        assertContains(hints, Hint(Action.SetDigit(r6c5, '7'), Reason(r6c5, '7'), HiddenSingle))
        assertContains(hints, Hint(Action.SetDigit(r8c3, '7'), Reason(r8c3, '7'), HiddenSingle))
    }
}