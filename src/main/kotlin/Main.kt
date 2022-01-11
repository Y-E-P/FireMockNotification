import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import resources.ResString
import utils.openFile
import java.util.*


@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    var isOpen by remember { mutableStateOf(true) }
    val controller by lazy { FireController() }
    if (isOpen) {
        Window(onCloseRequest = {
            isOpen = false
        }) {
            MenuBar {
                Menu(ResString.file, mnemonic = 'F') {
                    Item(
                        ResString.saveScheme,
                        enabled = false,
                        onClick = { controller.saveScheme() },
                        shortcut = KeyShortcut(Key.C, ctrl = true)
                    )
                    Item(
                        ResString.loadScheme,
                        enabled = false,
                        onClick = {
                            window.openFile()?.let {
                                CoroutineScope(Dispatchers.Swing).launch {
                                    controller.loadScheme(it)
                                }
                            }
                        },
                        shortcut = KeyShortcut(Key.V, ctrl = true)
                    )
                }
                Menu(ResString.about, mnemonic = 'A') {

                }
            }
            App(controller)
        }
    }
}

@Composable
@Preview
fun App(controller: FireController) {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            ContentEdit(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopStart),
                controller
            )
            ConsoleOutput(modifier = Modifier.align(Alignment.BottomCenter), controller)
        }
    }
}

@Composable
fun ContentEdit(modifier: Modifier, controller: FireController) {
    var isEnabled by remember { mutableStateOf(false) }
    controller.addOnDataChangeListener {
        isEnabled = it.isNotEmpty()
    }
    Column(modifier) {
        val buttonModifier = Modifier.padding(4.dp)
        Row(modifier = Modifier.wrapContentSize()) {
            Button(modifier = buttonModifier, enabled = isEnabled, onClick = { controller.run() }) {
                Text(text = ResString.run)
            }
            Button(
                modifier = buttonModifier, enabled = isEnabled, onClick = { controller.clear() }) {
                Text(text = ResString.clear)
            }
            Button(
                modifier = buttonModifier, onClick = { controller.openConsole() }) {
                Text(text = ResString.consoleOutput)
            }
        }
        Divider(modifier = Modifier.fillMaxWidth().wrapContentHeight(), Color.Black)
        ParamsEditor(controller = controller)
    }
}

@Composable
fun ParamsEditor(modifier: Modifier = Modifier, controller: FireController) {
    var itemsList by mutableStateOf(controller.params.toMutableStateList())
    controller.addOnDataChangeListener {
        itemsList = it.toMutableStateList()
    }
    Column(modifier.fillMaxSize()) {
        DefaultParams(Modifier.padding(6.dp), controller)
        Row(modifier = Modifier.wrapContentSize()) {
            Button(modifier = Modifier.padding(4.dp), onClick = {
                controller.addNewItem()
            }) {
                Text(ResString.addParam)
            }
        }
        LazyColumn {
            itemsIndexed(itemsList, key = { _, item -> item.id }) { index, item ->
                ParamItemView(index = index, item = item, onChanged = { indexChanged, key, value ->
                    controller.changeData(indexChanged, key, value)
                }, onRemoved = {
                    controller.removeItem(it)
                })
            }

        }
    }
}

@Composable
fun DefaultParams(modifier: Modifier = Modifier, controller: FireController) {
    Row(modifier = modifier.wrapContentSize()) {
        CombinedText(label = ResString.intentLabel, onTextReady = {
            controller.editIntent(it)
        })
        Spacer(Modifier.width(6.dp))
        CombinedText(label = ResString.packageLabel, onTextReady = {
            controller.editAppPackage(it)
        })
    }
}

