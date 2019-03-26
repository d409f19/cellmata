package dk.aau.cs.d409f19.cellumata

import dk.aau.cs.d409f19.antlr.*
import dk.aau.cs.d409f19.cellumata.ast.SymbolTable
import dk.aau.cs.d409f19.cellumata.ast.visit
import org.antlr.v4.runtime.CharStreams
import dk.aau.cs.d409f19.cellumata.walkers.LiteralExtractorVisitor
import dk.aau.cs.d409f19.cellumata.walkers.ScopeCheckVisitor
import org.antlr.v4.runtime.CommonTokenStream
import java.lang.Exception
import java.nio.file.Paths

fun main() {

    val path = Paths.get("src/main/resources/stress.cell")

    try {
        val inputStream = CharStreams.fromPath(path)
        val lexer = CellmataLexer(inputStream)
        val tokenStream = CommonTokenStream(lexer)
        val parser = CellmataParser(tokenStream)

        // Build AST
        val startContext = parser.start()
        val ast = visit(startContext)
        println(ast)
        ErrorLogger.assertNoErrors()

        // Literals
        LiteralExtractorVisitor().visit(ast)

        // Symbol table and scope
        val symbolTable = SymbolTable()
        ScopeCheckVisitor(symbolTable).visit(ast)
        println(symbolTable)
        ErrorLogger.assertNoErrors()

    } catch (e:  TerminatedCompilationException) {

        println("Compilation failed: ${e.message}")
        ErrorLogger.printAllErrors()

    } catch (e: Exception) {
        e.printStackTrace()
    }
}