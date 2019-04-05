package dk.aau.cs.d409f19

import dk.aau.cs.d409f19.cellumata.ast.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Assumptions.*
import org.junit.jupiter.api.Test
import org.opentest4j.AssertionFailedError

class General {

    /**
     * Test boilerplate world declaration AST
     */
    private fun worldDecl(): Boolean {
        // Get AST of  boilerplate program with only world declaration
        val ast = Utilities.compileProgram(Utilities.getWorldDecl())
        // Casting ast to RootNode
        val rootNode = ast as RootNode

        // Try asserting values, return whether succeeded or failed
        try {
            // Assert world values
            assertEquals(10, rootNode.world.dimensions[0].size)
            assertEquals(20, rootNode.world.dimensions[1].size)
            assertEquals(WorldType.WRAPPING, rootNode.world.dimensions[0].type)
            assertEquals(WorldType.WRAPPING, rootNode.world.dimensions[1].type)
            assertEquals(5, rootNode.world.cellSize)
            assertEquals(120, rootNode.world.tickrate)
        } catch (e: AssertionFailedError) {
            return false
        }
        return true
    }

    @Test
    fun worldDeclTest() {
        // Assert worldDecl compiles and asserts true throughout
        assertTrue(worldDecl())
    }

    /**
     * Test boilerplate world and constant declaration AST
     */
    private fun constDecl(): Boolean {
        // Assume that worldDeclTest passes
        assumeTrue(worldDecl())

        // Get AST for boilerplate program with only world declaration and constant declaration
        val ast = Utilities.compileProgram(Utilities.getWorldDecl() + "\n\n" + Utilities.getConstDecl())

        try {
            // Cast first body of rootNode to ConstDecl
            val const = (ast as RootNode).body[0] as ConstDecl
            // Assert that identifier of constant is default; "ident"
            assertEquals("ident", const.ident)

            // Cast expression of constant-node to BoolLiteral
            val constExpr = const.expr as BoolLiteral
            // Assert value and type of constant
            assertFalse(constExpr.value)
            assertTrue(const.type is BooleanType)
        } catch (e: AssertionFailedError) {
            return false
        }
        return true
    }

    @Test
    fun constDeclTest() {
        // Assert constDecl compiles and asserts true throughout
        assertTrue(constDecl())
    }

    /**
     * Test boilerplate program AST
     */
    private fun stateDecl(): Boolean {
        // Assume that constDeclTest passes
        assumeTrue(constDecl())

        // Get AST for boilerplate program
        val ast = Utilities.compileProgram(Utilities.getBoilerplate())

        try {
            // Second body of RootNode should be StateDecl
            val state = (ast as RootNode).body[1] as StateDecl
            // Assert identifier and colour-declaration
            assertEquals("stage", state.ident)
            assertEquals(255, state.red)
            assertEquals(200, state.green)
            assertEquals(100, state.blue)

            // Cast first element of state-body to BecomeStmt, then to NamedExpr, and assert identifier of BecomeStmt
            val becomeStmtNamedExpr = (state.body[0] as BecomeStmt).state as NamedExpr
            assertEquals("stage", becomeStmtNamedExpr.ident)
        } catch (e: AssertionFailedError) {
            return false
        }
        return true
    }

    @Test
    fun stateDeclTest() {
        // Assert stateDecl compiles and asserts true throughout
        assertTrue(stateDecl())
    }
}