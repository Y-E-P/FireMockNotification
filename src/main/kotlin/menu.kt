import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import resources.ResString

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FrameWindowScope.FireMenu(onClickItem: (MenuItem) -> Unit) {
    MenuBar {
        Menu(ResString.file, mnemonic = 'F') {
            Item(
                ResString.saveScheme,
                enabled = true,
                onClick = { onClickItem(MenuItem.SAVE) },
                shortcut = KeyShortcut(Key.S, ctrl = true)
            )
            Item(
                ResString.saveAsScheme,
                enabled = true,
                onClick = { onClickItem(MenuItem.SAVE_AS) },
                shortcut = KeyShortcut(Key.S, ctrl = true, alt = true)
            )
            Item(
                ResString.loadScheme,
                enabled = true,
                onClick = { onClickItem(MenuItem.OPEN) },
                shortcut = KeyShortcut(Key.O, ctrl = true)
            )
        }
        Menu(ResString.help, mnemonic = 'A') {
            Item(
                ResString.faq,
                enabled = false,
                onClick = { onClickItem(MenuItem.ABOUT) },
                shortcut = KeyShortcut(Key.V, ctrl = true)
            )
            Item(
                ResString.about,
                enabled = true,
                onClick = { onClickItem(MenuItem.ABOUT) },
                shortcut = KeyShortcut(Key.V, ctrl = true)
            )
        }
    }
}

enum class MenuItem {
    SAVE, SAVE_AS, OPEN, ABOUT
}
