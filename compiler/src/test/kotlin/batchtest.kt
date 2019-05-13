package dk.aau.cs.d409f19

import dk.aau.cs.d409f19.cellumata.CompilerSettings
import dk.aau.cs.d409f19.cellumata.ErrorLogger
import dk.aau.cs.d409f19.cellumata.TerminatedCompilationException
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.opentest4j.AssertionFailedError
import java.io.File
import java.util.stream.Stream

private const val passingBatchDir = "src/main/resources/compiling-programs/"
private const val failingBatchDir = "src/main/resources/non-compiling-programs/"
private const val failingBatchMissingFeaturesDir = "src/main/resources/non-compiling-feature-programs/"

private val failingDirs = listOf(failingBatchDir, failingBatchMissingFeaturesDir)

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
         */
        @ParameterizedTest
        @MethodSource("getCompilingPrograms")
        fun batchPass(filename: String, program: String) {
            try {
                compileTestProgramKotlin(program)
            } catch (e: TerminatedCompilationException) {
                ErrorLogger.printAllErrors()
                fail { "CompileErrors occurred while compiling '$filename'" }
            } catch (e: Exception) {
                fail("Compiler crashed compiling '$filename'!", e)
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

            // For each dir of failing programs
            failingDirs.forEach {
                // Walk top-down
                File(it).walk().forEach {
                    // If it is a file and has extension 'cell'
                    if (it.isFile && it.extension == "cell") {
                        list.add(Arguments.of(it.name, it.readText()))
                    }
                }
            }
            return list.stream()
        }

        /**
         * Batch-compiles all programs under the non-compiling dirs and fails on error-free compilation.
         * Also checks that ErrorLogger handles errors correctly during assertNoErrors-calls under compilation.
         * TODO: differentiate between different failing compilations
         */
        @ParameterizedTest
        @MethodSource("getNonCompilingPrograms")
        fun batchFail(filename: String, program: String) {
            try {
                assertThrows<TerminatedCompilationException>(
                    "Non-compiling program did not throw a TerminatedCompilationException! Filename: $filename"
                ) {
                    compileTestProgramKotlin(program)
                }
            } catch (e: AssertionFailedError) {
                try {
                    // If compilation does not throw a TerminatedCompilationException,
                    // compile again and assert for errors in parser
                    val compileData = compileTestProgramKotlin(program)

                    // Assert that no errors are contained in ErrorLogger, as this would indicate a fault with asserting for no errors
                    assertFalse(
                        ErrorLogger.hasErrors(),
                        "Non-compiling program failed spectacularly! Errorlogger initially didn't throw an " +
                                "TerminatedCompilationException, yet the ErrorLogger contains errors!"
                    )
                    // Assert that parsing errors are found
                    assertTrue(
                        compileData.parser.numberOfSyntaxErrors > 0,
                        "Non-compiling program had neither any TerminatedCompilationException nor parsing-errors! Filename: $filename"
                    )
                } catch (e: Exception) {
                    fail("Spectacular error in compiler. Printing stacktrace of: ${e.javaClass}", e)
                }

            }
        }
    }
}