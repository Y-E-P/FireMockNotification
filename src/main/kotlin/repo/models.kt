import java.text.SimpleDateFormat
import java.util.*


sealed class Item(open val id: Int, open var key: String) {
    data class ItemString(
        override val id: Int,
        override var key: String,
        var str: String
    ) : Item(id, key)

    data class ItemBoolean(
        override val id: Int, override var key: String, var boolean: Boolean
    ) : Item(id, key)

    data class ItemFloat(override val id: Int, override var key: String, var number: Float) : Item(id, key)
    data class ItemInt(override val id: Int, override var key: String, var number: Int) : Item(id, key)
    data class ItemLong(override val id: Int, override var key: String, var number: Long) : Item(id, key)
}

fun Item.type(): DataType =
    when (this) {
        is Item.ItemString -> DataType.STRING
        is Item.ItemBoolean -> DataType.BOOLEAN
        is Item.ItemFloat -> DataType.FLOAT
        is Item.ItemInt -> DataType.INTEGER
        is Item.ItemLong -> DataType.LONG
        else -> DataType.STRING
    }

fun Item.dataAsString(): String =
    when (this) {
        is Item.ItemString -> str
        is Item.ItemBoolean -> this.boolean.toString()
        is Item.ItemFloat -> this.number.toString()
        is Item.ItemInt -> this.number.toString()
        is Item.ItemLong -> this.number.toString()
        else -> this.toString()
    }

enum class DataType {
    STRING, INTEGER, FLOAT, BOOLEAN, LONG/*, URI*/
}

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
            onConsoleDataChanged(consoleData)
        }
    }

}

public class ConsoleItem(val id: String, val data: String, val type: Type, val time: String) {
    enum class Type {
        ERROR, OUTPUT, INPUT
    }
}

sealed class Reaction<out T> {
    class Success<out T>(val data: T) : Reaction<T>()
    object Loading : Reaction<Nothing>()
    object Empty : Reaction<Nothing>()
    class Error(val exception: Throwable) : Reaction<Nothing>()
}

data class ParamsModel(
    var intent: String = "",
    var packageName: String = "",
    val params: LinkedList<Item> = LinkedList()
) {

    fun addItem(id: Int, item: Item = Item.ItemString(id, "", "")) {
        params.add(item)
    }

    fun updateParam(index: Int, item: Item) {
        params[index] = item
    }

    fun updateKey(index: Int, key: String) {
        params[index].key = key
    }

    fun updateValue(index: Int, value: Any) {
        when (value) {
            is String -> (params[index] as? Item.ItemString)?.str = value
            is Int -> (params[index] as? Item.ItemInt)?.number = value
            is Boolean -> (params[index] as? Item.ItemBoolean)?.boolean = value
            is Float -> (params[index] as? Item.ItemFloat)?.number = value
            is Long -> (params[index] as? Item.ItemLong)?.number = value
        }

    }

    fun updateType(index: Int, value: DataType) {
        val id = params[index].id
        val key = params[index].key
        params[index] = when (value) {
            DataType.LONG -> Item.ItemLong(id, key, 0)
            DataType.STRING -> Item.ItemString(id, key, "")
            DataType.INTEGER -> Item.ItemInt(id, key, 0)
            DataType.FLOAT -> Item.ItemFloat(id, key, 0.0f)
            DataType.BOOLEAN -> Item.ItemBoolean(id, key, false)
        }
    }

    fun removeItem(index: Int) {
        params.removeAt(index)
    }

    fun clear() {
        intent = ""
        packageName = ""
        this.params.clear()
    }
}


data class Device(val id: String, val model: String, val name: String)

class DeviceBuilder {
    var id: String = ""
    var model: String = ""
    var name: String = ""
    fun build() = Device(id, model, name)
}