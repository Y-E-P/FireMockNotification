import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import repo.ModelParser
import service.DeviceSearchService
import utils.prepareCommand
import java.io.BufferedReader
import java.io.File

class AppViewModel {
    private val runtime by lazy { Runtime.getRuntime() }
    val dispatcher = CoroutineScope(Dispatchers.Swing)

    private val dataListeners: ArrayList<(ParamsModel) -> Unit> = ArrayList()
    private var currentFile: File? = null
    private val consoleModel = ConsoleModel()
    var onConsoleOutput: (List<ConsoleItem>) -> Unit = {}
        set(value) {
            field = value
            consoleModel.onConsoleDataChanged = field
        }
    var onConsoleOpen: (Boolean) -> Unit = {}
    var onSaveAsCallback: (String) -> Unit = {}
    private var idCounter: Int = 2

    private val _state = MutableStateFlow<AppState>(AppState.Initial)
    val state: StateFlow<AppState> = _state

    sealed class AppState {
        object Initial : AppState()
        data class DevicesFound(val devicesList: List<Device>) : AppState()
        data class Console(val isOpen: Boolean, val consoleData: List<ConsoleItem>) : AppState()
    }

    sealed class Message {
        data class PackageUpdate(val packageName: String) : Message()
        data class IntentUpdate(val intentName: String) : Message()
        data class KeyUpdate(val index: Int, val key: String) : Message()
        data class ValueUpdate(val index: Int, val value: String) : Message()
        data class TypeUpdate(val index: Int, val type: DataType) : Message()
        data class Remove(val index: Int) : Message()
        object AddItem : Message()
    }

    var model: ParamsModel = ParamsModel()
    private var isConsoleOpened: Boolean = false

    val deviceService = DeviceSearchService
    var onDevicesListChanges: (List<Device>) -> Unit = {}
        set(value) {
            field = value
            deviceService.onDevicesListChanged = field
        }


    fun addOnDataChangeListener(onDataChanged: (ParamsModel) -> Unit) {
        dataListeners.add(onDataChanged)
    }

    fun removeOnDataChangeListener(onDataChanged: (ParamsModel) -> Unit) {
        dataListeners.remove(onDataChanged)
    }

    fun run() {
        startCommand(model.prepareCommand())
    }

    fun startDeviceSearchService() {
        deviceService.startMonitoringDevices()
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

    fun clear() {
        this.model.clear()
        notifyChanges()
    }

    fun openConsole() {
        onConsoleOpen(isConsoleOpened)
        isConsoleOpened = !isConsoleOpened
    }

    private fun emitEvent(state: AppState) {
        dispatcher.launch {
            _state.emit(state)
        }
    }

    fun sendMessage(msg: Message) {
        when (msg) {
            is Message.IntentUpdate -> model.intent = msg.intentName
            is Message.PackageUpdate -> model.packageName = msg.packageName
            is Message.KeyUpdate -> model.updateKey(msg.index, msg.key)
            is Message.Remove -> model.removeItem(msg.index)
            is Message.TypeUpdate -> model.updateType(msg.index, msg.type)
            is Message.ValueUpdate -> model.updateValue(msg.index, msg.value)
            is Message.AddItem -> model.addItem(idCounter++)
        }
        notifyChanges()
    }

    fun loadScheme(file: File) {
        CoroutineScope(Dispatchers.Swing).launch {
            processFile(file).collect {
                model = it.data
                idCounter = model.params.last().id + 1
                notifyChanges()
                currentFile = file
            }
        }
    }

    private fun processFile(file: File): Flow<Reaction.Success<ParamsModel>> = flow {
        emit(Reaction.Success(ModelParser().fromJson(file.readText())))
    }

    fun saveScheme() {
        currentFile?.writeText(ModelParser().toJson(model))
    }

    fun saveAsScheme() {
        onSaveAsCallback(ModelParser().toJson(model))
    }

    private fun notifyChanges() {
        for (listener in dataListeners) {
            listener(model)
        }
    }

    private fun updateData(index: Int, id: Int, key: String, value: String) {
        model.updateParam(index, Item.ItemString(id, key, value))
    }
}