package repo

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
    }

fun Item.dataAsString(): String =
    when (this) {
        is Item.ItemString -> str
        is Item.ItemBoolean -> this.boolean.toString()
        is Item.ItemFloat -> this.number.toString()
        is Item.ItemInt -> this.number.toString()
        is Item.ItemLong -> this.number.toString()
    }

enum class DataType {
    STRING, INTEGER, FLOAT, BOOLEAN, LONG
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
    val params: LinkedList<Item> = LinkedList(),
    private var idCounter: Int = 0
) {

    fun addItem(id: Int = ++idCounter, item: Item = Item.ItemString(id, "", "")) {
        params.add(item)
    }

    fun updateParam(index: Int, item: Item) {
        params[index] = item
    }

    fun updateKey(index: Int, key: String) {
        params[index].key = key
    }

    fun updateValue(index: Int, value: Any) {
        params[index] = when (value) {
            is String -> (params[index] as? Item.ItemString)?.copy(str = value)
            is Int -> (params[index] as? Item.ItemInt)?.copy(number = value)
            is Boolean -> (params[index] as? Item.ItemBoolean)?.copy(boolean = value)
            is Float -> (params[index] as? Item.ItemFloat)?.copy(number = value)
            is Long -> (params[index] as? Item.ItemLong)?.copy(number = value)
            else -> {
                (params[index] as Item.ItemString).copy()
            }
        }!!

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