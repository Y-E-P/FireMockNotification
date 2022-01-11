package repo

import Item
import ParamsModel
import org.json.simple.JSONArray
import org.json.simple.JSONObject


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
                    put(VALUE, (param as Item.ItemString).str)
                }.let {
                    this.add(it)
                }

            }
        }
        return jsonObject.toJSONString()
    }

    fun fromJson(json: String): ParamsModel {
        return ParamsModel()
    }

}