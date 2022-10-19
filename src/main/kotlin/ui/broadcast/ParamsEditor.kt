package ui.broadcast

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import repo.Item
import repo.Item.DataType.BOOLEAN
import repo.Item.DataType.values
import resources.ResString
import ui.base.BaseDropdown
import ui.dialogs.AddItemDialog
import java.util.*

@Composable
fun ParamsEditorScreen(modifier: Modifier = Modifier, viewModel: EditorViewModel) {
    val state by remember { viewModel.viewState }
    ParamsEditor(
        modifier,
        state = state,
        onEventSent = { event -> viewModel.setEvent(event) })
}

@Composable
private fun ParamsEditor(
    modifier: Modifier = Modifier,
    state: EditorContract.State,
    onEventSent: (event: EditorContract.Event) -> Unit
) {
    Column(modifier.fillMaxSize()) {
        val buttonModifier = Modifier.padding(4.dp)
        Row {
            Button(modifier = buttonModifier, onClick = {
                onEventSent(EditorContract.Event.Run)
            }) {
                Text(text = ResString.run)
            }
            Button(
                modifier = buttonModifier, onClick = {
                    onEventSent(EditorContract.Event.Clear)
                }
            ) {
                Text(text = ResString.clear)
            }
        }
        DefaultParams(Modifier.padding(6.dp), state.packageName, state.intentName) { message ->
            onEventSent(message)
        }

        if (state.dialogState is EditorContract.DialogState.Open) {
            AddItemDialog(state.dialogState.item,
                onCanceled = {
                    onEventSent(EditorContract.Event.CancelItem)
                }, onSave = {
                    onEventSent(EditorContract.Event.SaveItem(it))
                }
            )
        }
        Row(modifier = Modifier.wrapContentSize()) {
            Button(modifier = Modifier.padding(4.dp), onClick = {
                onEventSent(EditorContract.Event.CreateItem)
            }) {
                Text(ResString.addParam)
            }
        }
        Row(modifier = modifier.wrapContentSize().padding(4.dp)) {
            Text(modifier = Modifier.weight(0.4f), text = "Key")
            Spacer(Modifier.width(6.dp))
            Text(modifier = Modifier.align(Alignment.CenterVertically).weight(0.4f), text = "Value")
            Spacer(Modifier.width(6.dp))
            Text(modifier = Modifier.align(Alignment.CenterVertically).weight(0.2f), text = "Actions")
        }
        Divider()
        LazyColumn {
            items(state.itemsList, key = { item -> item.id }) { item ->
                ParamItemView(item = item, onMessage = {
                    onEventSent(it)
                })
                Divider()
            }
        }
    }
}

@Composable
fun DefaultParams(
    modifier: Modifier = Modifier,
    packageName: String,
    intent: String,
    onChanged: (EditorContract.Event) -> Unit
) {
    Row(modifier = modifier.wrapContentSize()) {
        CombinedText(label = ResString.intentLabel, text = intent, onTextReady = {
            onChanged(EditorContract.Event.IntentUpdate(it))
        })
        Spacer(Modifier.width(6.dp))
        CombinedText(label = ResString.packageLabel, text = packageName, onTextReady = {
            onChanged(EditorContract.Event.PackageUpdate(it))
        })
    }
}

@Composable
fun ParamItemView(
    modifier: Modifier = Modifier,
    item: Item,
    onMessage: (EditorContract.Event) -> Unit
) {
    Row(modifier = modifier.wrapContentSize().padding(4.dp)) {
        Text(modifier = Modifier.align(Alignment.CenterVertically).weight(0.3f), text = item.key)
        Spacer(Modifier.width(6.dp))
        Text(modifier = Modifier.align(Alignment.CenterVertically).weight(0.3f), text = item.data.toString())
        Spacer(Modifier.width(6.dp))
        IconButton(
            modifier = Modifier.padding(4.dp).align(Alignment.CenterVertically),
            onClick = { onMessage(EditorContract.Event.EditItem(item)) }) {
            Icon(Icons.Default.Edit, contentDescription = "Edit item")
        }
        IconButton(
            modifier = Modifier.padding(4.dp).align(Alignment.CenterVertically),
            onClick = { onMessage(EditorContract.Event.Remove(item.id)) }) {
            Icon(Icons.Default.Delete, contentDescription = "Delete item")
        }
    }
}

@Composable
fun CombinedText(
    modifier: Modifier = Modifier,
    label: String,
    text: String,
    onTextReady: (text: String) -> Unit,
) {
    var isInError by remember { mutableStateOf(false) }
    OutlinedTextField(
        modifier = modifier,
        isError = isInError,
        value = text,
        label = { Text(text = if (!isInError) label else ResString.emptyError) },
        onValueChange = { newText ->
            if (newText != text) {
                onTextReady(newText.trim())
            }
            isInError = newText.isEmpty()
        }
    )
}

@Composable
fun CombinedText(
    modifier: Modifier = Modifier,
    label: String,
    text: String,
    onTextReady: (text: String) -> Unit,
    isEmptyState: Boolean
) {
    OutlinedTextField(
        modifier = modifier,
        isError = isEmptyState,
        value = text,
        label = { Text(text = if (!isEmptyState) label else ResString.emptyError) },
        onValueChange = { newText ->
            if (newText != text) {
                onTextReady(newText.trim())
            }
        }
    )
}
