package utils

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import java.awt.Component
import java.awt.Cursor
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.cursorForHorizontalResize(): Modifier = this.pointerHoverIcon(PointerIcon((Cursor(Cursor.N_RESIZE_CURSOR))))

fun Component.openFile(): File? {
    val fileChooser = JFileChooser()
    fileChooser.currentDirectory = File(System.getProperty("user.home"))
    fileChooser.fileFilter = FileNameExtensionFilter("JSON files (*.json)", "json")
    val result = fileChooser.showOpenDialog(this)
    return if (result == JFileChooser.APPROVE_OPTION) {
        val selectedFile = fileChooser.selectedFile
        println("Selected file: " + selectedFile.absolutePath)
        selectedFile
    } else null
}

fun Component.saveFile(json: String) {
    val fileChooser = JFileChooser()
    fileChooser.currentDirectory = File(System.getProperty("user.home"))
    fileChooser.fileFilter = FileNameExtensionFilter("JSON files (*.json)", "json")
    val result = fileChooser.showSaveDialog(this)
    if (result == JFileChooser.APPROVE_OPTION) {
        val selectedFile = fileChooser.selectedFile
        selectedFile.writeText(json)
        println("Selected file: " + selectedFile.absolutePath)
    }
}