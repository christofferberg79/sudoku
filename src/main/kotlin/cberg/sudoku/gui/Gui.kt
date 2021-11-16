package cberg.sudoku.gui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
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
        game(
            "..2.3...8.....8....31.2.....6..5.27..1.....5.2.4.6..31....8.6.5.......13..531.4..",
            GameDimensions(square = 36.dp, thinLine = 1.dp, thickLine = 2.dp)
        )
    }
}

class GameState(val squares: List<Square>) {
    constructor(input: String) : this(List(81) { i ->
        require(input.length == 81)
        require(input.all { c -> c == '.' || c in '1'..'9' })
        val given = input[i] in '1'..'9'
        val value = if (given) input[i] else null
        Square(value, given)
    })

    data class Square(val value: Char?, val given: Boolean)

    fun update(i: Int, char: Char?): GameState {
        if (squares[i].value == char) {
            return this
        }
        val newSquare = squares[i].copy(value = char)
        val newSquares = squares.subList(0, i) + newSquare + squares.subList(i + 1, squares.size)
        return GameState(newSquares)
    }
}

class GameDimensions(private val square: Dp, private val thinLine: Dp, private val thickLine: Dp) {
    fun gameSize() = thickLine * 4 + thinLine * 6 + square * 9
    fun squareOffset(i: Int) = thickLine + (square + thinLine) * i + (thickLine - thinLine) * (i / 3)
    fun squareSize() = square
}

@Composable
private fun game(initialState: String, dimensions: GameDimensions) {
    var state by remember { mutableStateOf(GameState(initialState)) }

    Box(modifier = Modifier.size(dimensions.gameSize()).background(Color.Black)) {
        for (row in 0..8) {
            for (col in 0..8) {
                val i = row * 9 + col
                square(
                    modifier = Modifier.size(dimensions.squareSize())
                        .offset(dimensions.squareOffset(col), dimensions.squareOffset(row)),
                    square = state.squares[i],
                    onType = { char -> state = state.update(i, char) },
                    onDelete = { state = state.update(i, null) }
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun square(modifier: Modifier, square: GameState.Square, onType: (Char) -> Unit, onDelete: () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    Box(
        modifier = modifier
            .background(Color.White)
            .run {
                if (square.given) {
                    this
                } else {
                    this
                        .focusRequester(focusRequester)
                        .clickable { focusRequester.requestFocus() }
                        .onKeyEvent { event ->
                            if (event.nativeKeyEvent.id != KEY_PRESSED) {
                                return@onKeyEvent false
                            }
                            if (event.key == Key.Delete || event.key == Key.Backspace) {
                                onDelete()
                                return@onKeyEvent true
                            }
                            val char = event.nativeKeyEvent.keyChar
                            if (char in '1'..'9') {
                                onType(char)
                                return@onKeyEvent true
                            }
                            return@onKeyEvent false
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