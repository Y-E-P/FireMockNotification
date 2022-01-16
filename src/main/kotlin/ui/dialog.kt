package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import resources.ResString

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.requiredWidth(300.dp),
        title = {
            Text(text = "Title")
        },
        text = {
            Column {
                Text("Custom Text")
                Checkbox(checked = false, onCheckedChange = {})
            }
        },
        buttons = {
            Row(
                modifier = Modifier.padding(all = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDismiss
                ) {
                    Text(ResString.ok)
                }
            }
        }
    )
}

@Composable
fun <T> BaseDropdown(
    modifier: Modifier = Modifier,
    type: T,
    items: Array<T>,
    onItemSelected: (T) -> Unit,
    title: @Composable (T) -> Unit,
    content: @Composable (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var data by remember { mutableStateOf(type) }
    Box(modifier = modifier) {
        Button(onClick = { expanded = true }) {
            title(data)
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                contentDescription = ResString.changeType
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            for (item in items) {
                DropdownMenuItem(onClick = {
                    data = item
                    expanded = false
                    onItemSelected(item)
                }) {
                    content(item)
                }
            }
        }
    }
}
