package dk.aau.cs.d409f19

import dk.aau.cs.d409f19.cellumata.CompilerData
import dk.aau.cs.d409f19.cellumata.ErrorLogger
import dk.aau.cs.d409f19.cellumata.ast.*
import dk.aau.cs.d409f19.cellumata.walkers.LiteralExtractorVisitor
import dk.aau.cs.d409f19.cellumata.walkers.ScopeCheckVisitor
import dk.aau.cs.d409f19.cellumata.walkers.TypeChecker
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class Utilities {

    companion object {

        /**
         * Compiles a program given as string parameter, asserts that the given program compiles successfully
         */
        fun compileProgram(program: String): CompilerData {
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
            return CompilerData(parser, ast, scopeChecker)
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
    }

    /**
     * Returns a boilerplate program
     */
    fun getBoilerplate(): String {
        return getWorldDecl() + getConstDecl() + "\n\n" + getStateDecl()
    }
}