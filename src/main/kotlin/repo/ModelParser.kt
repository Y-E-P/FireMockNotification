package repo

import Item
import ParamsModel
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
                    when (param) {
                        is Item.ItemString -> param.str
                        is Item.ItemBoolean -> param.boolean
                        is Item.ItemInt -> param.number
                        is Item.ItemFloat -> param.number
                        is Item.ItemLong -> param.number
                    }.also {
                        put(VALUE, it)
                    }
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
                val type = paramObj[TYPE] as? String
                when (type) {
                    STRING -> result.addItem(i, Item.ItemString(i, key, paramObj[VALUE] as String))
                    BOOLEAN -> result.addItem(i, Item.ItemBoolean(i, key, paramObj[VALUE] as Boolean))
                    INTEGER -> result.addItem(i, Item.ItemInt(i, key, paramObj[VALUE] as Int))
                    FLOAT -> result.addItem(i, Item.ItemFloat(i, key, paramObj[VALUE] as Float))
                    LONG -> result.addItem(i, Item.ItemLong(i, key, paramObj[VALUE] as Long))
                    else -> result.addItem(i, Item.ItemString(i, key, paramObj[VALUE] as String))
                }
            }
        } catch (e: ParseException) {
            onError(e.message ?: "Unknown error")
        }

        return result
    }

    private fun Item.getType(): String = when (this) {
        is Item.ItemString -> STRING
        is Item.ItemBoolean -> BOOLEAN
        is Item.ItemInt -> INTEGER
        is Item.ItemFloat -> FLOAT
        is Item.ItemLong -> LONG
    }


}