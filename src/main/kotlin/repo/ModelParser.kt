package repo

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.JSONValue
import org.json.simple.parser.ParseException


class ModelParser {

    companion object {
        private const val INTENT = "intent"
        private const val PACKAGE = "package"
        private const val PARAMS = "params"
        private const val TYPE = "type"
        private const val BOOLEAN = "boolean"
        private const val STRING = "string"
        private const val INTEGER = "integer"
        private const val LONG = "long"
        private const val FLOAT = "float"
        private const val KEY = "key"
        private const val VALUE = "value"
    }

    fun toJson(paramsModel: ParamsModel): String {
        val jsonObject = JSONObject()
        jsonObject[INTENT] = paramsModel.intent
        jsonObject[PACKAGE] = paramsModel.packageName
        jsonObject[PARAMS] = JSONArray().apply {
            for (param in paramsModel.params) {
                JSONObject().apply {
                    put(KEY, param.key)
                    put(VALUE, param.data)
                    put(TYPE, param.getType())
                }.let {
                    this.add(it)
                }
            }
        }
        return jsonObject.toJSONString()
    }

    fun fromJson(json: String, onError: (error: String) -> Unit = {}): ParamsModel {
        val result = ParamsModel()
        try {
            val mainObj = JSONValue.parseWithException(json) as JSONObject
            result.intent = mainObj[INTENT] as String
            result.packageName = mainObj[PACKAGE] as String
            val paramsArray = mainObj[PARAMS] as JSONArray
            for (i in 0 until paramsArray.size) {
                val paramObj = (paramsArray[i] as JSONObject)
                val key: String = paramObj[KEY] as String
                val type = paramObj[TYPE] as String
                result.addItem(i, Item(i, key, type.getTypeBy(), type.getValueByTypeBy(paramObj)))
            }
        } catch (e: ParseException) {
            onError(e.message ?: "Unknown error")
        }

        return result
    }

    private fun Item.getType(): String = when (this.type) {
        Item.DataType.STRING -> STRING
        Item.DataType.BOOLEAN -> BOOLEAN
        Item.DataType.INTEGER -> INTEGER
        Item.DataType.FLOAT -> FLOAT
        Item.DataType.LONG -> LONG
    }

    private fun String.getTypeBy(): Item.DataType = when (this) {
        BOOLEAN -> Item.DataType.BOOLEAN
        INTEGER -> Item.DataType.INTEGER
        FLOAT -> Item.DataType.FLOAT
        LONG -> Item.DataType.LONG
        else -> Item.DataType.STRING
    }

    private fun String.getValueByTypeBy(obj: JSONObject): Any = when (this) {
        BOOLEAN -> obj[VALUE] as Boolean
        INTEGER -> obj[VALUE] as Int
        FLOAT -> obj[VALUE] as Float
        LONG -> obj[VALUE] as Long
        else -> obj[VALUE] as String
    }


}