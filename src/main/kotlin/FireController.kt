import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import utils.prepareCommand
import java.io.BufferedReader
import java.io.File

class FireController {
    private val runtime by lazy { Runtime.getRuntime() }

    private val dataListeners: ArrayList<(ParamsModel) -> Unit> = ArrayList()
    var onConsoleOutput: (List<ConsoleItem>) -> Unit = {}
    var onConsoleOpen: (Boolean) -> Unit = {}
    private var idCounter: Int = 2

    val model: ParamsModel = ParamsModel()
    val consoleData: MutableList<ConsoleItem> = mutableListOf()
    private var isConsoleOpened: Boolean = false

    fun addOnDataChangeListener(onDataChanged: (ParamsModel) -> Unit) {
        dataListeners.add(onDataChanged)
    }

    fun removeOnDataChangeListener(onDataChanged: (ParamsModel) -> Unit) {
        dataListeners.remove(onDataChanged)
    }

    fun run() {
        startCommand(model.prepareCommand())
    }

    private fun startCommand(command: String) {

        try {
            val process = runtime.exec(command)
            consoleData.add(ConsoleItem.Input(command))
            val stdoutString: String =
                process.inputStream.bufferedReader().use(BufferedReader::readText)
            val stderrString: String =
                process.errorStream.bufferedReader().use(BufferedReader::readText)
            process.waitFor()
            consoleData.add(ConsoleItem.Output(stdoutString))
            consoleData.add(ConsoleItem.Error(stderrString))
        } catch (e: InterruptedException) {
            consoleData.add(ConsoleItem.Error(e.message ?: "Unknown error"))
        } catch (e: Exception) {
            consoleData.add(ConsoleItem.Error(e.message ?: "Unknown error"))
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

    fun addNewItem() {
        model.addItem(idCounter++)
        notifyChanges()
    }

    fun changeData(index: Int, id: Int, key: String, value: String) {
        updateData(index, id, key, value)
        notifyChanges()
    }

    fun removeItem(index: Int) {
        model.removeItem(index).run {
            notifyChanges()
        }
    }

    fun editIntent(intent: String) {
        model.intent = intent
        notifyChanges()
    }

    fun editAppPackage(packageName: String) {
        model.intent = packageName
        notifyChanges()
    }

    fun loadScheme(file: File) {
        CoroutineScope(Dispatchers.Swing).launch {
            processFile(file).collect {
                // onJsonLoaded(it)
            }
        }
    }

    private fun processFile(file: File): Flow<Reaction.Success<String>> = flow {
        file.readText().let {
            // println("JsonObject : $toString")
        }
    }

    fun saveScheme() {
        //TODO: will be implemented after first launch
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