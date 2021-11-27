package cberg.sudoku.gui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.singleWindowApplication
import cberg.sudoku.game.GameStatus
import cberg.sudoku.game.Position
import cberg.sudoku.game.Square
import cberg.sudoku.game.isEmpty
import cberg.sudoku.solver.Hint

fun gui() = singleWindowApplication(title = "Sudoku") {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val model = remember {
            Model("..4..1..8........73..4.....1..2.6..9....387...2.....1..8.3...2..6..1.....7.....65")
        }
        Sudoku(model)
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
fun Sudoku(model: Model) {
    val game = model.game
    val settings = model.settings

    Row {
        Column {
            Game(
                squares = game.squares,
                onType = model::writeChar,
                onDelete = model::erase
            )
            NewGame(onNewGame = model::startNewGame)
        }

        Column(Modifier.padding(10.dp)) {
            Text(
                text = when (model.gameStatus) {
                    GameStatus.NotDone -> "Not Done"
                    GameStatus.IncorrectSolution -> "Incorrect"
                    GameStatus.CorrectSolution -> "Correct"
                }
            )

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

            Button(onClick = model::writePencilMarks) {
                Text("Write pencil marks")
            }

            Button(onClick = model::solve) {
                Text("Solve")
            }

            Hints(
                hints = model.hints,
                onClick = model::apply
            )
        }
    }
}

@Composable
fun Game(
    squares: List<Square>,
    onType: (Position, Char) -> Unit,
    onDelete: (Position) -> Unit
) {
    val dim = GameDimensions(square = 50.dp, thinLine = 1.5.dp, thickLine = 3.dp)
    Box(modifier = dim.gameModifier().background(Color.Black)) {
        for (square in squares) {
            Square(
                modifier = dim.squareModifier(square.position),
                square = square,
                onType = { char -> onType(square.position, char) },
                onDelete = { onDelete(square.position) }
            )
        }
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
    var isFocused by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .background(if (isFocused) Color.LightGray else Color.White)
            .focusRequester(focusRequester)
            .onFocusChanged { focusState -> isFocused = focusState.hasFocus }
            .clickable(interactionSource, indication = null) { focusRequester.requestFocus() }
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
fun SquareMarks(square: Square) {
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
fun SquareValue(square: Square) {
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
fun NewGame(onNewGame: (String) -> Unit) {
    Row {
        var gameString by remember { mutableStateOf("") }
        TextField(
            value = gameString,
            onValueChange = { gameString = it }
        )
        Button(onClick = { onNewGame(gameString) }) {
            Text("Start New Game")
        }
    }
}

@Composable
fun Setting(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row {
        Switch(checked = checked, onCheckedChange = onCheckedChange)
        Text(text = text, modifier = Modifier.align(Alignment.CenterVertically))
    }
}

@Composable
fun Hints(hints: Sequence<Hint>, onClick: (Hint) -> Unit) {
    val groupedHints = hints.groupBy { hint -> hint.technique }
    Column {
        Text(text = "Hints", fontSize = 20.sp)
        for ((technique, hintsInTechnique) in groupedHints) {
            Text(text = "$technique", fontSize = 16.sp)
            for (hint in hintsInTechnique) {
                Column(Modifier.padding(bottom = 10.dp).clickable { onClick(hint) }) {
                    Text("${hint.reason}")
                    for (action in hint.actions) {
                        Text("$action")
                    }
                }
            }
        }
    }
}

private sealed class SquareInput {
    object Delete : SquareInput()
    data class Value(val value: Char) : SquareInput()
}

@OptIn(ExperimentalComposeUiApi::class)
private fun KeyEvent.toSquareInput(): SquareInput? {
    if (type != KeyEventType.KeyDown) {
        return null
    }
    if (key == Key.Delete || key == Key.Backspace) {
        return SquareInput.Delete
    }
    val char = utf16CodePoint.toChar()
    if (char in '1'..'9') {
        return SquareInput.Value(char)
    }
    return null
}
