package cberg.sudoku.gui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cberg.sudoku.game.*

data class Settings(
    val autoErasePencilMarks: Boolean = true,
    val pencil: Boolean = false
)

class Model(input: String) {
    var game by mutableStateOf(initialGame(input))
        private set

    var settings by mutableStateOf(Settings())
        private set

    private inline fun updateGame(update: Game.() -> Game) {
        game = game.update()
    }

    private inline fun updateSettings(update: Settings.() -> Settings) {
        settings = settings.update()
    }

    fun writeChar(index: Int, char: Char) = updateGame {
        if (settings.pencil) {
            toggleMark(index, char)
        } else {
            if (settings.autoErasePencilMarks) {
                setValue(index, char).eraseMarks(index, char)
            } else {
                setValue(index, char)
            }
        }
    }

    fun erase(index: Int) = updateGame {
        if (!settings.pencil) {
            eraseValue(index)
        } else {
            this
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
}
