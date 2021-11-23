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
            UniqueSolution(solvedGame.getOutput())
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

    private fun Game.getOutput() = squares.joinToString(separator = "") { s -> "${s.value ?: '.'}" }

}

val symbols = '1'..'9'

sealed class Solution
class UniqueSolution(val solution: String) : Solution()
object InvalidPuzzle : Solution()
object TooHard : Solution()

sealed interface Action {
    operator fun invoke(game: Game): Game

    class SetValue(private val position: Position, private val value: Char) : Action {
        override fun invoke(game: Game) = game.setValueAndEraseMarks(position, value)
        override fun toString() = "$position => $value"
    }

    class EraseMarks(private val position: Position, private val marks: List<Char>) : Action {
        override fun invoke(game: Game) = marks.fold(game) { g, m -> g.eraseMark(position, m) }
        override fun toString() = "$position => erase marks: ${marks.joinToString()}"
    }
}

fun solve(game: Game) = generateSequence(game, ::next).last()

private fun next(game: Game) = game.actions().firstOrNull()?.let { action -> action(game) }

fun Game.actions(): Sequence<Action> = nakedSingles() + hiddenSingles() + nakedPairs() + nakedTriples()

private fun Game.nakedSingles(): Sequence<Action> = squares.asSequence()
    .filter { square -> square.marks.size == 1 }
    .map { square -> Action.SetValue(square.position, square.marks.single()) }

private fun Game.hiddenSingles(): Sequence<Action> = groups.flatMap { group ->
    symbols.asSequence().mapNotNull { symbol ->
        group.singleOrNull { position -> symbol in squareAt(position).marks }
            ?.let { position -> Action.SetValue(position, symbol) }
    }
}

private fun Game.nakedPairs(): Sequence<Action> = groups.flatMap { group ->
    group.asSequence()
        .map { position -> squareAt(position) }
        .filter { square -> square.isEmpty() }
        .let { squares -> pairsFrom(squares) }
        .map { (s1, s2) -> Triple(s1.position, s2.position, s1.marks + s2.marks) }
        .filter { (_, _, marks) -> marks.size == 2 }
        .flatMap { (p1, p2, marks) ->
            group.asSequence().filter { position -> position != p1 && position != p2 }
                .map { position -> position to marks.filter { c -> c in squareAt(position).marks } }
                .filterNot { (_, marks) -> marks.isEmpty() }
                .map { (position, marks) -> Action.EraseMarks(position, marks) }
        }
}

private fun Game.nakedTriples(): Sequence<Action> = groups.flatMap { group ->
    group.asSequence()
        .map { position -> squareAt(position) }
        .filter { square -> square.isEmpty() }
        .let { squares -> triplesFrom(squares) }
        .map { (s1, s2, s3) -> Triple(s1.position, s2.position, s3.position) to s1.marks + s2.marks + s3.marks }
        .filter { (_, marks) -> marks.size == 3 }
        .flatMap { (positions, marks) ->
            group.asSequence().filter { position -> position != positions.first && position != positions.second && position != positions.third }
                .map { position -> position to marks.filter { c -> c in squareAt(position).marks } }
                .filterNot { (_, marks) -> marks.isEmpty() }
                .map { (position, marks) -> Action.EraseMarks(position, marks) }
        }
}

private fun <E> pairsFrom(l: Sequence<E>): Sequence<Pair<E, E>> =
    l.flatMapIndexed { i, v1 ->
        l.drop(i + 1).map { v2 -> Pair(v1, v2) }
    }

private fun <E> triplesFrom(l: Sequence<E>): Sequence<Triple<E, E, E>> =
    l.flatMapIndexed { i1, v1 ->
        l.drop(i1 + 1).flatMapIndexed { i2, v2 ->
            l.drop(i1 + i2 + 2).map { v3 -> Triple(v1, v2, v3) }
        }
    }
