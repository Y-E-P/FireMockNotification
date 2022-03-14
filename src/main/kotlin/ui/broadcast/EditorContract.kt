package ui.broadcast

import repo.DataType
import repo.Item
import ui.base.ViewEvent
import ui.base.ViewSideEffect
import ui.base.ViewState

class EditorContract {

    sealed class Event : ViewEvent {
        data class PackageUpdate(val packageName: String) : Event()
        data class IntentUpdate(val intentName: String) : Event()
        data class KeyUpdate(val index: Int, val key: String) : Event()
        data class ValueUpdate(val index: Int, val value: String) : Event()
        data class TypeUpdate(val index: Int, val type: DataType) : Event()
        data class Remove(val index: Int) : Event()
        object AddItem : Event()
        object Clear : Event()
        object Run : Event()
    }

    data class State(
        val packageName: String,
        val intentName: String,
        val itemsList: List<Item> = listOf()
    ) : ViewState

    sealed class Effect : ViewSideEffect {
        data class RunCommand(val cmd: String) : Effect()
    }
}