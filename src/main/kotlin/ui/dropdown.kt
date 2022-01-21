package ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import resources.ResString

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
