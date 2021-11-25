package cberg.sudoku.solver

import cberg.sudoku.game.*

class Solver {
    fun solve(input: String): Solution {
        val game = Game(input).writePencilMarks()

        if (game.isInvalidPuzzle()) {
            return InvalidPuzzle
        }

        val solvedGame = solve(game)

        return if (solvedGame.squares.any(Square::isEmpty)) {
            TooHard
        } else {
            UniqueSolution(solvedGame.toString())
        }
    }

    private fun Game.isInvalidPuzzle() = insufficientGivens()
            || duplicateGivens()
            || noCandidate()
            || missingCandidate()

    private fun Game.insufficientGivens() = squares.count(Square::isNotEmpty) < 17

    private fun Game.duplicateGivens() = groups.any { group ->
        group.map { position -> squareAt(position) }
            .filter(Square::isNotEmpty)
            .groupingBy(Square::value)
            .eachCount()
            .values.any { it > 1 }
    }

    private fun Game.noCandidate() = squares.any { square ->
        square.isEmpty() && square.marks.isEmpty()
    }

    private fun Game.missingCandidate() = symbols.any { symbol ->
        groups.any { group ->
            group.map { position -> squareAt(position) }
                .none { square -> symbol == square.value || symbol in square.marks }
        }
    }

}

val symbols = '1'..'9'

sealed class Solution
class UniqueSolution(val solution: String) : Solution()
object InvalidPuzzle : Solution()
object TooHard : Solution()

fun solve(game: Game) = generateSequence(game, ::applyFirstAction).last()

private fun applyFirstAction(game: Game) = game.actions().firstOrNull()?.applyTo(game)

fun Game.filteredActions(): Sequence<Action> {
    return ActionSequence(actions())
}

private fun Game.actions(): Sequence<Action> = nakedSingles() +
        hiddenSingles() +
        nakedTuples(2) +
        hiddenTuples(2) +
        nakedTuples(3) +
        hiddenTuples(3)

private fun Game.nakedSingles(): Sequence<Action> = squares.asSequence()
    .filter { square -> square.marks.size == 1 }
    .map { square ->
        val position = square.position
        val value = square.marks.single()
        Action.SetValue(position, value, Technique.NakedSingle(position, value))
    }

private fun Game.hiddenSingles(): Sequence<Action> = groups.flatMap { group ->
    symbols.asSequence().mapNotNull { symbol ->
        group.singleOrNull { position -> symbol in squareAt(position).marks }
            ?.let { position -> Action.SetValue(position, symbol, Technique.HiddenSingle(position, symbol)) }
    }
}

private fun Game.nakedTuples(n: Int): Sequence<Action> = groups.flatMap { group ->
    group.asSequence()
        .map { position -> squareAt(position) }
        .filter { square -> square.isEmpty() }
        .let { squares -> tuplesFrom(squares, n) }
        .map { squares -> squares to squares.map { square -> square.marks }.reduce(Set<Char>::plus) }
        .filter { (_, marks) -> marks.size == n }
        .map { (squares, marks) -> squares.map { square -> square.position } to marks }
        .flatMap { (tuplePositions, tupleMarks) ->
            group.asSequence()
                .filterNot { position -> position in tuplePositions }
                .map { position -> position to tupleMarks.intersect(squareAt(position).marks) }
                .filterNot { (_, marks) -> marks.isEmpty() }
                .map { (position, marks) ->
                    Action.EraseMarks(position, marks.toSet(), Technique.NakedTuple(n, tuplePositions, tupleMarks))
                }
        }
}

private fun Game.hiddenTuples(n: Int): Sequence<Action> = groups.flatMap { group ->
    val emptySquares = group
        .map { position -> squareAt(position) }
        .filter { square -> square.isEmpty() }
    val marks = emptySquares.fold(mutableSetOf<Char>()) { marks, square -> marks.apply { addAll(square.marks) } }
    val tuples = tuplesFrom(marks.asSequence(), n)
    tuples.associateWith { tuple -> emptySquares.filter { square -> square.marks.any { c -> tuple.contains(c) } } }
        .filterValues { squares -> squares.size == n }
        .flatMap { (tuple, squares) ->
            squares.map { square -> square.position to square.marks - tuple }
                .filterNot { (_, marksToErase) -> marksToErase.isEmpty() }
                .map { (position, marksToErase) ->
                    val technique = Technique.HiddenTuple(n, squares.map { it.position }, tuple)
                    Action.EraseMarks(position, marksToErase, technique)
                }
        }
}

private fun <E> tuplesFrom(l: Sequence<E>, n: Int): Sequence<Set<E>> {
    require(n >= 1)

    return if (n == 1) {
        l.map { setOf(it) }
    } else {
        l.flatMapIndexed { i, v ->
            tuplesFrom(l.drop(i + 1), n - 1)
                .map { it + v }
        }
    }
}
