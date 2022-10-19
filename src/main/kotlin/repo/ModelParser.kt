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
                    put(VALUE, param.value.data)
                    put(TYPE, param.value.type.name)
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
                val type = paramObj[TYPE] as? String ?: Item.DataType.STRING.name
                result.addItem(i, Item(i, key, type.getTypeBy(), type.getValueByTypeBy(paramObj)))
            }
        } catch (e: ParseException) {
            onError(e.message ?: "Unknown error")
        }

        return result
    }

    private fun String.getTypeBy(): Item.DataType = try {
        Item.DataType.valueOf(this)
    } catch (e: Exception) {
        Item.DataType.STRING
    }

    private fun String.getValueByTypeBy(obj: JSONObject): Any {
        val rawItem = obj[VALUE] as? String
        val item = when (getTypeBy()) {
            Item.DataType.BOOLEAN -> rawItem?.toBoolean() ?: false
            Item.DataType.INTEGER -> rawItem?.toInt() ?: 0
            Item.DataType.FLOAT -> rawItem?.toFloat() ?: 0.0f
            Item.DataType.LONG -> rawItem?.toLong() ?: 0L
            else -> rawItem ?: ""
        }
        return item
    }


}