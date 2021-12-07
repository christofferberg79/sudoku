package cberg.sudoku

import cberg.sudoku.gui.gui
import cberg.sudoku.solver.InvalidPuzzle
import cberg.sudoku.solver.Solver
import cberg.sudoku.solver.TooHard
import cberg.sudoku.solver.UniqueSolution
import java.io.File
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

fun main(args: Array<String>) {
    when {
        args.isEmpty() -> gui()
        args.size == 2 && args[0] == "solve" -> solve(args[1])
        args.size == 2 && args[0] == "file" -> solveFile(args)
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

@OptIn(ExperimentalTime::class)
private fun solveFile(args: Array<String>) {
    println("File: ${args[1]}")
    File(args[1]).bufferedReader().use { reader ->
        var count = 0
        val solver = Solver()
        val map = mutableMapOf<String, Int>()
        val time = measureTime {
            do {
                reader.readLine()?.let { input ->
                    if (input.matches(Regex("[1-9.]{81}"))) {
                        count++
                        val result = solver.solve(input)
                        map.merge(result::class.simpleName!!, 1) { a, b -> a + b }
                    }
                } ?: break
            } while (count < 1000)
        }
        println("Attempted $count puzzles in ${time.inWholeMilliseconds} ms")
        for ((r, n) in map) {
            println("%-20s %5d".format(r, n))
        }
    }
}
