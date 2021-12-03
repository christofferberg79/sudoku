package cberg.sudoku.gui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.isTypedEvent
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.singleWindowApplication
import cberg.sudoku.game.*
import cberg.sudoku.solver.Hint

fun gui() = singleWindowApplication(title = "Sudoku") {
    val model = remember {
        Model("")
    }
    Sudoku(model)
}

@Composable
fun Sudoku(model: Model) {
    val game = model.grid
    val settings = model.settings

    Column(
        modifier = Modifier
            .padding(10.dp)
            .onKeyEvent { event ->
                val digit = event.key.toSudokuDigit()
                if (event.isCtrlPressed && event.type == KeyEventType.KeyDown && digit in Grid.digits) {
                    checkNotNull(digit)
                    model.analyze(digit)
                    return@onKeyEvent true
                }
                return@onKeyEvent false
            },
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Game(
                grid = game,
                given = model.given,
                analyzing = model.analyzing,
                onType = model::writeDigit,
                onDelete = model::erase
            )

            Column {
                Text(
                    text = when (model.gameStatus) {
                        GameStatus.NotDone -> "Not Done"
                        GameStatus.IncorrectSolution -> "Incorrect"
                        GameStatus.CorrectSolution -> "Correct"
                    }
                )

                Text(
                    text = "Analyzing: ${model.analyzing ?: "none"}",
                    modifier = Modifier.padding(top = 10.dp)
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

                Button(onClick = model::reset) {
                    Text("Reset")
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

        NewGame(onNewGame = model::startNewGame)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun Key.toSudokuDigit() = when (this) {
    Key.One -> 1
    Key.Two -> 2
    Key.Three -> 3
    Key.Four -> 4
    Key.Five -> 5
    Key.Six -> 6
    Key.Seven -> 7
    Key.Eight -> 8
    Key.Nine -> 9
    else -> null
}

@Composable
fun Game(
    grid: Grid,
    given: Set<Position>,
    analyzing: Int?,
    onType: (Position, Int) -> Unit,
    onDelete: (Position) -> Unit
) {
    val focusRequesters = remember { grid.cells.associate { square -> square.position to FocusRequester() } }

    SudokuGrid(
        size = 468.dp,
        thickLine = BorderStroke(2.dp, Color.Black),
        thinLine = BorderStroke(1.dp, Color.Gray)
    ) { position ->
        val focusRequester = focusRequesters.getValue(position)
        val focusManager = LocalFocusManager.current
        val square = grid.cellAt(position)
        Square(
            modifier = Modifier.focusOrder(focusRequester) {
                up = focusRequesters.getValue(position.up())
                down = focusRequesters.getValue(position.down())
                left = focusRequesters.getValue(position.left())
                right = focusRequesters.getValue(position.right())
            },
            cell = square,
            given = square.position in given,
            analyzing = analyzing,
            onInput = { input ->
                when (input) {
                    is SquareInput.Digit -> onType(position, input.digit)
                    is SquareInput.Delete -> onDelete(position)
                    is SquareInput.Move -> focusManager.moveFocus(input.direction)
                }
            },
            onClick = { focusRequester.requestFocus() }
        )
    }
}

@Composable
private fun SudokuGrid(
    size: Dp,
    thickLine: BorderStroke,
    thinLine: BorderStroke,
    content: @Composable BoxScope.(position: Position) -> Unit
) {
    Box(Modifier.size(size).background(thickLine.brush).padding(thickLine.width)) {
        Grid(thickLine) { outerRow, outerCol ->
            Grid(thinLine) { innerRow, innerCol ->
                content(Position(outerRow * 3 + innerRow, outerCol * 3 + innerCol))
            }
        }
    }
}

@Composable
private fun Grid(
    line: BorderStroke = BorderStroke(0.dp, Color.Transparent),
    content: @Composable BoxScope.(row: Int, col: Int) -> Unit
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
    modifier: Modifier = Modifier,
    cell: Cell,
    given: Boolean,
    analyzing: Int?,
    onInput: (SquareInput) -> Unit,
    onClick: () -> Unit
) {
    val background = when {
        cell.isNotEmpty() && cell.digit == analyzing -> Color.Blue
        cell.isEmpty() && analyzing in cell.candidates -> Color.Green
        else -> Color.White
    }
    Box(modifier
        .clickable { onClick() }
        .background(background)
        .onKeyEvent { event ->
            event.toSquareInput()?.let { input ->
                onInput(input)
                return@onKeyEvent true
            }
            return@onKeyEvent false
        }
        .fillMaxSize()
        .padding(1.dp)
    ) {
        if (cell.isEmpty()) {
            SquareMarks(cell)
        } else {
            SquareValue(cell, given)
        }
    }
}

@Composable
fun SquareMarks(cell: Cell) {
    Grid { row, col ->
        val digit = 1 + row * 3 + col
        if (digit in cell.candidates) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = "$digit",
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun BoxScope.SquareValue(cell: Cell, given: Boolean) {
    Text(
        text = "${cell.digit}",
        modifier = Modifier.align(Alignment.Center),
        fontSize = 40.sp,
        color = if (given) Color.Black else Color.Blue
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NewGame(onNewGame: (String) -> Unit) {
    var gameString by remember { mutableStateOf("") }
    TextField(
        value = gameString,
        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
        onValueChange = { newValue ->
            if (newValue.length <= 81) {
                gameString = newValue
            }
        },
        label = { Text("New Game") },
        modifier = Modifier
            .width(600.dp)
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown && (event.key == Key.Enter || event.key == Key.NumPadEnter)) {
                    onNewGame(gameString)
                    return@onPreviewKeyEvent true
                }
                return@onPreviewKeyEvent false
            }
    )
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

sealed class SquareInput {
    object Delete : SquareInput()
    data class Digit(val digit: Int) : SquareInput()
    data class Move(val direction: FocusDirection) : SquareInput()
}

@OptIn(ExperimentalComposeUiApi::class)
private fun KeyEvent.toSquareInput(): SquareInput? = when {
    isTypedEvent -> {
        when (val digit = utf16CodePoint.toChar().digitToIntOrNull()) {
            null -> null
            in Grid.digits -> SquareInput.Digit(digit)
            else -> null
        }
    }
    type == KeyEventType.KeyDown -> {
        when (key) {
            Key.Delete -> SquareInput.Delete
            Key.Backspace -> SquareInput.Delete
            Key.DirectionUp -> SquareInput.Move(FocusDirection.Up)
            Key.DirectionDown -> SquareInput.Move(FocusDirection.Down)
            Key.DirectionLeft -> SquareInput.Move(FocusDirection.Left)
            Key.DirectionRight -> SquareInput.Move(FocusDirection.Right)
            else -> null
        }
    }
    else -> null
}
