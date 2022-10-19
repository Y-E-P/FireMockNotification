package repo

import java.util.*
import kotlin.collections.HashMap


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
    val params: MutableMap<Int, Item> = HashMap(),
    private var idCounter: Int = 0
) {

    fun createItem(id: Int = ++idCounter, item: Item = Item(id, "", Item.DataType.STRING, "")): Item {
        return item
    }

    fun addItem(id: Int = ++idCounter, item: Item){
        params[id] = item
    }

    fun updateType(index: Int, value: Item.DataType) {
        /*val id = params[index].id
        val key = params[index].key
        params[index] = when (value) {
            Item.DataType.LONG -> Item(id, key, value, 0L)
            Item.DataType.STRING -> Item(id, key, value, "")
            Item.DataType.INTEGER -> Item(id, key, value, 0)
            Item.DataType.FLOAT -> Item(id, key, value, 0.0f)
            Item.DataType.BOOLEAN -> Item(id, key, value, false)
        }*/
    }

    fun removeItem(id: Int) {
        params.remove(id)
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