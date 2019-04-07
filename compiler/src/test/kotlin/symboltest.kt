package dk.aau.cs.d409f19

import dk.aau.cs.d409f19.cellumata.ast.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.opentest4j.AssertionFailedError

class SymbolTest {

    /**
     * Tests assignments statements which should be legal, given an identifier and value as parameter.
     * If assertions fails, exceptions are caught, and false is returned to indicate a failed test to caller.
     */
    private fun <T> assignStmtPass(ident: String, value: T): Boolean {

        val compilerData =
            Utilities.compileProgram(Utilities.getWorldDecl() + "\n\n" + Utilities.getStateDecl(body = "let $ident = $value;"))

        // Get SymbolTable for entire program
        val symbolTable = compilerData.scopeChecker.getSymbolTable()
        // Get SymbolTable for first subscope, which is first StateDecl
        val stateSymbolTable = symbolTable.tables[0]
        // Get expression of AssignStmt which is value of the key of ident
        val literal = (stateSymbolTable.symbols[ident] as AssignStmt).expr

        try {
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
        }
        return true
    }

    /**
     * Dispatches assignment statement tests
     */
    @Test
    fun assignStmtTest() {
        assertTrue(assignStmtPass("x", 42))
        assertTrue(assignStmtPass("integer", 16368))
        assertTrue(assignStmtPass("falseBool", false))
        assertTrue(assignStmtPass("trueBool", true))
    }
}