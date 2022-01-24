package ui.broadcast

import AppViewModel
import DataType
import Item
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dataAsString
import resources.ResString
import type
import ui.base.BaseDropdown
import java.util.*

@Composable
fun ParamsEditor(modifier: Modifier = Modifier, controller: AppViewModel) {
    var itemsList by mutableStateOf(controller.model.params.toMutableStateList())
    var packageName by mutableStateOf(controller.model.packageName)
    var intent by mutableStateOf(controller.model.intent)
    controller.addOnDataChangeListener {
        itemsList = it.params.toMutableStateList()
        intent = it.packageName
        packageName = it.intent
    }
    Column(modifier.fillMaxSize()) {
        DefaultParams(Modifier.padding(6.dp), packageName, intent) { message ->
            controller.sendMessage(message)
        }
        Row(modifier = Modifier.wrapContentSize()) {
            Button(modifier = Modifier.padding(4.dp), onClick = {
                controller.sendMessage(AppViewModel.Message.AddItem)
            }) {
                Text(ResString.addParam)
            }
        }
        LazyColumn {
            itemsIndexed(itemsList, key = { _, item -> item.id }) { index, item ->
                ParamItemView(index = index, item = item, onMessage = {
                    controller.sendMessage(it)
                })
            }
        }
    }
}

@Composable
fun DefaultParams(
    modifier: Modifier = Modifier,
    packageName: String,
    intent: String,
    onChanged: (AppViewModel.Message) -> Unit
) {
    Row(modifier = modifier.wrapContentSize()) {
        CombinedText(label = ResString.intentLabel, text = intent, onTextReady = {
            onChanged(AppViewModel.Message.IntentUpdate(it))
        })
        Spacer(Modifier.width(6.dp))
        CombinedText(label = ResString.packageLabel, text = packageName, onTextReady = {
            onChanged(AppViewModel.Message.PackageUpdate(it))
        })
    }
}

@Composable
fun ParamItemView(
    modifier: Modifier = Modifier,
    index: Int,
    item: Item,
    onMessage: (AppViewModel.Message) -> Unit
) {
    val dataType = remember { mutableStateOf(item.type()) }
    Row(modifier = modifier.wrapContentSize().padding(4.dp)) {
        CombinedText(modifier = Modifier.weight(0.3f), label = ResString.keyLabel, text = item.key, onTextReady = {
            onMessage(AppViewModel.Message.KeyUpdate(index, it))
        })
        Spacer(Modifier.width(6.dp))
        if (dataType.value == DataType.BOOLEAN) {
            BaseDropdown(modifier = Modifier.align(Alignment.CenterVertically), type = false, listOf(true, false), {
                onMessage(AppViewModel.Message.ValueUpdate(index, it.toString()))
            }, title = {
                Text(text = it.toString())
            }) {
                Text(text = if (it) ResString.trueString else ResString.falseString)
            }
        } else {
            CombinedText(
                modifier = Modifier.align(Alignment.CenterVertically).weight(0.3f),
                label = ResString.valueLabel,
                text = item.dataAsString(),
                onTextReady = {
                    onMessage(AppViewModel.Message.ValueUpdate(index, it))
                })
        }
        Spacer(Modifier.width(6.dp))

        BaseDropdown(
            modifier = Modifier.padding(4.dp).align(Alignment.CenterVertically),
            type = item.type(),
            DataType.values().toList(),
            title = {
                Text(text = it.name.uppercase(Locale.getDefault()))
            },
            onItemSelected = {
                onMessage(AppViewModel.Message.TypeUpdate(index, it))
                dataType.value = it
            }) {
            Text(text = it.name.uppercase(Locale.getDefault()))
        }
        IconButton(
            modifier = Modifier.padding(4.dp).align(Alignment.CenterVertically),
            onClick = { onMessage(AppViewModel.Message.Remove(index)) }) {
            Icon(Icons.Default.Delete, contentDescription = null)
        }
    }
}

@Composable
fun CombinedText(
    modifier: Modifier = Modifier,
    label: String,
    text: String,
    onTextReady: (text: String) -> Unit
) {
    var isInError by remember { mutableStateOf(false) }
    OutlinedTextField(
        modifier = modifier,
        isError = isInError,
        value = text,
        label = { Text(text = if (!isInError) label else ResString.emptyError) },
        onValueChange = { newText ->
            if (newText != text) {
                onTextReady(newText.trim())
            }
            isInError = newText.isEmpty()
        }
    )
}
