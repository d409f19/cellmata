package dk.aau.cs.d409f19

import dk.aau.cs.d409f19.cellumata.ErrorLogger
import dk.aau.cs.d409f19.cellumata.TerminatedCompilationException
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.util.stream.Stream
import org.junit.jupiter.api.Assertions.fail as Fail

private const val passingBatchDir = "src/main/resources/compiling-programs/"
private const val failingBatchDir = "src/main/resources/non-compiling-programs/"

class BatchTest {

    @TestInstance(PER_CLASS)
    @DisplayName("Batch-testing passing programs")
    class BatchPassingTests {

        /**
         * Reset ErrorLogger before each test
         */
        @BeforeEach
        fun resetErrorLogger() {
            ErrorLogger.reset()
        }

        /**
         * Returns a list of strings from each '.cell' file under the compiling-programs dir
         */
        private fun getCompilingPrograms(): Stream<Arguments> {
            val list = mutableListOf<Arguments>()
            // Walk top-down
            File(passingBatchDir).walk().forEach {
                // If it is a file and has extension 'cell'
                if (it.isFile && it.extension == "cell") {
                    list.add(Arguments.of(it.name, it.readText()))
                }
            }
            return list.stream()
        }

        /**
         * Batch-compiles all programs under the compiling dir and fails on failed compilation
         * TODO: better output on failed compiles
         */
        @ParameterizedTest
        @MethodSource("getCompilingPrograms")
        fun batchPass(filename: String, program: String) {
            compileProgram(program)
            // If any errors found, print them and throw exception
            if (ErrorLogger.hasErrors()) {
                ErrorLogger.printAllErrors()
                throw TerminatedCompilationException("Errors occurred in program compilation! Filename: $filename")
            }
        }
    }

    @TestInstance(PER_CLASS)
    @DisplayName("Batch-testing failing programs")
    class BatchFailingTests {

        /**
         * Reset ErrorLogger before each test
         */
        @BeforeEach
        fun resetErrorLogger() {
            ErrorLogger.reset()
        }

        /**
         * Returns a list of strings from each '.cell' file under the non-compiling-programs dir
         */
        private fun getNonCompilingPrograms(): Stream<Arguments> {
            val list = mutableListOf<Arguments>()
            // Walk top-down
            File(failingBatchDir).walk().forEach {
                // If it is a file and has extension 'cell'
                if (it.isFile && it.extension == "cell") {
                    list.add(Arguments.of(it.name, it.readText()))
                }
            }
            return list.stream()
        }

        /**
         * Batch-compiles all programs under the non-compiling dir and fails on error-free compilation
         * TODO: differentiate between different failing compilations
         */
        @ParameterizedTest
        @MethodSource("getNonCompilingPrograms")
        fun batchFail(filename: String, program: String) {
            val compileData = compileProgram(program)
            // Assert that errors are found
            assertTrue(
                ErrorLogger.hasErrors() || compileData.parser.numberOfSyntaxErrors > 0,
                "Non-compiling program compiled! Filename: $filename"
            )
        }
    }
}