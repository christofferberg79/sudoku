package cberg.sudoku.gui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
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
        game(
            "..2.3...8.....8....31.2.....6..5.27..1.....5.2.4.6..31....8.6.5.......13..531.4..",
            GameDimensions(square = 36.dp, thinLine = 1.dp, thickLine = 2.dp)
        )
    }
}

class GameDimensions(private val square: Dp, private val thinLine: Dp, private val thickLine: Dp) {
    fun gameSize() = thickLine * 4 + thinLine * 6 + square * 9
    fun squareOffset(i: Int) = thickLine + (square + thinLine) * i + (thickLine - thinLine) * (i / 3)
    fun squareSize() = square
}

@Composable
private fun game(initialState: String, dimensions: GameDimensions) {
    val model = remember { Model(initialState) }
    val state = model.state

    Box(modifier = Modifier.size(dimensions.gameSize()).background(Color.Black)) {
        for (row in 0..8) {
            for (col in 0..8) {
                val i = row * 9 + col
                square(
                    modifier = Modifier.size(dimensions.squareSize())
                        .offset(dimensions.squareOffset(col), dimensions.squareOffset(row)),
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
        modifier = modifier
            .background(Color.White)
            .run {
                if (square.given) {
                    this
                } else {
                    this.focusRequester(focusRequester)
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
            }
    ) {
        if (square.value != null) {
            Text(
                text = square.value.toString(),
                modifier = Modifier.align(Alignment.Center),
                fontSize = 30.sp,
                color = if (square.given) Color.Black else Color.Blue
            )
        }
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
