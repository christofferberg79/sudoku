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

fun solve(game: Game) = generateSequence(game, ::applyFirstHint).last()

private fun applyFirstHint(game: Game) = game.hints().firstOrNull()?.applyTo(game)

fun Game.filteredHints(): Sequence<Hint> {
    return HintSequence(hints(), this)
}

private fun Game.hints(): Sequence<Hint> = nakedSingles() +
        hiddenSingles() +
        nakedTuples(2) +
        hiddenTuples(2) +
        nakedTuples(3) +
        hiddenTuples(3)

private fun Game.nakedSingles(): Sequence<Hint> = squares.asSequence()
    .filter { square -> square.marks.size == 1 }
    .map { square ->
        val position = square.position
        val value = square.marks.single()
        Hint(Action.SetValue(position, value), Reason(position, value), Technique.NakedSingle)
    }

private fun Game.hiddenSingles(): Sequence<Hint> = groups.flatMap { group ->
    symbols.asSequence().mapNotNull { symbol ->
        group.singleOrNull { position -> symbol in squareAt(position).marks }
            ?.let { position ->
                Hint(Action.SetValue(position, symbol), Reason(position, symbol), Technique.HiddenSingle)
            }
    }
}

private fun Game.nakedTuples(n: Int): Sequence<Hint> = groups.flatMap { group ->
    group.asSequence()
        .map { position -> squareAt(position) }
        .filter { square -> square.isEmpty() }
        .let { squares -> tuplesFrom(squares, n) }
        .map { squares -> squares to squares.map { square -> square.marks }.reduce(Set<Char>::plus) }
        .filter { (_, marks) -> marks.size == n }
        .map { (squares, marks) -> Reason(squares.map { square -> square.position }, marks) }
        .mapNotNull { reason ->
            val actions = group.asSequence()
                .filterNot { position -> position in reason.positions }
                .map { position -> position to reason.marks.intersect(squareAt(position).marks) }
                .filterNot { (_, marks) -> marks.isEmpty() }
                .map { (position, marks) -> Action.EraseMarks(position, marks.toSet()) }
                .toList()
            if (actions.isEmpty()) {
                null
            } else {
                Hint(actions, reason, Technique.NakedTuple(n))
            }
        }
}

private fun Game.hiddenTuples(n: Int): Sequence<Hint> = groups.flatMap { group ->
    val emptySquares = group
        .map { position -> squareAt(position) }
        .filter { square -> square.isEmpty() }
    val marks = emptySquares.fold(mutableSetOf<Char>()) { marks, square -> marks.apply { addAll(square.marks) } }
    val tuples = tuplesFrom(marks.asSequence(), n)
    tuples.associateWith { tuple -> emptySquares.filter { square -> square.marks.any { c -> tuple.contains(c) } } }
        .filterValues { squares -> squares.size == n }
        .mapNotNull { (tuple, squares) ->
            val reason = Reason(squares.map { it.position }, tuple)
            val technique = Technique.HiddenTuple(n)
            val actions = squares.map { square -> square.position to square.marks - tuple }
                .filterNot { (_, marksToErase) -> marksToErase.isEmpty() }
                .map { (position, marksToErase) -> Action.EraseMarks(position, marksToErase) }
            if (actions.isEmpty()) {
                null
            } else {
                Hint(actions, reason, technique)
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
