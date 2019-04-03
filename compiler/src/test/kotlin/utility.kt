package dk.aau.cs.d409f19

import dk.aau.cs.d409f19.cellumata.ErrorLogger
import dk.aau.cs.d409f19.cellumata.ast.*
import dk.aau.cs.d409f19.cellumata.walkers.LiteralExtractorVisitor
import dk.aau.cs.d409f19.cellumata.walkers.ScopeCheckVisitor
import dk.aau.cs.d409f19.cellumata.walkers.TypeChecker
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TestUtilities {

    /**
     * Compiles a program given as string parameter, asserts that the given program compiles successfully
     */
    fun CompileProgram(program: String): AST {
        val source = CharStreams.fromString(program)
        val lexer = dk.aau.cs.d409f19.antlr.CellmataLexer(source)
        val tokenStream = CommonTokenStream(lexer)
        val parser = dk.aau.cs.d409f19.antlr.CellmataParser(tokenStream)

        // Build AST
        val startContext = parser.start()
        val ast = reduce(startContext)
        // Asserts that no errors has been found during the last phase, if any are found throw exception
        ErrorLogger.assertNoErrors()

        // Extract literals
        LiteralExtractorVisitor().visit(ast)

        // Symbol table and scope
        val scopeChecker = ScopeCheckVisitor()
        scopeChecker.visit(ast)
        val symbolTable = scopeChecker.getSymbolTable()
        // Asserts that no errors has been found during the last phase, if any are found throw exception
        ErrorLogger.assertNoErrors()

        // Type checking
        TypeChecker(symbolTable).visit(ast)
        // Asserts that no errors has been found during the last phase, if any are found throw exception
        ErrorLogger.assertNoErrors()

        // Assert that no errors occured
        assertFalse(ErrorLogger.hasErrors())
        return ast
    }

    /**
     * Takes a program as a string and returns the parser for testing errors
     */
    fun GetParser(program: String): dk.aau.cs.d409f19.antlr.CellmataParser {
        val source = CharStreams.fromString(program)
        val lexer = dk.aau.cs.d409f19.antlr.CellmataLexer(source)
        val tokenStream = CommonTokenStream(lexer)
        return dk.aau.cs.d409f19.antlr.CellmataParser(tokenStream)
    }

    /**
     * Returns a world declaration based on given parameters and with sane defaults
     * TODO: should handle optional, second dimension - as of now, it's required
     */
    fun getWorldDecl(
        dimOneSize: Int = 10,
        dimOneType: String = "wrap",
        dimTwoSize: Int = 20,
        dimTwoType: String = "wrap",
        tickrate: Int = 120,
        cellsize: Int = 5
    ): String {
        return """world {
            |  size = $dimOneSize[$dimOneType], $dimTwoSize[$dimTwoType]
            |  tickrate = $tickrate
            |  cellsize = $cellsize
            |}
            |
            | """.trimMargin()
    }

    /**
     * Returns a constant declaration based on given parameters and with sane defaults
     */
    fun getConstDecl(ident: String = "ident", value: String = "false"): String {
        return "const $ident = $value;"
    }

    /**
     * Returns a state array declaration based on given parameters and with sane defaults
     * TODO: stateArray is default empty as compiler on master-branch did not support this yet when branching out
     */
    fun getStateDecl(
        ident: String = "stage",
        stateArray: String = "",
        red: Int = 255,
        green: Int = 200,
        blue: Int = 100,
        body: String = "",
        become: String = "become $ident;"
    ): String {
        return """state $ident $stateArray ($red, $green, $blue) {
            |  $body
            |  $become
            |}
            |""".trimMargin()
    }

    /**
     * Returns a boilerplate program
     */
    fun getBoilerplate(): String {
        return getWorldDecl() + getConstDecl() + "\n\n" + getStateDecl()
    }

    /**
     * Compile boilerplate program from defaults
     */
    @Test
    fun BoilerplateTest() {
        // Get boilerplate program, print, and compile it
        val program = getBoilerplate()
        print(program)
        val ast = CompileProgram(program)

        // Casting ast to RootNode
        val rootNode = ast as RootNode
        // TODO: Should be refactored to actual values when compiler gains ability
        assertNull(rootNode.world.cellSize)
        assertNull(rootNode.world.tickrate)

        // Cast first body of rootNode to ConstDecl
        val const = rootNode.body.get(0) as ConstDecl
        // Assert that identifier of constant is default; "ident"
        assertEquals("ident", const.ident)

        // Cast expression of constant-node to BoolLiteral
        val constExpr = const.expr as BoolLiteral
        // Assert value and type of constant
        assertFalse(constExpr.value)
        assertTrue(const.type is BooleanType)

        // Second body of RootNode should be StateDecl
        val state = rootNode.body.get(1) as StateDecl
        // Assert identifier and colour-declaration
        assertEquals("stage", state.ident)
        assertEquals(255, state.red)
        assertEquals(200, state.green)
        assertEquals(100, state.blue)

        // Cast first element of state-body to BecomeStmt, then to NamedExpr, and assert identifier of BecomeStmt
        val becomeStmt = state.body.get(0) as BecomeStmt
        val becomeStmtNamedExpr = becomeStmt.state as NamedExpr
        assertEquals("stage", becomeStmtNamedExpr.ident)
    }
}