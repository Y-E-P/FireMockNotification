import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import resources.ResString
import resources.ResString.APP_NAME
import ui.base.AboutDialog
import ui.base.BaseDropdown
import ui.console.ConsoleOutput
import utils.openFile
import utils.saveFile
import java.awt.Dimension
import java.util.*


@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
fun main() = application {
    var isOpen by remember { mutableStateOf(true) }
    val openDialog = remember { mutableStateOf(false) }
    val controller by lazy { AppViewModel() }
    controller.startDeviceSearchService()

    if (isOpen) {
        Window(title = APP_NAME,
            icon = loadSvgPainter(
                this::class.java.classLoader.getResourceAsStream("notification_8.svg"),
                LocalDensity.current
            ),
            onCloseRequest = {
                controller.shutDown()
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
fun App(controller: AppViewModel) {
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
            ) {
                ParamsEditor(controller = controller)
            }
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
fun ContentEdit(modifier: Modifier, controller: AppViewModel, content: @Composable () -> Unit) {
    var isEnabled by remember { mutableStateOf(false) }
    var devicesList by remember { mutableStateOf(emptyList<Device>().toMutableStateList()) }
    var device by remember { mutableStateOf(Device("-1", "none", "Not selected")) }
    controller.addOnDataChangeListener {
        isEnabled = it.params.isNotEmpty()
    }
    controller.onDevicesListChanges = {
        devicesList = it.toMutableStateList()
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
            Spacer(Modifier.weight(1f))
            BaseDropdown(
                modifier = buttonModifier,
                type = device,
                items = devicesList, title = {
                    Text(text = it.name)
                }, onItemSelected = {
                    device = it
                }) {
                Text(it.name.uppercase(Locale.getDefault()), fontSize = 12.sp)
            }
        }
        Divider(modifier = Modifier.fillMaxWidth().wrapContentHeight(), Color.Black)
        content()
    }
}

@Composable
fun ParamsEditor(modifier: Modifier = Modifier, controller: AppViewModel) {
    var itemsList by mutableStateOf(controller.model.params.toMutableStateList())
    var packageName by mutableStateOf(controller.model.packageName)
    var intent by mutableStateOf(controller.model.intent)
    controller.addOnDataChangeListener {
        itemsList = it.params.toMutableStateList()
        intent = it.packageName
        packageName = it.intent
    }
    Column(modifier.fillMaxSize()) {
        DefaultParams(Modifier.padding(6.dp), packageName, intent) { message ->
            controller.sendMessage(message)
        }
        Row(modifier = Modifier.wrapContentSize()) {
            Button(modifier = Modifier.padding(4.dp), onClick = {
                controller.sendMessage(AppViewModel.Message.AddItem)
            }) {
                Text(ResString.addParam)
            }
        }
        LazyColumn {
            itemsIndexed(itemsList, key = { _, item -> item.id }) { index, item ->
                ParamItemView(index = index, item = item, onMessage = {
                    controller.sendMessage(it)
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
    onChanged: (AppViewModel.Message) -> Unit
) {
    Row(modifier = modifier.wrapContentSize()) {
        CombinedText(label = ResString.intentLabel, text = intent, onTextReady = {
            onChanged(AppViewModel.Message.IntentUpdate(it))
        })
        Spacer(Modifier.width(6.dp))
        CombinedText(label = ResString.packageLabel, text = packageName, onTextReady = {
            onChanged(AppViewModel.Message.PackageUpdate(it))
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
fun ParamItemView(
    modifier: Modifier = Modifier,
    index: Int,
    item: Item,
    onMessage: (AppViewModel.Message) -> Unit
) {
    val dataType = remember { mutableStateOf(item.type()) }
    Row(modifier = modifier.wrapContentSize().padding(4.dp)) {
        CombinedText(modifier = Modifier.weight(0.3f), label = ResString.keyLabel, text = item.key, onTextReady = {
            onMessage(AppViewModel.Message.KeyUpdate(index, it))
        })
        Spacer(Modifier.width(6.dp))
        if (dataType.value == DataType.BOOLEAN) {
            BaseDropdown(modifier = Modifier.align(Alignment.CenterVertically), type = false, listOf(true, false), {
                onMessage(AppViewModel.Message.ValueUpdate(index, it.toString()))
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
                    onMessage(AppViewModel.Message.ValueUpdate(index, it))
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
                onMessage(AppViewModel.Message.TypeUpdate(index, it))
                dataType.value = it
            }) {
            Text(text = it.name.uppercase(Locale.getDefault()))
        }
        IconButton(
            modifier = Modifier.padding(4.dp).align(Alignment.CenterVertically),
            onClick = { onMessage(AppViewModel.Message.Remove(index)) }) {
            Icon(Icons.Default.Delete, contentDescription = null)
        }
    }
}




