import java.util.*

sealed class Item(open val id: Int, open var key: String) {
    data class ItemString(
        override val id: Int,
        override var key: String,
        var str: String
    ) : Item(id, key)

    data class ItemBoolean(
        override val id: Int, override var key: String, val boolean: Boolean
    ) : Item(id, key)

    data class ItemFloat(override val id: Int, override var key: String, val number: Float) : Item(id, key)
    data class ItemInt(override val id: Int, override var key: String, val number: Int) : Item(id, key)
    data class ItemLong(override val id: Int, override var key: String, val number: Long) : Item(id, key)
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
    STRING, INTEGER, FLOAT, BOOLEAN, LONG/*, URI*/
}

sealed class ConsoleItem(open val data: String) {
    data class Error(val output: String) : ConsoleItem(output)
    data class Input(val output: String) : ConsoleItem(output)
    data class Output(val output: String) : ConsoleItem(output)
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

    fun updateValue(index: Int, value: String) {
        (params[index] as Item.ItemString).str = value
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