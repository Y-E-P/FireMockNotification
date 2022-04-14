import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import repo.Device
import repo.ModelParser
import repo.ParamsModel
import repo.Reaction
import service.DeviceSearchService
import ui.base.BaseViewModel
import ui.base.ViewEvent
import ui.base.ViewSideEffect
import ui.base.ViewState
import ui.console.ConsoleItem
import ui.console.ConsoleModel
import java.io.BufferedReader
import java.io.File

class AppViewModel : BaseViewModel<
        AppContract.Event,
        AppContract.AppState,
        AppContract.Effect>(CoroutineScope(Dispatchers.Swing)) {

    private val runtime by lazy { Runtime.getRuntime() }

    private var currentFile: File? = null
    private val consoleModel = ConsoleModel()
    var onSaveAsCallback: (String) -> Unit = {}
    var selectedDevice: Device = Device("-1", "none", "Not selected")
    private val deviceService = DeviceSearchService

    init {
        consoleModel.onConsoleDataChanged = {
            setState { copy(consoleOut = it) }
        }
    }

    override fun setInitialState(): AppContract.AppState =
        AppContract.AppState(
            controlsEnabled = false,
            consoleOpened = false,
            selected = selectedDevice,
            devicesList = emptyList(),
            consoleOut = emptyList(),
            model = ParamsModel()
        )

    override fun handleEvents(event: AppContract.Event) {
        when (event) {
            is AppContract.Event.Load -> TODO()
            is AppContract.Event.Save -> currentFile?.writeText(event.json)
            is AppContract.Event.SaveAs -> TODO()
            is AppContract.Event.OpenConsole -> setState { copy(consoleOpened = true) }
            is AppContract.Event.CloseConsole -> setState { copy(consoleOpened = false) }
        }
    }

    fun run(cmd: String) {
        startCommand(cmd)
    }

    fun startDeviceSearchService() {
        deviceService.startMonitoringDevices()
        deviceService.onDevicesListChanged = {
            setState { copy(devicesList = it) }
        }
    }

    fun shutDown() {
        deviceService.shutDown()
    }

    private fun startCommand(command: String) {
        try {
            val process = runtime.exec(command)
            consoleModel.emitInput(command)
            val stdoutString: String =
                process.inputStream.bufferedReader().use(BufferedReader::readText)
            val stderrString: String =
                process.errorStream.bufferedReader().use(BufferedReader::readText)
            process.waitFor()
            stdoutString.takeIf { it.isNotEmpty() }?.let { consoleModel.emitOutput(it) }
            stderrString.takeIf { it.isNotEmpty() }?.let { consoleModel.emitError(stderrString) }
        } catch (e: InterruptedException) {
            consoleModel.emitError(e.message ?: "Unknown error")
        } catch (e: Exception) {
            consoleModel.emitError(e.message ?: "Unknown error")
        }
    }

    fun loadScheme(file: File) {
        CoroutineScope(Dispatchers.Swing).launch {
            processFile(file).collect {
                setState { copy(model = it.data) }
                currentFile = file
            }
        }
    }

    private fun processFile(file: File): Flow<Reaction.Success<ParamsModel>> = flow {
        emit(Reaction.Success(ModelParser().fromJson(file.readText())))
    }

}

class AppContract {

    sealed class Event : ViewEvent {
        data class SaveAs(val json: String) : Event()
        data class Save(val json: String) : Event()
        data class Load(val file: File) : Event()
        data class SelectDevice(val device: Device) : Event()
        object OpenConsole : Event()
        object CloseConsole : Event()
    }

    data class AppState(
        val controlsEnabled: Boolean,
        val consoleOpened: Boolean,
        val selected: Device,
        val devicesList: List<Device>,
        val consoleOut: List<ConsoleItem>,
        val model: ParamsModel
    ) : ViewState

    sealed class Effect : ViewSideEffect {
        data class RunCommand(val cmd: String) : Effect()
    }
}
