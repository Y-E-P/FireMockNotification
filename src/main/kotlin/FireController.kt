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
import java.util.*

class FireController {
    private val runtime by lazy { Runtime.getRuntime() }

    private val dataListeners: ArrayList<(LinkedList<Item>) -> Unit> = ArrayList()
    var onConsoleOutput: (List<ConsoleItem>) -> Unit = {}
    var onConsoleOpen: (Boolean) -> Unit = {}
    var onDefaultsChanged: (LinkedList<Item.ItemString>) -> Unit = {}
    var onAddNewParam: (index: Int, data: Item) -> Unit = { _, _ -> }
    private var idCounter: Int = 2

    val params: LinkedList<Item> = LinkedList()
    val consoleData: List<ConsoleItem> = emptyList()
    private var isConsoleOpened: Boolean = false

    private val defaultParams: LinkedList<Item.ItemString> = LinkedList<Item.ItemString>().apply {
        add(Item.ItemString(0, "intent", ""))
        add(Item.ItemString(1, "package", ""))
    }

    fun addOnDataChangeListener(onDataChanged: (LinkedList<Item>) -> Unit) {
        dataListeners.add(onDataChanged)
    }

    fun removeOnDataChangeListener(onDataChanged: (LinkedList<Item>) -> Unit) {
        dataListeners.remove(onDataChanged)
    }

    fun run() {
        startCommand(params.prepareCommand(defaultParams[0].str, defaultParams[1].str))
    }

    private fun startCommand(command: String) {
        val process = runtime.exec(command)
        try {
            process.waitFor()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        val stdoutString: String =
            process.inputStream.bufferedReader().use(BufferedReader::readText)
        val stderrString: String =
            process.errorStream.bufferedReader().use(BufferedReader::readText)
        /*binding.command.text = command
        binding.error.text = stderrString
        binding.out.text = stdoutString*/
        println(command)
        println(stderrString)
        println(stdoutString)
    }

    fun clear() {
        this.params.clear()
        notifyChanges()
    }

    fun openConsole() {
        onConsoleOpen(isConsoleOpened)
        isConsoleOpened = !isConsoleOpened
    }

    fun addNewItem() {
        params.add(Item.ItemString(idCounter++, "key", "value"))
        notifyChanges()
    }

    fun changeData(index: Int, key: String, value: String) {
        updateData(index, key, value)
        notifyChanges()
    }

    fun removeItem(index: Int) {
        params.removeAt(index).run {
            notifyChanges()
        }
    }

    fun editIntent(intent: String) {
        defaultParams[0] = Item.ItemString(0, "intent", intent)
        notifyDefaultsChanges()
    }

    fun editAppPackage(packageName: String) {
        defaultParams[1] = Item.ItemString(1, "package", packageName)
        notifyDefaultsChanges()
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
            listener(params)
        }
    }

    private fun notifyDefaultsChanges() {
        onDefaultsChanged(defaultParams)
    }

    private fun updateData(index: Int, key: String, value: String) {
        params[index] = Item.ItemString(params[index].id, key, value)
    }
}