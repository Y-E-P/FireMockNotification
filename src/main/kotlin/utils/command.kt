package utils

import Device
import DeviceBuilder
import Item
import ParamsModel
import java.util.*

/**
 * Documentation https://developer.android.com/studio/command-line/adb#IntentSpec
 * -e | --es extra_key extra_string_value
Add string data as a key-value pair.
--ez extra_key extra_boolean_value
Add boolean data as a key-value pair.
--ei extra_key extra_int_value
Add integer data as a key-value pair.
--el extra_key extra_long_value
Add long data as a key-value pair.
--ef extra_key extra_float_value
Add float data as a key-value pair.
 * */

const val BROADCAST = "adb shell am broadcast"

fun ParamsModel.prepareCommand(): String {
    var command = BROADCAST.plus(" ")
        .plus(CommandLineType.Receiver.type)
        .plus(" ")
        .plus(this.packageName)
        .plus(" ")
        .plus(CommandLineType.Intent.type)
        .plus(" ")
        .plus(this.intent)
    for (item in this.params) {
        command = command.plus(" ").plus(item.commandType().type).plus(" ")
        command = when (item) {
            is Item.ItemString -> command.plus(item.key).plus(" ").plus(item.str).escape()!!
            is Item.ItemBoolean -> command.plus(item.key).plus(" ").plus(item.boolean)
            is Item.ItemInt -> command.plus(item.key).plus(" ").plus(item.number)
            is Item.ItemFloat -> command.plus(item.key).plus(" ").plus(item.number)
            is Item.ItemLong -> command.plus(item.key).plus(" ").plus(item.number)
        }
    }
    return command
}

enum class CommandLineType(val type: String) {
    StringValue("--es"),
    FloatValue("--ef"),
    BooleanValue("--ez"),
    LongValue("--el"),
    IntegerValue("--ei"),
    Intent("-a"),
    Receiver("-n"),
}

private fun Item.commandType(): CommandLineType = when (this) {
    is Item.ItemString -> CommandLineType.StringValue
    is Item.ItemBoolean -> CommandLineType.BooleanValue
    is Item.ItemInt -> CommandLineType.IntegerValue
    is Item.ItemFloat -> CommandLineType.FloatValue
    is Item.ItemLong -> CommandLineType.LongValue
}

fun parseDevicesList(line: String): Device {
    val stringTokenizer = StringTokenizer(line)
    val builder = DeviceBuilder()
    while (stringTokenizer.hasMoreTokens()) {
        if (builder.id.isEmpty()) {
            builder.id = stringTokenizer.nextToken()
        }
        val item = stringTokenizer.nextToken()
        if (item.contains("model:")) {
            val splitedModel = item.split(":")
            builder.model = splitedModel[1]
        }
        if (item.contains("device:")) {
            val splitedModel = item.split(":")
            builder.name = splitedModel[1]
        }
    }
    return builder.build()
}


