package utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import java.awt.Component
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/*
fun Modifier.pointerMoveFilter(
    onEnter: () -> Boolean = { true },
    onExit: () -> Boolean = { true },
    onMove: (Offset) -> Boolean = { true }
): Modifier

fun Modifier.cursorForHorizontalResize(): Modifier*/

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