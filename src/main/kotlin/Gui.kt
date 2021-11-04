package cberg.sudoku

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun gui() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Sudoku",
        state = rememberWindowState(width = 450.dp, height = 450.dp)
    ) {
        Box(Modifier.padding(20.dp)) {
            game()
        }
    }
}

@Composable
private fun game() {
    val state = remember {
        mutableStateOf("..2.3...8.....8....31.2.....6..5.27..1.....5.2.4.6..31....8.6.5.......13..531.4..")
    }

    Column {
        for (i1 in 0..54 step 27) {
            Row {
                for (i2 in 0..6 step 3) {
                    block(i1 + i2, state.value)
                }
            }
        }
    }
}

@Composable
fun block(startIndex: Int, state: String) {
    Column(modifier = Modifier.border(2.dp, Color.Black)) {
        for (i1 in 0..18 step 9) {
            Row {
                for (i2 in 0..2) {
                    val index = startIndex + i1 + i2
                    square(index, state)
                }
            }
        }
    }
}

@Composable
fun square(index: Int, state: String) {
    Box(modifier = Modifier.width(40.dp).height(40.dp).border(0.5.dp, Color.Black)) {
//        Text(index.toString(), Modifier.align(Alignment.Center))
        val c = state[index]
        if (c in '1'..'9') {
            Text(c.toString(), Modifier.align(Alignment.Center))
        }
    }
}