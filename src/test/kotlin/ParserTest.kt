import org.junit.Before
import org.junit.Test
import repo.ModelParser
import repo.ParamsModel

class ParserTest {

    private val model: ParamsModel = ParamsModel()
    private val parser: ModelParser = ModelParser()

    @Before

    fun prepareData() {
        model.intent = "com.intent"
        model.packageName = "com.myapp.some.RECEIVE"
        for (i: Int in 0 until 10) {
            model.addItem(i)
        }
        parser.toJson(model)
    }

    @Test
    fun prepare_json_test() {

    }
}