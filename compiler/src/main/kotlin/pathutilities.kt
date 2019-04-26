package dk.aau.cs.d409f19.cellumata

import java.nio.file.Path

/**
 * Returns the file extension of a file. E.g. "directory/program.cell" returns "cell". The method should also support
 * directories with dots in their name.
 */
fun Path.extention(): String {
    val str = this.toString()
    val lastDot = str.lastIndexOf('.')
    val lastSeparator = Math.max(str.lastIndexOf('/'), str.lastIndexOf('\\'))
    return if (lastDot > lastSeparator) str.substring(lastDot + 1) else ""
}