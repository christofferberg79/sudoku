package cberg.sudoku.gui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import cberg.sudoku.gui.Model.Square
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
    fun squareModifier(col: Int, row: Int) = Modifier.size(square).offset(squareOffset(col), squareOffset(row))
}

@Composable
private fun game(initialState: String) {
    val model = remember { Model(initialState) }
    val state = model.state

    val dim = GameDimensions(square = 36.dp, thinLine = 1.dp, thickLine = 2.dp)

    Box(modifier = dim.gameModifier().background(Color.Black)) {
        for (row in 0..8) {
            for (col in 0..8) {
                val i = row * 9 + col
                square(
                    modifier = dim.squareModifier(col, row).background(Color.White),
                    square = state.squares[i],
                    onType = { char -> model.writeChar(i, char) },
                    onDelete = { model.deleteChar(i) }
                )
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
        modifier = modifier.mapIf(!square.given) {
            it.focusRequester(focusRequester)
                .clickable { focusRequester.requestFocus() }
                .onKeyEvent { event ->
                    when (val input = event.toSquareInput()) {
                        is SquareInput.Value -> onType(input.value)
                        is SquareInput.Delete -> onDelete()
                        else -> return@onKeyEvent false
                    }
                    return@onKeyEvent true
                }
        }
    ) {
        if (square.value == null) {
            squareMarks(square)
        } else {
            squareValue(square)
        }
    }
}

private inline fun <T> T.mapIf(condition: Boolean, transform: (T) -> T) =
    if (condition) transform(this) else this

@Composable
private fun squareMarks(square: Square) {
    Column(Modifier.fillMaxSize().padding(2.dp)) {
        for (row in 0..2) {
            Row(Modifier.fillMaxWidth().weight(1f)) {
                for (col in 0..2) {
                    val c = '1' + row * 3 + col
                    Box(Modifier.weight(1f).fillMaxSize()) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = if (c in square.marks) "$c" else "",
                            fontSize = 10.sp
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
            fontSize = 30.sp,
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
