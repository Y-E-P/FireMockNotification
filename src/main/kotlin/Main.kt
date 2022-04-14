import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
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
import ui.broadcast.EditorViewModel
import ui.broadcast.ParamsEditorScreen
import ui.console.ConsoleOutput
import utils.openFile
import utils.saveFile
import java.awt.Dimension
import java.util.*


fun main() = application {
    var isOpen by remember { mutableStateOf(true) }
    val openDialog = remember { mutableStateOf(false) }
    val viewModelApp by lazy { AppViewModel() }
    //start it once
    LaunchedEffect(true) {
        viewModelApp.startDeviceSearchService()
    }

    if (isOpen) {
        Window(title = APP_NAME,
            icon = loadSvgPainter(
                this::class.java.classLoader.getResourceAsStream("notification_8.svg"),
                LocalDensity.current
            ),
            onCloseRequest = {
                viewModelApp.shutDown()
                isOpen = false
            }) {
            window.minimumSize = Dimension(1024, 768)
            viewModelApp.onSaveAsCallback = {
                window.saveFile(it)
            }
            FireMenu {
                when (it) {
                    MenuItem.ABOUT -> openDialog.value = true
                    MenuItem.SAVE -> viewModelApp.setEvent(AppContract.Event.Save(""))
                    MenuItem.SAVE_AS -> viewModelApp.setEvent(AppContract.Event.SaveAs(""))
                    MenuItem.OPEN -> {
                        window.openFile()?.let { file -> viewModelApp.loadScheme(file) }
                    }
                }
            }
            App(viewModelApp)
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
    var splitterState = SplitterState()
}

class SplitterState {
    var isResizing by mutableStateOf(false)
}

@Composable
@Preview
fun App(appViewModel: AppViewModel) {
    val state by remember { appViewModel.viewState }
    val editorViewModel by lazy { EditorViewModel(state.model) }
    val panelState = remember { PanelsState() }
    val animatedSize = if (panelState.splitterState.isResizing) {
        if (state.consoleOpened) panelState.expandedSize else panelState.collapsedSize
    } else {
        animateDpAsState(
            if (state.consoleOpened) panelState.expandedSize else panelState.collapsedSize,
            SpringSpec(stiffness = Spring.StiffnessLow)
        ).value
    }
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            ContentEdit(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopStart),
                appViewModel
            ) {
                ParamsEditorScreen(controller = appViewModel, viewModel = editorViewModel)
            }
            ConsoleOutput(
                modifier = Modifier.align(Alignment.BottomCenter).height(animatedSize),
                splitterState = panelState.splitterState,
                itemsList = state.consoleOut,
                onOpenConsole = {
                    appViewModel.setEvent(AppContract.Event.CloseConsole)
                },
                onResize = { delta ->
                    panelState.expandedSize =
                        (panelState.expandedSize - delta).coerceAtLeast(panelState.expandedSizeMin)
                })
        }
    }
}

@Composable
fun ContentEdit(modifier: Modifier, viewModel: AppViewModel, content: @Composable () -> Unit) {
    val state by remember { viewModel.viewState }

    Column(modifier) {
        val buttonModifier = Modifier.padding(4.dp)
        Row(modifier = Modifier.wrapContentSize()) {
            Button(
                modifier = buttonModifier, onClick = {
                    if (state.consoleOpened) {
                        viewModel.setEvent(AppContract.Event.CloseConsole)
                    } else {
                        viewModel.setEvent(AppContract.Event.OpenConsole)
                    }
                }) {
                Text(text = ResString.consoleOutput)
            }
            Spacer(Modifier.weight(1f))
            BaseDropdown(
                modifier = buttonModifier,
                type = state.selected,
                items = state.devicesList, title = {
                    Text(text = it.name)
                }, onItemSelected = {
                    viewModel.setEvent(AppContract.Event.SelectDevice(it))
                }) {
                Text(it.name.uppercase(Locale.getDefault()), fontSize = 12.sp)
            }
        }
        Divider(modifier = Modifier.fillMaxWidth().wrapContentHeight(), Color.Black)
        content()
    }
}




