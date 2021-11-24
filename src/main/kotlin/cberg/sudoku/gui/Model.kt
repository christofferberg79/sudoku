package cberg.sudoku.gui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cberg.sudoku.game.*
import cberg.sudoku.solver.Action
import cberg.sudoku.solver.filteredActions
import cberg.sudoku.solver.solve

data class Settings(
    val autoErasePencilMarks: Boolean = true,
    val pencil: Boolean = false
)

class Model(input: String) {
    var game by mutableStateOf(Game(input).writePencilMarks())
        private set

    var settings by mutableStateOf(Settings())
        private set

    var actions by mutableStateOf(game.filteredActions())
        private set

    var gameStatus by mutableStateOf(game.getStatus())
        private set

    private inline fun updateGame(update: Game.() -> Game) {
        game = game.update()
        actions = game.filteredActions()
        gameStatus = game.getStatus()
    }

    private inline fun updateSettings(update: Settings.() -> Settings) {
        settings = settings.update()
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
        val gameWithMarks = writePencilMarks()
        solve(gameWithMarks)
    }

    fun apply(action: Action) = updateGame {
        action.applyTo(game)
    }

    fun startNewGame(gameString: String) = updateGame {
        Game(gameString)
    }
}
