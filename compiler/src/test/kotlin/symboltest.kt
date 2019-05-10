package dk.aau.cs.d409f19

import dk.aau.cs.d409f19.cellumata.CompilerSettings
import dk.aau.cs.d409f19.cellumata.ErrorLogger
import dk.aau.cs.d409f19.cellumata.ast.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.opentest4j.AssertionFailedError
import java.util.stream.Stream

@TestInstance(PER_CLASS)
@DisplayName("Symbol table tests")
class SymbolTest {

    /**
     * Reset ErrorLogger before each test
     */
    @BeforeEach
    fun resetErrorLogger() {
        ErrorLogger.reset()
    }

    /**
     * Returns list of values for testing right-hand-side of assignment statement.
     * TODO: Does not include negative values as they fail! When fixed, they should be implemented again.
     */
    fun assignStmtPassData(): List<String> {
        return IntRange(-50, 50).step(10).map { it.toString() }
            .union(listOf("true", "false", "113240987.734723984", "1.0000000000000001", "-1.0000000000000001"))
            .toList()
    }

    /**
     * Tests assignments statements which should be legal, given an identifier and value as parameter.
     */
    @ParameterizedTest
    @MethodSource("assignStmtPassData")
    fun assignStmtLiteralPassTest(value: String) {

        val compilerData = compileTestProgram(
            getWorldDeclString() + "\n\n" + getStateDeclString(body = "let x = $value;"),
            CompilerSettings()
        )

        // Ensure that symbol table actually exists
        assertNotNull(compilerData.symbolTable)

        // Get SymbolTable for first subscope, which is first StateDecl
        val stateSymbolTable = compilerData.symbolTable!!.tables[0]

        // Assert identifier of variable is contained in first symbol scope
        assertTrue(stateSymbolTable.symbols.containsKey("x"))

        // Switch on literal type, when either three assignable literals, assert equal value, else throw exception
        when (val literal = (stateSymbolTable.symbols["x"] as AssignStmt).expr) {
            is IntLiteral -> assertEquals(value.toInt(), literal.value)
            is BoolLiteral -> assertEquals(value.toBoolean(), literal.value)
            is FloatLiteral -> assertEquals(value.toFloat(), literal.value, 0.00000000000000000001f)
            else -> throw AssertionFailedError("Type was not of assignable type! Was " + literal.getType())
        }
    }

    /**
     * Returns the list of reserved words for testing data. Note that this cannot be passed to test by:
     * '@ValueSource("strings = RESERVED_WORDS")' as JUnit5 complains that the source is not compile-time static
     */
    fun parserReservedSymbolsData(): List<String> {
        return RESERVED_WORDS
    }

    /**
     * Tests whether parser recognises syntax errors on reserved words from symbol table
     */
    @ParameterizedTest
    @MethodSource("parserReservedSymbolsData")
    fun parserReservedSymbolsTest(identifier: String) {
        val parser = getParser(getWorldDeclString() + getStateDeclString(ident = identifier))
        // Stop parser from printing errors to stderr for less noisy console
        parser.removeErrorListeners()
        // Parse program
        parser.start()

        // Assert that syntax-errors are recognised for reserved words
        assertTrue(parser.numberOfSyntaxErrors > 0, "Test failed on: $identifier")
    }

    /**
     * Returns a list of arguments, which are an identifier and a list of strings
     */
    fun symbolRedefinitionFailData(): Stream<Arguments> {
        return Stream.of(
            Arguments.of("x", listOf("4", "2")),
            Arguments.of("x", listOf("true", "false")),
            Arguments.of("x", listOf("true", "true")),
            Arguments.of("x", listOf("4.2", "42.2")),
            Arguments.of("x", listOf("4", "2+2")),
            Arguments.of("x", listOf("true", "5")),
            Arguments.of("x", listOf("false", "-42.5")),
            Arguments.of("x", listOf("-42.5", "42.5")),
            Arguments.of("x", listOf("-42.5", "true"))
        )
    }

    /**
     * Tests whether each error on compiled program with multiple assignments on same identifier registers an
     * SymbolRedefinitionError and whether the identifier is equal to passed parameter.
     * Can take a generic list of values, which should be limited to int, float, and boolean types,
     * as language only supports those literals
     *
     * @param ident Identifier to use for each assignment statement
     * @param values List of values which will be assigned to given identifier
     */
    @ParameterizedTest
    @MethodSource("symbolRedefinitionFailData")
    fun symbolRedefinitionFailTest(ident: String, values: List<String>) {
        val stringBuilder = StringBuilder()
        // For each value, create assignment expression with equal identifier and value
        values.forEach { stringBuilder.appendln("let $ident = $it;") }

        // Compile boilerplate program with state having the constructed body
        compileTestProgramInsecure(getWorldDeclString() + getStateDeclString(body = stringBuilder.toString()))

        assertTrue(ErrorLogger.hasErrors(), "ErrorLogger is empty when expecting errors")

        // For each error recorded, assert that error is of SymbolRedefinitionError-type and with given identifier
        ErrorLogger.allErrors().forEach {
            assertTrue(
                it is SymbolRedefinitionError,
                "Class assertion error at: $it"
            ) // If class is not a SymbolRedefinitionError, AssertionFailedException is thrown
            assertTrue(
                (it as SymbolRedefinitionError).ident == ident,
                "Identifier error at: $it"
            ) /* If identifier of SymbolRedefinitionError is not equal to the
                    actual identifier passed, AssertionFailedException is thrown*/
        }
    }
}