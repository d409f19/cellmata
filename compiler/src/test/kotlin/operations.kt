package dk.aau.cs.d409f19

import dk.aau.cs.d409f19.cellumata.ErrorLogger
import dk.aau.cs.d409f19.cellumata.ast.reduce
import dk.aau.cs.d409f19.cellumata.walkers.LiteralExtractorVisitor
import dk.aau.cs.d409f19.cellumata.walkers.ScopeCheckVisitor
import dk.aau.cs.d409f19.cellumata.walkers.TypeChecker
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class OperationTests {
    
    /**
     * Compiles a program given as string parameter, asserts that the given program compiles successfully
     */
    fun CompileProgram(program: String) {
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
        Assertions.assertFalse(ErrorLogger.hasErrors())
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
     * Returns a boilerplate program with optionally inserted line
     */
    fun getBoilerplate(source: String? = ""): String {
        return "world { size = 10[wrap], 20[wrap] tickrate = 120 cellsize = 5} $source state foo (0,0,0) { let y = 42; become foo; }"
    }

    /**
     * Compile boilerplate program
     */
    @Test
    fun BoilerplateTest() {
        CompileProgram(getBoilerplate())
    }

    @Test
    fun AdditionTest1() {
        CompileProgram(getBoilerplate("const bar = 2 + 42;"))
    }
}