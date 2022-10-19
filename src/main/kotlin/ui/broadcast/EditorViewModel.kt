package ui.broadcast

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import repo.ModelParser
import repo.ParamsModel
import repo.Reaction
import ui.base.BaseViewModel
import ui.broadcast.EditorContract.*
import ui.broadcast.EditorContract.Event.*
import utils.prepareCommand
import java.io.File

class EditorViewModel : BaseViewModel<Event, State, Effect>(CoroutineScope(Dispatchers.Swing)) {

    private var currentFile: File? = null

    override fun setInitialState(): State = State("", "", DialogState.Closed)
    private val model: ParamsModel = ParamsModel()

    override fun handleEvents(event: Event) {
        when (event) {
            CreateItem -> {
                setState { copy(dialogState = DialogState.Open(model.createItem())) }
            }

            is EditItem -> {
                setState { copy(dialogState = DialogState.Open(event.item.copy())) }
            }

            CancelItem -> {
                setState { copy(dialogState = DialogState.Closed) }
            }

            is SaveItem -> {
                model.addItem(event.item.id, event.item)
                updateItemsList()
                setState { copy(dialogState = DialogState.Closed) }
            }

            is IntentUpdate -> {
                model.intent = event.intentName
                setState { copy(intentName = model.intent) }
            }

            is PackageUpdate -> {
                model.packageName = event.packageName
                setState { copy(packageName = model.packageName) }
            }

            is Remove -> {
                model.removeItem(event.id)
                updateItemsList()
            }

            Clear -> {
                model.clear()
                setState { copy(packageName = "", intentName = "", itemsList = ArrayList()) }
            }

            Run -> setEffect { Effect.RunCommand(model.prepareCommand()) }
            is SaveAs -> {
                currentFile = event.file
                CoroutineScope(Dispatchers.IO).launch {
                    event.file.writeText(ModelParser().toJson(paramsModel = model))
                }
            }

            Save -> {
                if (currentFile == null) {
                    setEffect { Effect.CreateFile }
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        currentFile?.writeText(ModelParser().toJson(paramsModel = model))
                    }
                }
            }

            is Load -> {
                CoroutineScope(Dispatchers.Swing).launch {
                    processFile(event.file).collect {
                        setState {
                            copy(
                                intentName = it.data.intent,
                                packageName = it.data.packageName,
                                itemsList = it.data.params.map { it.value }
                            )
                        }
                        currentFile = event.file
                    }
                }
            }
        }
    }

    private fun processFile(file: File): Flow<Reaction.Success<ParamsModel>> = flow {
        emit(Reaction.Success(ModelParser().fromJson(file.readText())))
    }

    private fun updateItemsList() {
        setState { copy(itemsList = model.params.map { it.value }.toList()) }
    }
}