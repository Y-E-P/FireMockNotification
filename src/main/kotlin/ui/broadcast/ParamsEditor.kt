package ui.broadcast

import AppViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import repo.DataType
import repo.Item
import repo.dataAsString
import repo.type
import resources.ResString
import ui.base.BaseDropdown
import ui.base.LAUNCH_LISTEN_FOR_EFFECTS
import java.util.*

@Composable
fun ParamsEditorScreen(modifier: Modifier = Modifier, controller: AppViewModel, viewModel: EditorViewModel) {
    val state by remember { viewModel.viewState }
    ParamsEditor(
        modifier,
        state = state,
        effectFlow = viewModel.effect,
        onEventSent = { event -> viewModel.setEvent(event) },
        onOutsideCommand = {
            if (it is EditorContract.Effect.RunCommand) {
                controller.run(it.cmd)
            }
        }
    )
}

@Composable
private fun ParamsEditor(
    modifier: Modifier = Modifier,
    state: EditorContract.State,
    effectFlow: Flow<EditorContract.Effect>?,
    onEventSent: (event: EditorContract.Event) -> Unit,
    onOutsideCommand: (event: EditorContract.Effect) -> Unit
) {
    LaunchedEffect(LAUNCH_LISTEN_FOR_EFFECTS) {
        effectFlow?.onEach { itEffect ->
            when (itEffect) {
                is EditorContract.Effect.RunCommand -> onOutsideCommand(itEffect)
            }
        }?.collect()
    }
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
        Row(modifier = Modifier.wrapContentSize()) {
            Button(modifier = Modifier.padding(4.dp), onClick = {
                onEventSent(EditorContract.Event.AddItem)
            }) {
                Text(ResString.addParam)
            }
        }
        LazyColumn {
            itemsIndexed(state.itemsList, key = { _, item -> item.id }) { index, item ->
                ParamItemView(index = index, item = item, onMessage = {
                    onEventSent(it)
                })
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
    index: Int,
    item: Item,
    onMessage: (EditorContract.Event) -> Unit
) {
    val dataType = remember { mutableStateOf(item.type()) }
    Row(modifier = modifier.wrapContentSize().padding(4.dp)) {
        CombinedText(modifier = Modifier.weight(0.3f), label = ResString.keyLabel, text = item.key, onTextReady = {
            onMessage(EditorContract.Event.KeyUpdate(index, it))
        })
        Spacer(Modifier.width(6.dp))
        if (dataType.value == DataType.BOOLEAN) {
            BaseDropdown(modifier = Modifier.align(Alignment.CenterVertically), type = false, listOf(true, false), {
                onMessage(EditorContract.Event.ValueUpdate(index, it.toString()))
            }, title = {
                Text(text = it.toString())
            }) {
                Text(text = if (it) ResString.trueString else ResString.falseString)
            }
        } else {
            CombinedText(
                modifier = Modifier.align(Alignment.CenterVertically).weight(0.3f),
                label = ResString.valueLabel,
                text = item.dataAsString(),
                onTextReady = {
                    onMessage(EditorContract.Event.ValueUpdate(index, it))
                })
        }
        Spacer(Modifier.width(6.dp))

        BaseDropdown(
            modifier = Modifier.padding(4.dp).align(Alignment.CenterVertically),
            type = item.type(),
            DataType.values().toList(),
            title = {
                Text(text = it.name.uppercase(Locale.getDefault()))
            },
            onItemSelected = {
                onMessage(EditorContract.Event.TypeUpdate(index, it))
                dataType.value = it
            }) {
            Text(text = it.name.uppercase(Locale.getDefault()))
        }
        IconButton(
            modifier = Modifier.padding(4.dp).align(Alignment.CenterVertically),
            onClick = { onMessage(EditorContract.Event.Remove(index)) }) {
            Icon(Icons.Default.Delete, contentDescription = null)
        }
    }
}

@Composable
fun CombinedText(
    modifier: Modifier = Modifier,
    label: String,
    text: String,
    onTextReady: (text: String) -> Unit
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
