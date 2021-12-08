package cberg.sudoku

import cberg.sudoku.gui.gui
import cberg.sudoku.solver.InvalidPuzzle
import cberg.sudoku.solver.Solver
import cberg.sudoku.solver.TooHard
import cberg.sudoku.solver.UniqueSolution
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.bufferedReader
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

fun main(args: Array<String>) {
    when {
        args.isEmpty() -> gui()
        args.size == 1 && args[0] == "testfiles" -> testfiles()
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

@OptIn(ExperimentalTime::class)
fun testfiles() {
    val solver = Solver()
    val inputRegex = Regex("[1-9.]{81}")

    println("File                                    Total  Attempts    Solved      %          Time")
    println("--------------------------------------------------------------------------------------")
    var total = 0
    var attempts = 0
    var solved = 0
    var time = Duration.seconds(0)
    val formatString = "%-35s%10d%10d%10d%7d %%%10d s"
    Path("./test/data").listDirectoryEntries().filter { it.isRegularFile() }.forEach { file ->
        var fileTotal = 0
        var fileAttempts = 0
        var fileSolved = 0
        val fileTime = measureTime {
            file.bufferedReader().useLines { lines ->
                lines.filter { line -> line.matches(inputRegex) }
                    .forEachIndexed { index, input ->
                        fileTotal++
                        if (index < 100000) {
                            fileAttempts++
                            if (solver.solve(input) is UniqueSolution) {
                                fileSolved++
                            }
                        }
                    }
            }
        }
        println(
            formatString.format(
                file.fileName,
                fileTotal,
                fileAttempts,
                fileSolved,
                fileSolved * 100 / fileAttempts,
                fileTime.inWholeSeconds
            )
        )
        total += fileTotal
        attempts += fileAttempts
        solved += fileSolved
        time += fileTime
    }
    println("--------------------------------------------------------------------------------------")
    println(
        formatString.format(
            "Total",
            total,
            attempts,
            solved,
            solved * 100 / attempts,
            time.inWholeSeconds
        )
    )
}
