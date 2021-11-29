package cberg.sudoku.gui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusOrder
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.singleWindowApplication
import cberg.sudoku.game.*
import cberg.sudoku.solver.Hint

fun gui() = singleWindowApplication(title = "Sudoku") {
    Box(
        modifier = Modifier.fillMaxSize().padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        val model = remember {
            Model("..4..1..8........73..4.....1..2.6..9....387...2.....1..8.3...2..6..1.....7.....65")
        }
        Sudoku(model)
    }
}

@Composable
fun Sudoku(model: Model) {
    val game = model.game
    val settings = model.settings

    Row {
        Column {
            Game(
                game = game,
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
    game: Game,
    onType: (Position, Char) -> Unit,
    onDelete: (Position) -> Unit
) {
    val squares = game.squares.associateBy { square -> square.position }
    val focusRequesters = remember { squares.keys.associateWith { FocusRequester() } }

    SudokuGrid(
        size = 468.dp,
        thickLine = BorderStroke(2.dp, Color.Black),
        thinLine = BorderStroke(1.dp, Color.Gray)
    ) { row, col ->
        val position = Position(row, col)
        Square(
            square = squares.getValue(position),
            focusRequesters = focusRequesters,
            onType = { char -> onType(position, char) },
            onDelete = { onDelete(position) }
        )
    }
}

@Composable
private fun SudokuGrid(
    size: Dp,
    thickLine: BorderStroke,
    thinLine: BorderStroke,
    content: @Composable (row: Int, col: Int) -> Unit
) {
    Box(Modifier.size(size).background(thickLine.brush).padding(thickLine.width)) {
        Grid(thickLine) { outerRow, outerCol ->
            Grid(thinLine) { innerRow, innerCol ->
                content(outerRow * 3 + innerRow, outerCol * 3 + innerCol)
            }
        }
    }
}

@Composable
private fun Grid(
    line: BorderStroke = BorderStroke(0.dp, Color.Transparent),
    content: @Composable (row: Int, col: Int) -> Unit
) {
    Column(Modifier.background(line.brush), verticalArrangement = Arrangement.spacedBy(line.width)) {
        repeat(3) { row ->
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(line.width)) {
                repeat(3) { col ->
                    Box(Modifier.weight(1f)) {
                        content(row, col)
                    }
                }
            }
        }
    }
}

@Composable
fun Square(
    square: Square,
    focusRequesters: Map<Position, FocusRequester>,
    onType: (Char) -> Unit,
    onDelete: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val focusRequester = focusRequesters.getValue(square.position)
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .background(if (isFocused) Color.LightGray else Color.White)
            .focusOrder(focusRequester) {
                up = focusRequesters.getValue(square.position.up())
                down = focusRequesters.getValue(square.position.down())
                left = focusRequesters.getValue(square.position.left())
                right = focusRequesters.getValue(square.position.right())
            }
            .onFocusChanged { focusState -> isFocused = focusState.hasFocus }
            .clickable(interactionSource, indication = null) { focusRequester.requestFocus() }
            .onKeyEvent { event ->
                when (val input = event.toSquareInput()) {
                    is SquareInput.Value -> onType(input.value)
                    is SquareInput.Delete -> onDelete()
                    is SquareInput.Move -> focusManager.moveFocus(input.direction)
                    else -> return@onKeyEvent false
                }
                return@onKeyEvent true
            }
            .padding(1.dp)
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
    Grid { row, col ->
        val c = '1' + row * 3 + col
        if (c in square.marks) {
            Box(Modifier.fillMaxSize()) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = "$c",
                    fontSize = 14.sp
                )
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
    data class Move(val direction: FocusDirection) : SquareInput()
}

@OptIn(ExperimentalComposeUiApi::class)
private fun KeyEvent.toSquareInput(): SquareInput? {
    when {
        type != KeyEventType.KeyDown -> return null
        key == Key.Delete || key == Key.Backspace -> return SquareInput.Delete
        key == Key.DirectionUp -> return SquareInput.Move(FocusDirection.Up)
        key == Key.DirectionDown -> return SquareInput.Move(FocusDirection.Down)
        key == Key.DirectionLeft -> return SquareInput.Move(FocusDirection.Left)
        key == Key.DirectionRight -> return SquareInput.Move(FocusDirection.Right)
        else -> {
            val char = utf16CodePoint.toChar()
            if (char in '1'..'9') {
                return SquareInput.Value(char)
            }
            return null
        }
    }
}
