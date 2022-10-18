package ui.dialogs

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import repo.Item
import resources.ResString
import ui.base.BaseDropdown
import ui.base.MultiToggleButton
import ui.broadcast.CombinedText
import ui.broadcast.EditorContract
import java.awt.Dimension

@Composable
fun AddItemDialog(item: Item, onMessage: (EditorContract.Event) -> Unit) {
    Dialog(
        resizable = true,
        state = rememberDialogState(position = WindowPosition(Alignment.Center)),
        title = ResString.editItemDialogTitle,
        onCloseRequest = {
            onMessage(EditorContract.Event.CancelItem)
        }) {
        window.minimumSize = Dimension(500, 250)
        Column(Modifier.fillMaxSize()) {
            ParamItemView(item = item, onMessage = onMessage)
        }
    }
}

@Composable
@Preview
fun PreviewAddItemDialog() {
    ParamItemView(item = Item(0, "key", Item.DataType.STRING, "")) {

    }
}

@Composable
fun ParamItemView(
    modifier: Modifier = Modifier,
    item: Item,
    onMessage: (EditorContract.Event) -> Unit
) {
    var dataType by remember { mutableStateOf(item.type) }
    var data by remember { mutableStateOf(item.data) }
    var key by remember { mutableStateOf(item.key) }
    var errors by remember { mutableStateOf(listOf(false, false)) }
    Column(modifier.fillMaxWidth().padding(4.dp)) {
        Row(modifier = modifier.fillMaxWidth()) {
            CombinedText(modifier = Modifier.weight(0.3f), label = ResString.keyLabel, text = key, onTextReady = {
                key = it
                errors = listOf(key.isEmpty(), data.toString().isEmpty())
            }, errors[0])
            Spacer(Modifier.width(6.dp))
            if (dataType == Item.DataType.BOOLEAN) {
                BaseDropdown(modifier = Modifier.align(Alignment.CenterVertically), type = false, listOf(true, false), {
                    data = it.toString()
                }, title = {
                    Text(text = it.toString())
                }) {
                    Text(text = if (it) ResString.trueString else ResString.falseString)
                }
            } else {
                CombinedText(
                    modifier = Modifier.align(Alignment.CenterVertically).weight(0.3f),
                    label = ResString.valueLabel,
                    text = data.toString(),
                    onTextReady = {
                        data = it
                        errors = listOf(key.isEmpty(), data.toString().isEmpty())
                    }, errors[1]
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        MultiToggleButton(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            dataType,
            Item.DataType.values().toList()
        ) {
            data = ""
            dataType = it
        }
        Spacer(Modifier.weight(1f))
        Row(Modifier.padding(6.dp).align(Alignment.End)) {
            Button(onClick = {
                onMessage(EditorContract.Event.CancelItem)
            }) {
                Text(ResString.cancel)
            }
            Spacer(Modifier.width(6.dp))
            Button(onClick = {
                errors = listOf(key.isEmpty(), data.toString().isEmpty())
                if (errors.any { false }) {
                    onMessage(EditorContract.Event.SaveItem(Item(item.id, key, dataType, data)))
                }
            }) {
                Text(ResString.save)
            }
        }
    }
}