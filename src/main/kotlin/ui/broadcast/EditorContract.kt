package ui.broadcast

import repo.Item
import ui.base.ViewEvent
import ui.base.ViewSideEffect
import ui.base.ViewState
import java.io.File
import kotlin.contracts.Effect

class EditorContract {

    sealed class Event : ViewEvent {
        data class PackageUpdate(val packageName: String) : Event()
        data class IntentUpdate(val intentName: String) : Event()
        data class KeyUpdate(val index: Int, val key: String) : Event()
        data class ValueUpdate(val index: Int, val value: String) : Event()
        data class TypeUpdate(val index: Int, val type: Item.DataType) : Event()
        data class Remove(val index: Int) : Event()
        object CreateItem : Event()
        object CancelItem : Event()
        data class SaveItem(val item: Item) : Event()
        object Clear : Event()
        object Run : Event()
        data class SaveAs(val file: File) : Event()
        object Save : Event()
        data class Load(val file: File) : Event()
    }

    data class State(
        val packageName: String,
        val intentName: String,
        val dialogState: DialogState = DialogState.Closed,
        val itemsList: List<Item> = listOf()
    ) : ViewState

    sealed class DialogState{
        data class Open(val item: Item): DialogState()
        object Closed: DialogState()
    }

    sealed class Effect : ViewSideEffect {
        data class RunCommand(val cmd: String) : Effect()
        object CreateFile : Effect()
    }
}