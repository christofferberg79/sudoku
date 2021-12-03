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

class Model(input: String) {
    private var initialGrid by mutableStateOf(Grid(input))

    var grid by mutableStateOf(initialGrid)
        private set

    val given by derivedStateOf { initialGrid.cells.filter { it.isNotEmpty() }.map { it.position }.toSet() }

    var settings by mutableStateOf(Settings())
        private set

    val hints by derivedStateOf { grid.filteredHints() }

    val gameStatus by derivedStateOf { grid.getStatus() }

    var analyzing by mutableStateOf<Char?>(null)

    private inline fun updateGrid(update: Grid.() -> Grid) {
        grid = grid.update()
    }

    private inline fun updateSettings(update: Settings.() -> Settings) {
        settings = settings.update()
    }

    fun reset() = updateGrid { initialGrid }

    fun writeChar(position: Position, char: Char) {
        if (position in given) {
            return
        }

        updateGrid {
            if (settings.pencil) {
                toggleCandidate(position, char)
            } else if (settings.autoErasePencilMarks) {
                setDigitAndEraseCandidates(position, char)
            } else {
                setDigit(position, char)
            }
        }
    }

    fun erase(position: Position) {
        if (position in given) {
            return
        }

        updateGrid {
            erase(position)
        }
    }

    fun writePencilMarks() = updateGrid {
        setAllCandidates()
    }

    fun togglePencil() = updateSettings {
        copy(pencil = !pencil)
    }

    fun toggleAutoErasePencilMarks() = updateSettings {
        copy(autoErasePencilMarks = !autoErasePencilMarks)
    }

    fun solve() = updateGrid {
        solve(this)
    }

    fun apply(hint: Hint) = updateGrid {
        hint.applyTo(this)
    }

    fun startNewGame(gameString: String) {
        initialGrid = Grid(gameString)
        reset()
    }

    fun analyze(char: Char) {
        analyzing = if (char == analyzing) {
            null
        } else {
            char
        }
    }
}
