import org.junit.Assert
import org.junit.Test
import utils.CommandLineType
import utils.prepareCommand
import java.util.*


class CommandTest {
    private val intent: String = "com.some.intent/Receiver"
    private val receiver: String = "com.some.intent.UPDATE"


    @Test
    fun testEmptyCommand() {
        val emptyLinkedList = LinkedList<Item>()
        Assert.assertEquals(
            emptyLinkedList.prepareCommand(intent, receiver),
            "adb am broadcast -n $receiver -a $intent"
        )
    }

    @Test
    fun testSingleParamStringCommand() {
        val dataset = LinkedList<Item>()
        dataset.add(Item.ItemString(0, "team", "supper-pupper"))
        Assert.assertEquals(
            "adb am broadcast -n $receiver -a $intent --es team supper-pupper", dataset.prepareCommand(intent, receiver)
        )
    }

    @Test
    fun testMultupleParamStringCommand() {
        val dataset = LinkedList<Item>()
        dataset.add(Item.ItemString(0, "team_string", "supper-pupper"))
        dataset.add(Item.ItemInt(1, "team_int", 100))
        dataset.add(Item.ItemFloat(2, "team_float", 100.1f))
        dataset.add(Item.ItemBoolean(3, "team_boolean", false))
        dataset.add(Item.ItemLong(4, "team_long", 100L))
        Assert.assertEquals(
            "adb am broadcast -n $receiver -a $intent ${CommandLineType.StringValue.type} team_string supper-pupper ${CommandLineType.IntegerValue.type} team_int 100 ${CommandLineType.FloatValue.type} team_float 100.1 ${CommandLineType.BooleanValue.type} team_boolean false ${CommandLineType.LongValue.type} team_long 100",
            dataset.prepareCommand(intent, receiver),
        )
    }

}