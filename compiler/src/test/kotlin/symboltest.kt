package dk.aau.cs.d409f19

import dk.aau.cs.d409f19.cellumata.ErrorLogger
import dk.aau.cs.d409f19.cellumata.ast.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.opentest4j.AssertionFailedError
import java.lang.NullPointerException

class SymbolTest {

    /**
     * Reset ErrorLogger as it is globally persistent and will interfere with tests
     */
    @BeforeEach
    fun resetErrorLogger() {
        ErrorLogger.reset()
    }

    /**
     * Tests assignments statements which should be legal, given an identifier and value as parameter.
     * If assertions fails, exceptions are caught, and false is returned to indicate a failed test to caller.
     */
    private fun <T> assignStmtPass(ident: String, value: T): Boolean {

        val compilerData =
            compileProgram(getWorldDeclString() + "\n\n" + getStateDeclString(body = "let $ident = $value;"))

        // Get SymbolTable for first subscope, which is first StateDecl
        val stateSymbolTable = compilerData.symbolTable.tables[0]

        try {
            // Assert identifier of variable is contained in first symbol scope
            assertTrue(stateSymbolTable.symbols.containsKey(ident))

            // Get expression of AssignStmt which is value of the key of ident. Will
            val literal = (stateSymbolTable.symbols[ident] as AssignStmt).expr

            // Switch on literal type, when either three assignable literals, assert equal value, else throw exception
            when (literal) {
                is IntLiteral -> assertEquals(value, literal.value)
                is BoolLiteral -> assertEquals(value, literal.value)
                is FloatLiteral -> assertEquals(value, literal.value)
                else -> throw AssertionFailedError("Type was not of assignable type! Was " + literal.getType())
            }
        } catch (e: AssertionFailedError) { // If any assertion errors thrown, catch and return false
            return false
        }
        return true
    }

    /**
     * Dispatches assignment statement tests
     */
    @Test
    fun assignStmtTest() {
        for (i in -50..50) assertTrue(assignStmtPass("x", i))
        assertTrue(assignStmtPass("floatingPoint", 1636098.1239487.toFloat()))
        assertTrue(assignStmtPass("falseBool", false))
        assertTrue(assignStmtPass("trueBool", true))
    }

    /**
     * Tests whether parser recognises syntax errors on reserved words from symbol table
     */
    @Test
    fun parserReservedSymbolsTest() {
        RESERVED_WORDS.forEach {
            val parser = getParser(getWorldDeclString() + getStateDeclString(ident = it))
            // Stop parser from printing errors to stderr for less noisy console
            parser.removeErrorListeners()
            // Parse program
            parser.start()

            // Assert that syntax-errors are recognised for reserved words
            assertTrue(parser.numberOfSyntaxErrors > 0, "Test failed on: $it")
        }
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
    fun <T> symbolRedefinition(ident: String, values: List<T>): Boolean {
        val stringBuilder = StringBuilder()
        // For each value, create assignment expression with equal identifier and given value
        values.forEach {
            stringBuilder.appendln("let $ident = ${it.toString()};")
        }

        // Compile boilerplate program with state having the constructed body
        compileProgram(getWorldDeclString() + getStateDeclString(body = stringBuilder.toString()))

        try {
            // Assert that errors are registered
            assertTrue(ErrorLogger.hasErrors())

            // For each error recorded, assert that error is of SymbolRedefinitionError-type and with given identifier
            ErrorLogger.allErrors().forEach {
                assertTrue(
                    it is SymbolRedefinitionError,
                    "Class assertion error at: $it"
                ) // If class is not a SRE, ASE is thrown with message on error if 'is'-keyword assertion fails
                assertTrue(
                    (it as SymbolRedefinitionError).ident == ident,
                    "Identifier error at: $it"
                ) /* If identifier of SRE, which is the ident of the given symbol which is redefined,
                     is not equal to the actual ident passed, then assertion fails */
            }
        } catch (e: AssertionFailedError) {
            return false
        }
        return true
    }

    /**
     * Dispatches symbolRedefinition test on given parameter which should fail
     */
    @Test
    fun symbolRedefinitionFailTest() {
        assertTrue(
            symbolRedefinition("a", listOf(4, 2, true, 42.2.toFloat(), false)),
            "Test failed trying to redefine symbol"
        )
    }

    /**
     * Dispatches symbolRedefinition test on given parameter which should pass
     */
    @Test
    fun symbolRedefinitionPassTest() {
        assertFalse(symbolRedefinition("x", listOf(2)), "Test succeeded when expecting fail")
        assertFalse(symbolRedefinition("y", listOf(false)), "Test succeeded when expecting fail")
        assertFalse(symbolRedefinition("z", listOf(42.0.toFloat())), "Test succeeded when expecting fail")
    }


}