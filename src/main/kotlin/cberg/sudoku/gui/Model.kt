package cberg.sudoku.gui

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cberg.sudoku.game.*
import cberg.sudoku.solver.Hint
import cberg.sudoku.solver.filteredHints
import cberg.sudoku.solver.solve

data class Settings(
    val autoErasePencilMarks: Boolean = true,
    val pencil: Boolean = false
)

class Model(private val input: String) {
    var game by mutableStateOf(Game(input).writePencilMarks())
        private set

    var settings by mutableStateOf(Settings())
        private set

    val hints by derivedStateOf { game.filteredHints() }

    val gameStatus by derivedStateOf { game.getStatus() }

    var analyzing by mutableStateOf<Char?>(null)

    private inline fun updateGame(update: Game.() -> Game) {
        game = game.update()
    }

    private inline fun updateSettings(update: Settings.() -> Settings) {
        settings = settings.update()
    }

    fun reset() = updateGame {
        Game(input).writePencilMarks()
    }

    fun writeChar(position: Position, char: Char) = updateGame {
        if (settings.pencil) {
            toggleMark(position, char)
        } else if (settings.autoErasePencilMarks) {
            setValueAndEraseMarks(position, char)
        } else {
            setValue(position, char)
        }
    }

    fun erase(position: Position) = updateGame {
        if (settings.pencil) {
            this
        } else {
            eraseValue(position)
        }
    }

    fun writePencilMarks() = updateGame {
        writePencilMarks()
    }

    fun togglePencil() = updateSettings {
        copy(pencil = !pencil)
    }

    fun toggleAutoErasePencilMarks() = updateSettings {
        copy(autoErasePencilMarks = !autoErasePencilMarks)
    }

    fun solve() = updateGame {
        solve(this)
    }

    fun apply(hint: Hint) = updateGame {
        hint.applyTo(game)
    }

    fun startNewGame(gameString: String) = updateGame {
        Game(gameString).writePencilMarks()
    }

    fun analyze(char: Char) {
        analyzing = if (char == analyzing) {
            null
        } else {
            char
        }
    }
}
