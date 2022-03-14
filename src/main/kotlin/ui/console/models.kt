package ui.console

import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ConsoleModel {
    private val consoleData: MutableList<ConsoleItem> = mutableListOf()
    var onConsoleDataChanged: (List<ConsoleItem>) -> Unit = {}

    fun emitError(data: String) {
        emit(data, ConsoleItem.Type.ERROR)
    }

    fun emitOutput(data: String) {
        emit(data, ConsoleItem.Type.OUTPUT)
    }

    fun emitInput(data: String) {
        emit(data, ConsoleItem.Type.INPUT)
    }

    private fun emit(data: String, type: ConsoleItem.Type) {
        val id = UUID.randomUUID().toString()
        val time = SimpleDateFormat("yyyy/MM/dd HH:mm").format(Date())
        ConsoleItem(id, data, type, time).let {
            consoleData.add(it)
            onConsoleDataChanged(ArrayList(consoleData))
        }
    }
}

class ConsoleItem(val id: String, val data: String, val type: Type, val time: String) {
    enum class Type {
        ERROR, OUTPUT, INPUT
    }
}