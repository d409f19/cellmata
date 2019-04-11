package dk.aau.cs.d409f19

import dk.aau.cs.d409f19.Utilities.Companion.compileProgram
import org.junit.jupiter.api.Test
import java.io.File

private const val batchDir = "src/main/resources/compiling-programs/"

class BatchTest {

    /**
     * Tests whether all programs under a given path compiles
     */
    @Test
    fun batchPass() {
        batchDir.getPrograms().forEach {
            compileProgram(it)
        }
    }

    /**
     * Returns a list of strings from each '.cell' file under a given directory.
     * Since this function is a class-extension of string, but private, which takes a string as a receiver,
     * this only alters functionality of strings under this class
     */
    private fun String.getPrograms(): List<String> {
        val list = mutableListOf<String>()
        // Walk top-down
        File(this).walk().forEach {
            // If it is a file and has extension 'cell', then add source to list as string
            if (it.isFile && it.extension == "cell") {
                list.add(it.readText())
            }
        }
        return list
    }
}