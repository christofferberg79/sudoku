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
        game()
    }
}

@Composable
private fun game() {
    var state by remember {
        mutableStateOf("..2.3...8.....8....31.2.....6..5.27..1.....5.2.4.6..31....8.6.5.......13..531.4..")
    }

    Column {
        for (row in 0..8) {
            Row {
                for (col in 0..8) {
                    val i = row * 9 + col
                    square(
                        char = state[i],
                        onTyped = { char ->
                            state = state.substring(0, i) + char + state.substring(i + 1)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun square(char: Char, onTyped: (Char) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    Box(
        modifier = Modifier
            .size(40.dp)
            .border(1.dp, Color.Black)
            .focusRequester(focusRequester)
            .clickable { focusRequester.requestFocus() }
            .onKeyEvent { event ->
                if (event.nativeKeyEvent.id == KEY_PRESSED && event.nativeKeyEvent.keyChar in '1'..'9'
                ) {
                    onTyped(event.nativeKeyEvent.keyChar)
                    return@onKeyEvent true
                } else {
                    return@onKeyEvent false
                }
            }
    ) {
        if (char in '1'..'9') {
            Text(
                text = char.toString(),
                modifier = Modifier.align(Alignment.Center),
                fontSize = 30.sp
            )
        }
    }
}