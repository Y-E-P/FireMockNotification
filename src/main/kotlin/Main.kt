import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import resources.ResString
import resources.ResString.APP_NAME
import ui.base.AboutDialog
import ui.base.BaseDropdown
import ui.base.LAUNCH_LISTEN_FOR_EFFECTS
import ui.broadcast.EditorContract
import ui.broadcast.EditorViewModel
import ui.broadcast.ParamsEditorScreen
import ui.console.ConsoleOutput
import utils.openFile
import utils.saveFile
import java.awt.Dimension
import java.util.*


fun main() = application {
    val openDialog = remember { mutableStateOf(false) }
    val viewModelApp by lazy { AppViewModel() }
    val editorViewModel by lazy { EditorViewModel() }

    Window(title = APP_NAME,
        icon = loadSvgPainter(
            this::class.java.classLoader.getResourceAsStream("notification_8.svg"),
            LocalDensity.current
        ),
        onCloseRequest = {
            viewModelApp.shutDown()
            exitApplication()
        }) {

        LaunchedEffect(LAUNCH_LISTEN_FOR_EFFECTS) {
            viewModelApp.startDeviceSearchService()
            editorViewModel.effect.onEach { itEffect ->
                when (itEffect) {
                    is EditorContract.Effect.CreateFile ->
                        window.saveFile { file ->
                            editorViewModel.setEvent(EditorContract.Event.SaveAs(file))
                        }

                    is EditorContract.Effect.RunCommand -> viewModelApp.run(itEffect.cmd)
                }
            }.collect()
        }
        window.minimumSize = Dimension(1024, 768)
        ToolboxMenu {
            when (it) {
                MenuItem.ABOUT -> openDialog.value = true
                MenuItem.SAVE -> editorViewModel.setEvent(EditorContract.Event.Save)
                MenuItem.SAVE_AS -> {
                    window.saveFile { file ->
                        editorViewModel.setEvent(EditorContract.Event.SaveAs(file))
                    }
                }

                MenuItem.OPEN -> {
                    window.openFile()?.let { file -> editorViewModel.setEvent(EditorContract.Event.Load(file)) }
                }
            }
        }
        App(viewModelApp, editorViewModel)
        if (openDialog.value) {
            AboutDialog {
                openDialog.value = false
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
fun App(appViewModel: AppViewModel, editorViewModel: EditorViewModel) {
    val state by remember { appViewModel.viewState }
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
                ParamsEditorScreen(viewModel = editorViewModel)
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




