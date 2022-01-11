import org.junit.Assert
import org.junit.Before
import org.junit.Test
import utils.CommandLineType
import utils.prepareCommand
import java.util.*


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
        model.addItem(0, Item.ItemString(0, "team", "supper-pupper"))
        Assert.assertEquals(
            "adb shell am broadcast -n $receiver -a $intent --es team supper-pupper", model.prepareCommand()
        )
        model.clear()
    }

    @Test
    fun testMultupleParamStringCommand() {
        model.addItem(0,Item.ItemString(0, "team_string", "supper-pupper"))
        model.addItem(1,Item.ItemInt(1, "team_int", 100))
        model.addItem(2,Item.ItemFloat(2, "team_float", 100.1f))
        model.addItem(3,Item.ItemBoolean(3, "team_boolean", false))
        model.addItem(4,Item.ItemLong(4, "team_long", 100L))
        Assert.assertEquals(
            "adb shell am broadcast -n $receiver -a $intent ${CommandLineType.StringValue.type} team_string supper-pupper ${CommandLineType.IntegerValue.type} team_int 100 ${CommandLineType.FloatValue.type} team_float 100.1 ${CommandLineType.BooleanValue.type} team_boolean false ${CommandLineType.LongValue.type} team_long 100",
            model.prepareCommand(),
        )
    }

}