package cberg.sudoku

fun main(args: Array<String>) {
    if (args.size == 2 && args[0] == "solve") {
        val input = args[1]
        val solver = Solver()
        when (val result = solver.solve(input)) {
            is InvalidPuzzle -> println("Invalid puzzle")
            is UniqueSolution -> println("Unique solution found: ${result.solution}")
            is TooHard -> println("Too hard")
        }
    }
}