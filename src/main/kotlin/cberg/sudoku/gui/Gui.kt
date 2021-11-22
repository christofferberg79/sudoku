package cberg.sudoku.gui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.singleWindowApplication
import cberg.sudoku.game.*
import cberg.sudoku.solver.actions
import java.awt.event.KeyEvent.KEY_PRESSED

fun gui() = singleWindowApplication(title = "Sudoku") {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        app("..4..1..8........73..4.....1..2.6..9....387...2.....1..8.3...2..6..1.....7.....65")
    }
}

class GameDimensions(private val square: Dp, private val thinLine: Dp, private val thickLine: Dp) {
    private fun gameSize() = thickLine * 4 + thinLine * 6 + square * 9
    private fun squareOffset(i: Int) = thickLine + (square + thinLine) * i + (thickLine - thinLine) * (i / 3)

    fun gameModifier() = Modifier.size(gameSize())
    fun squareModifier(position: Position) = Modifier
        .size(square)
        .offset(squareOffset(position.col), squareOffset(position.row))
}

@Composable
private fun app(initialState: String) {
    val model = remember { Model(initialState) }
    val game = model.game
    val settings = model.settings


    Row {
        Game(game, model)

        Column(Modifier.padding(10.dp)) {
            Text(
                text = when (game.status) {
                    GameStatus.NotDone -> "Not Done"
                    GameStatus.IncorrectSolution -> "Incorrect"
                    GameStatus.CorrectSolution -> "Correct"
                }
            )

            Settings(settings, model)

            Button(onClick = model::writePencilMarks) {
                Text("Write pencil marks")
            }

            Button(onClick = model::solve) {
                Text("Solve")
            }

            Hints(game, model)
        }
    }
}

@Composable
private fun Game(game: Game, model: Model) {
    val dim = GameDimensions(square = 50.dp, thinLine = 1.5.dp, thickLine = 3.dp)
    Box(modifier = dim.gameModifier().background(Color.Black)) {
        for (square in game.squares) {
            Square(
                modifier = dim.squareModifier(square.position).background(Color.White),
                square = square,
                onType = { char -> model.writeChar(square.position, char) },
                onDelete = { model.erase(square.position) }
            )
        }
    }
}

@Composable
private fun Settings(settings: Settings, model: Model) {
    Setting(
        text = "Pencil marks",
        checked = settings.pencil,
        onCheckedChange = { model.togglePencil() }
    )

    Setting(
        text = "Auto-erase pencil marks",
        checked = settings.autoErasePencilMarks,
        onCheckedChange = { model.toggleAutoErasePencilMarks() }
    )
}

@Composable
private fun Setting(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row {
        Switch(checked = checked, onCheckedChange = onCheckedChange)
        Text(text = text, modifier = Modifier.align(Alignment.CenterVertically))
    }
}

@Composable
fun Square(
    modifier: Modifier,
    square: Square,
    onType: (Char) -> Unit,
    onDelete: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    Box(
        modifier = modifier
            .focusRequester(focusRequester)
            .clickable { focusRequester.requestFocus() }
            .onKeyEvent { event ->
                when (val input = event.toSquareInput()) {
                    is SquareInput.Value -> onType(input.value)
                    is SquareInput.Delete -> onDelete()
                    else -> return@onKeyEvent false
                }
                return@onKeyEvent true
            }
    ) {
        if (square.isEmpty()) {
            SquareMarks(square)
        } else {
            SquareValue(square)
        }
    }
}

@Composable
private fun SquareMarks(square: Square) {
    Column(Modifier.fillMaxSize().padding(1.dp)) {
        for (row in 0..2) {
            Row(Modifier.fillMaxWidth().weight(1f)) {
                for (col in 0..2) {
                    val c = '1' + row * 3 + col
                    Box(Modifier.weight(1f).fillMaxSize()) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = if (c in square.marks) "$c" else "",
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SquareValue(square: Square) {
    Box(Modifier.fillMaxSize()) {
        Text(
            text = "${square.value}",
            modifier = Modifier.align(Alignment.Center),
            fontSize = 40.sp,
            color = if (square.given) Color.Black else Color.Blue
        )
    }
}

@Composable
fun Hints(game: Game, model: Model) {
    Column {
        Text(text = "Hints", fontSize = 20.sp)
        for (action in game.actions()) {
            Text(text = action.toString(), modifier = Modifier.clickable { model.execute(action) })
        }
    }
}

private sealed class SquareInput {
    object Delete : SquareInput()
    data class Value(val value: Char) : SquareInput()
}

@OptIn(ExperimentalComposeUiApi::class)
private fun KeyEvent.toSquareInput(): SquareInput? {
    if (nativeKeyEvent.id != KEY_PRESSED) {
        return null
    }
    if (key == Key.Delete || key == Key.Backspace) {
        return SquareInput.Delete
    }
    val char = nativeKeyEvent.keyChar
    if (char in '1'..'9') {
        return SquareInput.Value(char)
    }
    return null
}
