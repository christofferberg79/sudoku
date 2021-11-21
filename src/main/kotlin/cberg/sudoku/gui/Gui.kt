@file:OptIn(ExperimentalComposeUiApi::class)

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
import java.awt.event.KeyEvent.KEY_PRESSED

fun gui() = singleWindowApplication(title = "Sudoku") {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        game("..2.3...8.....8....31.2.....6..5.27..1.....5.2.4.6..31....8.6.5.......13..531.4..")
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
private fun game(initialState: String) {
    val model = remember { Model(initialState) }
    val game = model.game
    val settings = model.settings

    val dim = GameDimensions(square = 50.dp, thinLine = 1.5.dp, thickLine = 3.dp)

    Row {
        Box(modifier = dim.gameModifier().background(Color.Black)) {
            game.squares.forEachIndexed { index, square ->
                square(
                    modifier = dim.squareModifier(square.position).background(Color.White),
                    square = square,
                    onType = { char -> model.writeChar(index, char) },
                    onDelete = { model.deleteChar(index) }
                )
            }
        }
        Column(Modifier.padding(10.dp)) {
            Text(
                text = when (game.status) {
                    GameStatus.NotDone -> "Not Done"
                    GameStatus.IncorrectSolution -> "Incorrect"
                    GameStatus.CorrectSolution -> "Correct"
                }
            )

            Row {
                Text(text = "Pencil:", modifier = Modifier.align(Alignment.CenterVertically))
                Switch(checked = settings.pencil, onCheckedChange = { model.togglePencil() })
            }

            Row {
                Text(text = "Auto-erase pencil marks:", modifier = Modifier.align(Alignment.CenterVertically))
                Switch(checked = settings.autoErasePencilMarks, onCheckedChange = { model.toggleAutoErasePencilMarks() })
            }

            Button(onClick = model::writePencilMarks) {
                Text("Write pencil marks")
            }
        }
    }
}

@Composable
fun square(
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
        if (square.value == null) {
            squareMarks(square)
        } else {
            squareValue(square)
        }
    }
}

@Composable
private fun squareMarks(square: Square) {
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
private fun squareValue(square: Square) {
    Box(Modifier.fillMaxSize()) {
        Text(
            text = "${square.value}",
            modifier = Modifier.align(Alignment.Center),
            fontSize = 40.sp,
            color = if (square.given) Color.Black else Color.Blue
        )
    }
}

private sealed class SquareInput {
    object Delete : SquareInput()
    data class Value(val value: Char) : SquareInput()
}

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
