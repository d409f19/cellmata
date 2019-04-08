package dk.aau.cs.d409f19

import dk.aau.cs.d409f19.Utilities.Companion.compileProgram
import dk.aau.cs.d409f19.Utilities.Companion.getParser
import dk.aau.cs.d409f19.Utilities.Companion.getStateDecl
import dk.aau.cs.d409f19.Utilities.Companion.getWorldDecl
import dk.aau.cs.d409f19.cellumata.ast.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.opentest4j.AssertionFailedError
import java.lang.NullPointerException

class SymbolTest {

    /**
     * Tests assignments statements which should be legal, given an identifier and value as parameter.
     * If assertions fails, exceptions are caught, and false is returned to indicate a failed test to caller.
     */
    private fun <T> assignStmtPass(ident: String, value: T): Boolean {

        val compilerData = compileProgram(getWorldDecl() + "\n\n" + getStateDecl(body = "let $ident = $value;"))

        // Get SymbolTable for entire program
        val symbolTable = compilerData.scopeChecker.getSymbolTable()
        // Get SymbolTable for first subscope, which is first StateDecl
        val stateSymbolTable = symbolTable.tables[0]

        try {
            // Get expression of AssignStmt which is value of the key of ident
            val literal = (stateSymbolTable.symbols[ident] as AssignStmt).expr

            // Assert identifier of variable is contained in first symbol scope
            assertTrue(stateSymbolTable.symbols.containsKey(ident))

            // Switch on literal type, when either three assignable literals, assert equal value, else throw exception
            when (literal) {
                is IntLiteral -> assertEquals(value, literal.value)
                is BoolLiteral -> assertEquals(value, literal.value)
                is FloatLiteral -> assertEquals(value, literal.value)
                else -> throw AssertionFailedError("Type was not of assignable type! Was " + literal.getType())
            }
        } catch (e: AssertionFailedError) { // If any assertion errors thrown, catch and return false
            return false
        } catch (e: NullPointerException) { // Catch NPE's which may be thrown due to looking up ident as key
            e.printStackTrace()
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
        RESERVED_SYMBOLS.forEach {
            val parser = getParser(getWorldDecl() + getStateDecl(ident = it))
            // Stop parser from printing errors to stderr for less noisy console
            parser.removeErrorListeners()
            // Parse program
            parser.start()

            // Assert that syntax-errors are recognised for reserved words
            assertTrue(parser.numberOfSyntaxErrors > 0, "Test failed on: $it")
        }
    }
}