@Composable
fun CombinedText(
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    label: String,
    onTextReady: (text: String) -> Unit
) {
    var changedText by remember { mutableStateOf("") }
    var isInError by remember { mutableStateOf(false) }
    OutlinedTextField(
        modifier = modifier.wrapContentWidth().onFocusChanged { focusState ->
            when {
                !focusState.hasFocus -> if (changedText.isEmpty()) {
                    //isInError = true
                } else {
                    //isInError = false
                    onTextReady(changedText.trim())
                }
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        isError = isInError,
        value = changedText,
        label = { Text(text = if (!isInError) label else ResString.emptyError) },
        onValueChange = { newText ->
            changedText = newText.trimStart { it == '0' }
            isInError = changedText.isEmpty()
        }
    )
}

@Composable
fun ParamItemView(
    modifier: Modifier = Modifier,
    index: Int,
    item: Item,
    onChanged: (index: Int, key: String, value: String) -> Unit,
    onRemoved: (index: Int) -> Unit,
) {
    var dataType = remember { mutableStateOf(item.type()) }
    Row(modifier = modifier.wrapContentSize()) {
        CombinedText(label = ResString.keyLabel, onTextReady = {
            onChanged(index, it, (item as Item.ItemString).str)
        })
        Spacer(Modifier.width(6.dp))
        if (dataType.value == DataType.BOOLEAN) {
            BooleanDropdown(modifier = Modifier.align(Alignment.CenterVertically), type = false) {

            }
        } else {
            val keyboardType = when (dataType.value) {
                DataType.INTEGER,
                DataType.FLOAT,
                DataType.LONG -> KeyboardType.Number
                else -> KeyboardType.Text
            }
            CombinedText(
                modifier = Modifier.align(Alignment.CenterVertically),
                keyboardType = keyboardType,
                label = ResString.valueLabel,
                onTextReady = {
                    onChanged(index, item.key, it)
                })
        }
        Spacer(Modifier.width(6.dp))

        TypeDropdown(
            modifier = Modifier.padding(4.dp).align(Alignment.CenterVertically),
            type = item.type(),
            items = DataType.values()
        ) {
            dataType.value = it
        }
        IconButton(
            modifier = Modifier.padding(4.dp).align(Alignment.CenterVertically), onClick = { onRemoved(index) }) {
            Icon(Icons.Default.Delete, contentDescription = null)
        }
    }
}


@Composable
fun ConsoleOutput(modifier: Modifier = Modifier, controller: FireController) {
    var isOpen by remember { mutableStateOf(false) }
    var itemsList by mutableStateOf(controller.consoleData.toMutableStateList())
    controller.onConsoleOpen = {
        isOpen = it
    }
    controller.onConsoleOutput = {
        itemsList = it.toMutableStateList()
    }
    Column(modifier.height(if (isOpen) 500.dp else 0.dp).fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().wrapContentHeight().background(Color.LightGray)) {
            Text(ResString.console, modifier = Modifier.padding(4.dp).align(Alignment.CenterVertically))
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier.padding(4.dp).align(Alignment.CenterVertically)
                    .clickable(role = Role.Button) {
                        controller.openConsole()
                    }) {
                Icon(if (isOpen) Icons.Default.Close else Icons.Default.ArrowDropDown, contentDescription = null)
            }
        }
        LazyColumn(reverseLayout = true) {
            items(itemsList) {
                Text(it.data, color = Color.White)
            }
        }
        Box(modifier = Modifier.fillMaxSize().background(Color.Black))
    }
}

@Composable
fun TypeDropdown(
    modifier: Modifier = Modifier,
    type: DataType,
    items: Array<DataType>,
    onTypeChanged: (DataType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var data by remember { mutableStateOf(type) }
    Box(modifier = modifier) {
        Button(onClick = { expanded = true }) {
            Text(text = data.name.uppercase(Locale.getDefault()))
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                contentDescription = ResString.changeType
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            for (item in items) {
                DropdownMenuItem(onClick = {
                    data = item
                    expanded = false
                    onTypeChanged(item)
                }) {
                    Text(item.name.uppercase(Locale.getDefault()))
                }
            }
        }
    }
}

@Composable
fun BooleanDropdown(
    modifier: Modifier = Modifier,
    type: Boolean,
    onTypeChanged: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var data by remember { mutableStateOf(type) }
    Box(modifier = modifier) {
        Button(onClick = { expanded = true }) {
            Text(text = data.toString())
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                contentDescription = ResString.changeType
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(onClick = {
                data = true
                expanded = false
                onTypeChanged(true)
            }) {
                Text(ResString.trueString)
            }

            DropdownMenuItem(onClick = {
                data = false
                expanded = false
                onTypeChanged(false)
            }) {
                Text(ResString.falseString)
            }
        }
    }
}



