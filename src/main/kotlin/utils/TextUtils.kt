package utils

import java.util.*

val floatRegex = "[+-]?([0-9]*[.])?[0-9]+"
val integerRegex = "[0-9]"


fun String?.escape(): String? {
    if (this == null) return null
    val sb = StringBuffer()
    escape(this, sb)
    return sb.toString()
}

/**
 * @param s - Must not be null.
 * @param sb
 */
fun escape(s: String, sb: StringBuffer) {
    for (element in s) {
        when (element) {
            '"' -> sb.append("\\\"")
            '\\' -> sb.append("\\\\")
            '\b' -> sb.append("\\b")
            '\n' -> sb.append("\\n")
            '\r' -> sb.append("\\r")
            '\t' -> sb.append("\\t")
            else ->
                if (element in '\u0000'..'\u001F' || element in '\u007F'..'\u009F' || element in '\u2000'..'\u20FF') {
                    val ss = Integer.toHexString(element.code)
                    sb.append("\\u")
                    var k = 0
                    while (k < 4 - ss.length) {
                        sb.append('0')
                        k++
                    }
                    sb.append(ss.uppercase(Locale.getDefault()))
                } else {
                    sb.append(element)
                }
        }
    }
}