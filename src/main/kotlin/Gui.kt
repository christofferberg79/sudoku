package cberg.sudoku

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
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
        val newSquare = squares[i].copy(value = char)
        val newSquares = squares.subList(0, i) + newSquare + squares.subList(i + 1, squares.size)
        return GameState(newSquares)
    }
}

@Composable
private fun game(initialState: String) {
    var state by remember { mutableStateOf(GameState(initialState)) }

    Column {
        for (row in 0..8) {
            Row {
                for (col in 0..8) {
                    val i = row * 9 + col
                    square(
                        square = state.squares[i],
                        onTyped = { char ->
                            state = state.update(i, char)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun square(square: GameState.Square, onTyped: (Char) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    Box(
        modifier = Modifier
            .size(40.dp)
            .border(1.dp, Color.Black)
            .run {
                if (square.given) {
                    this
                } else {
                    this
                        .focusRequester(focusRequester)
                        .clickable { focusRequester.requestFocus() }
                        .onKeyEvent { event ->
                            if (event.nativeKeyEvent.id == KEY_PRESSED &&
                                event.nativeKeyEvent.keyChar in '1'..'9' &&
                                event.nativeKeyEvent.keyChar != square.value
                            ) {
                                onTyped(event.nativeKeyEvent.keyChar)
                                return@onKeyEvent true
                            } else {
                                return@onKeyEvent false
                            }
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