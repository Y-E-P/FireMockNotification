package ui.console

import SplitterState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import resources.ResString
import utils.cursorForHorizontalResize

@Composable
fun ConsoleOutput(
    modifier: Modifier = Modifier,
    itemsList: List<ConsoleItem>,
    splitterState: SplitterState,
    onOpenConsole: () -> Unit,
    onResize: (delta: Dp) -> Unit
) {
    val density = LocalDensity.current
    Column(modifier.fillMaxWidth()) {
        Divider(modifier = Modifier.fillMaxWidth().height(2.dp).run {
            return@run this.draggable(
                state = rememberDraggableState {
                    with(density) {
                        onResize(it.toDp())
                    }
                },
                orientation = Orientation.Vertical,
                startDragImmediately = true,
                onDragStarted = { splitterState.isResizing = true },
                onDragStopped = { splitterState.isResizing = false },
            ).cursorForHorizontalResize()
        })
        Row(modifier = Modifier.fillMaxWidth().wrapContentHeight().background(Color.LightGray)) {
            Text(ResString.console, modifier = Modifier.padding(4.dp).align(Alignment.CenterVertically))
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier.padding(4.dp).align(Alignment.CenterVertically)
                    .clickable(role = Role.Button) {
                        onOpenConsole()
                    }) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
        }
        LazyColumn(modifier = Modifier.background(Color.Black).fillMaxSize()) {
            items(itemsList, key = { it.id }) {
                val color = when (it.type) {
                    ConsoleItem.Type.ERROR -> Color.Red
                    ConsoleItem.Type.OUTPUT -> Color.White
                    ConsoleItem.Type.INPUT -> Color.Green
                }
                SelectionContainer {
                    Row {
                        Text(
                            it.time,
                            color = Color.White,
                            modifier = Modifier.wrapContentWidth().wrapContentHeight().align(Alignment.Top)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            it.data,
                            color = color,
                            modifier = Modifier.fillMaxWidth().wrapContentHeight().align(Alignment.Top)
                        )
                    }
                }
            }
        }
    }
}
