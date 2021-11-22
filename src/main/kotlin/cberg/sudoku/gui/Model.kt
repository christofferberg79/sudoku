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
    var game by mutableStateOf(Game(input))
        private set

    var settings by mutableStateOf(Settings())
        private set

    private inline fun updateGame(update: Game.() -> Game) {
        game = game.update()
    }

    private inline fun updateSettings(update: Settings.() -> Settings) {
        settings = settings.update()
    }

    fun writeChar(position: Position, char: Char) = updateGame {
        writeChar(position, char)
    }

    private fun Game.writeChar(position: Position, char: Char): Game {
        if (settings.pencil) {
            if (game[position].value == null) {
                return toggleMark(position, char)
            }
        } else {
            if (!game[position].given && game[position].value != char) {
                return if (settings.autoErasePencilMarks) {
                    setValue(position, char).eraseMarks(position)
                } else {
                    setValue(position, char)
                }
            }
        }
        return this
    }

    fun erase(position: Position) = updateGame {
        erase(position)
    }

    private fun Game.erase(position: Position): Game {
        if (!settings.pencil && !game[position].given) {
            return eraseValue(position)
        }
        return this
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
