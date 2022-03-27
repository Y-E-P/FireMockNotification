import org.junit.Assert
import org.junit.Before
import org.junit.Test
import repo.Device
import repo.Item
import repo.ParamsModel
import utils.CommandLineType
import utils.parseDevicesList
import utils.prepareCommand


class CommandTest {
    private val intent: String = "com.some.intent/Receiver"
    private val receiver: String = "com.some.intent.UPDATE"

    private val model = ParamsModel()

    @Before
    fun prepareData() {
        model.intent = intent
        model.packageName = receiver
    }

    @Test
    fun testEmptyCommand() {
        Assert.assertEquals(
            model.prepareCommand(),
            "adb shell am broadcast -n $receiver -a $intent"
        )
    }

    @Test
    fun testSingleParamStringCommand() {
        model.addItem(0, Item(0, "team", Item.DataType.STRING, "supper-pupper"))
        Assert.assertEquals(
            "adb shell am broadcast -n $receiver -a $intent --es team supper-pupper", model.prepareCommand()
        )
        model.clear()
    }

    @Test
    fun testMultipleParamStringCommand() {
        model.addItem(0, Item(0, "team_string", Item.DataType.STRING, "supper-pupper"))
        model.addItem(1, Item(1, "team_int", Item.DataType.INTEGER, 100))
        model.addItem(2, Item(2, "team_float", Item.DataType.FLOAT, 100.1f))
        model.addItem(3, Item(3, "team_boolean", Item.DataType.BOOLEAN, false))
        model.addItem(4, Item(4, "team_long", Item.DataType.LONG, 100L))
        Assert.assertEquals(
            "adb shell am broadcast -n $receiver -a $intent ${CommandLineType.StringValue.type} team_string supper-pupper ${CommandLineType.IntegerValue.type} team_int 100 ${CommandLineType.FloatValue.type} team_float 100.1 ${CommandLineType.BooleanValue.type} team_boolean false ${CommandLineType.LongValue.type} team_long 100",
            model.prepareCommand(),
        )
    }

    @Test
    fun parseDevice() {
        val expectedDevice = Device("emulator-5554", "Android_SDK_built_for_x86", "generic_x86")
        val line =
            "emulator-5554          device product:sdk_gphone_x86 model:Android_SDK_built_for_x86 device:generic_x86 transport_id:1"
        val actualDevice = parseDevicesList(line)
        Assert.assertEquals(expectedDevice, actualDevice)
    }
}