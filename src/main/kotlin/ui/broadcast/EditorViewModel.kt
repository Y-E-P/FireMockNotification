package ui.broadcast

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import repo.ParamsModel
import ui.base.BaseViewModel
import ui.broadcast.EditorContract.*
import ui.broadcast.EditorContract.Event.*
import utils.prepareCommand

class EditorViewModel(private val model: ParamsModel = ParamsModel()) :
    BaseViewModel<Event, State, Effect>(CoroutineScope(Dispatchers.Swing)) {

    override fun setInitialState(): State =
        State(model.packageName, model.intent)

    override fun handleEvents(event: Event) {
        when (event) {
            AddItem -> {
                model.addItem()
                updateItemsList()
            }
            is IntentUpdate -> {
                model.intent = event.intentName
                setState { copy(intentName = model.intent) }
            }
            is KeyUpdate -> {
                model.updateKey(event.index, event.key)
                updateItemsList()
            }
            is PackageUpdate -> {
                model.packageName = event.packageName
                setState { copy(packageName = model.packageName) }
            }
            is Remove -> {
                model.removeItem(event.index)
                updateItemsList()
            }
            is TypeUpdate -> {
                model.updateType(event.index, event.type)
                updateItemsList()
            }
            is ValueUpdate -> {
                model.updateValue(event.index, event.value)
                updateItemsList()
            }
            Clear -> {
                model.clear()
                updateItemsList()
            }
            Run -> setEffect { Effect.RunCommand(model.prepareCommand()) }
        }
    }

    private fun updateItemsList() {
        setState { copy(itemsList = ArrayList(model.params)) }
    }
}