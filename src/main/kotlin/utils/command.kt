package utils

import repo.Device
import repo.DeviceBuilder
import repo.Item
import repo.ParamsModel
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
        command = command.plus(" ").plus(item.value.commandType().type).plus(" ")
        command = command.plus(item.key).plus(" ").plus(item.value.data.toString()).escape()!!
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

private fun Item.commandType(): CommandLineType = when (this.type) {
    Item.DataType.STRING -> CommandLineType.StringValue
    Item.DataType.BOOLEAN -> CommandLineType.BooleanValue
    Item.DataType.INTEGER -> CommandLineType.IntegerValue
    Item.DataType.FLOAT -> CommandLineType.FloatValue
    Item.DataType.LONG -> CommandLineType.LongValue
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


