import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import resources.ResString
import resources.ResString.APP_NAME
import ui.AboutDialog
import utils.cursorForHorizontalResize
import utils.openFile
import utils.saveFile
import java.awt.Dimension
import java.util.*


@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
fun main() = application {
    var isOpen by remember { mutableStateOf(true) }
    val openDialog = remember { mutableStateOf(false) }
    val controller by lazy { FireController() }

    if (isOpen) {
        Window(title = APP_NAME,
            icon = loadSvgPainter(
                this::class.java.classLoader.getResourceAsStream("notification_8.svg"),
                LocalDensity.current
            ),
            onCloseRequest = {
                isOpen = false
            }) {
            window.minimumSize = Dimension(1024, 768)
            controller.onSaveAsCallback = {
                window.saveFile(it)
            }
            FireMenu {
                when (it) {
                    MenuItem.ABOUT -> openDialog.value = true
                    MenuItem.SAVE -> controller.saveScheme()
                    MenuItem.SAVE_AS -> controller.saveAsScheme()
                    MenuItem.OPEN -> {
                        window.openFile()?.let { file -> controller.loadScheme(file) }
                    }
                }
            }
            App(controller)
            if (openDialog.value) {
                AboutDialog {
                    openDialog.value = false
                }
            }
        }
    }
}

private class PanelsState {
    val collapsedSize = 0.dp
    var expandedSize by mutableStateOf(300.dp)
    val expandedSizeMin = 100.dp
    var isExpanded by mutableStateOf(true)
    var splitterState = SplitterState()
}

class SplitterState {
    var isResizing by mutableStateOf(false)
}

@Composable
@Preview
fun App(controller: FireController) {
    val panelState = remember { PanelsState() }
    val animatedSize = if (panelState.splitterState.isResizing) {
        if (panelState.isExpanded) panelState.expandedSize else panelState.collapsedSize
    } else {
        animateDpAsState(
            if (panelState.isExpanded) panelState.expandedSize else panelState.collapsedSize,
            SpringSpec(stiffness = Spring.StiffnessLow)
        ).value
    }
    controller.onConsoleOpen = {
        panelState.isExpanded = it
    }
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            ContentEdit(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopStart),
                controller
            )
            ConsoleOutput(
                modifier = Modifier.align(Alignment.BottomCenter).height(animatedSize),
                controller,
                splitterState = panelState.splitterState
            ) { delta ->
                panelState.expandedSize = (panelState.expandedSize - delta).coerceAtLeast(panelState.expandedSizeMin)
            }
        }
    }
}

@Composable
fun ContentEdit(modifier: Modifier, controller: FireController) {
    var isEnabled by remember { mutableStateOf(false) }
    controller.addOnDataChangeListener {
        isEnabled = it.params.isNotEmpty()
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
    var itemsList by mutableStateOf(controller.model.params.toMutableStateList())
    var packageName by mutableStateOf(controller.model.packageName)
    var intent by mutableStateOf(controller.model.intent)
    controller.addOnDataChangeListener {
        itemsList = it.params.toMutableStateList()
        intent = it.packageName
        packageName = it.intent
    }
    Column(modifier.fillMaxSize()) {
        DefaultParams(Modifier.padding(6.dp), packageName, intent) { packageName, intent ->
            controller.editAppPackage(packageName)
            controller.editIntent(intent)
        }
        Row(modifier = Modifier.wrapContentSize()) {
            Button(modifier = Modifier.padding(4.dp), onClick = {
                controller.addNewItem()
            }) {
                Text(ResString.addParam)
            }
        }
        LazyColumn {
            itemsIndexed(itemsList, key = { _, item -> item.id }) { index, item ->
                ParamItemView(index = index, item = item, onChanged = { indexChanged, id, key, value ->
                    controller.changeData(indexChanged, id, key, value)
                }, onRemoved = {
                    controller.removeItem(it)
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
    onChanged: (packageName: String, intent: String) -> Unit
) {
    Row(modifier = modifier.wrapContentSize()) {
        CombinedText(label = ResString.intentLabel, text = packageName, onTextReady = {
            onChanged(it, intent)
        })
        Spacer(Modifier.width(6.dp))
        CombinedText(label = ResString.packageLabel, text = intent, onTextReady = {
            onChanged(packageName, it)
        })
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
        modifier = modifier.wrapContentWidth(),
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
fun ParamItemView(
    modifier: Modifier = Modifier,
    index: Int,
    item: Item,
    onChanged: (index: Int, id: Int, key: String, value: String) -> Unit,
    onRemoved: (index: Int) -> Unit,
) {
    val dataType = remember { mutableStateOf(item.type()) }
    Row(modifier = modifier.wrapContentSize().padding(4.dp)) {
        CombinedText(label = ResString.keyLabel, text = item.key, onTextReady = {
            onChanged(index, item.id, it, (item as Item.ItemString).str)
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
                label = ResString.valueLabel,
                text = item.dataAsString(),
                onTextReady = {
                    onChanged(index, item.id, item.key, it)
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
fun ConsoleOutput(
    modifier: Modifier = Modifier,
    controller: FireController,
    splitterState: SplitterState,
    onResize: (delta: Dp) -> Unit
) {
    val density = LocalDensity.current
    var itemsList by mutableStateOf(controller.consoleData.toMutableStateList())
    controller.onConsoleOutput = {
        itemsList = it.toMutableStateList()
    }
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
                        controller.openConsole()
                    }) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
        }
        LazyColumn(reverseLayout = true, modifier = Modifier.background(Color.Black).fillMaxSize()) {
            items(itemsList) {
                val color = when (it) {
                    is ConsoleItem.Error -> Color.Red
                    is ConsoleItem.Output -> Color.White
                    is ConsoleItem.Input -> Color.Green
                }
                SelectionContainer {
                    Text(it.data, color = color, modifier = Modifier.fillMaxWidth())
                }
            }
        }
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



