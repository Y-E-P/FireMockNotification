package repo

import java.util.*


data class Item(val id: Int, val key: String, val type: DataType, val data: Any) {

    enum class DataType {
        STRING, INTEGER, FLOAT, BOOLEAN, LONG
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
    val params: LinkedList<Item> = LinkedList(),
    private var idCounter: Int = 0
) {

    fun addItem(id: Int = ++idCounter, item: Item = Item(id, "", Item.DataType.STRING, "")) {
        params.add(item)
    }

    fun updateParam(index: Int, item: Item) {
        params[index] = item
    }

    fun updateKey(index: Int, key: String) {
        params[index] = params[index].copy(key = key)
    }

    fun updateValue(index: Int, value: Any) {
        params[index] = params[index].copy(data = value)
    }

    fun updateType(index: Int, value: Item.DataType) {
        val id = params[index].id
        val key = params[index].key
        params[index] = when (value) {
            Item.DataType.LONG -> Item(id, key, value, 0L)
            Item.DataType.STRING -> Item(id, key, value, "")
            Item.DataType.INTEGER -> Item(id, key, value, 0)
            Item.DataType.FLOAT -> Item(id, key, value, 0.0f)
            Item.DataType.BOOLEAN -> Item(id, key, value, false)
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