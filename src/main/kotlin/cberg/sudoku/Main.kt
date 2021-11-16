package cberg.sudoku

import cberg.sudoku.gui.gui
import cberg.sudoku.solver.InvalidPuzzle
import cberg.sudoku.solver.Solver
import cberg.sudoku.solver.TooHard
import cberg.sudoku.solver.UniqueSolution

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        return gui()
    }
    if (args.size == 2 && args[0] == "solve") {
        return solve(args[1])
    }
}

private fun solve(input: String) {
    val solver = Solver()
    when (val result = solver.solve(input)) {
        is InvalidPuzzle -> println("Invalid puzzle")
        is UniqueSolution -> println("Unique solution found: ${result.solution}")
        is TooHard -> println("Too hard")
    }
}