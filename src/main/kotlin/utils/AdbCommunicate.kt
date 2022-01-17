package utils

import java.io.BufferedReader

class AdbCommunicate {

    fun execute(command: String, onCallback: (ConsoleOutput) -> Unit) {
        try {
            val process = Runtime.getRuntime().exec(command)
            val stdoutString: String =
                process.inputStream.bufferedReader().use(BufferedReader::readText)
            val stderrString: String =
                process.errorStream.bufferedReader().use(BufferedReader::readText)
            process.waitFor()
            stdoutString.takeIf { it.isNotEmpty() }?.let { onCallback(ConsoleOutput.Success(it)) }
            stderrString.takeIf { it.isNotEmpty() }?.let { onCallback(ConsoleOutput.Error(it)) }
        } catch (e: InterruptedException) {
            onCallback(ConsoleOutput.Error(e.message ?: "Unknown error"))
        } catch (e: Exception) {
            onCallback(ConsoleOutput.Error(e.message ?: "Unknown error"))
        }
    }

    sealed class ConsoleOutput(open val data: String) {
        data class Error(override val data: String) : ConsoleOutput(data)
        data class Success(override val data: String) : ConsoleOutput(data)
    }


}