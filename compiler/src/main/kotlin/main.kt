package dk.aau.cs.d409f19.cellumata

import dk.aau.cs.d409f19.antlr.CellmataLexer
import dk.aau.cs.d409f19.antlr.CellmataParser
import dk.aau.cs.d409f19.cellumata.ast.reduce
import dk.aau.cs.d409f19.cellumata.walkers.ASTGrapher
import dk.aau.cs.d409f19.cellumata.walkers.LiteralExtractorVisitor
import dk.aau.cs.d409f19.cellumata.walkers.ScopeCheckVisitor
import dk.aau.cs.d409f19.cellumata.walkers.TypeChecker
import org.antlr.v4.runtime.ANTLRFileStream
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File

fun main() {
    // Read from stress.cell
    val inputStream = ANTLRFileStream("src/main/resources/stress.cell")
    // Setup lexer
    val lexer = CellmataLexer(inputStream)
    val tokenStream = CommonTokenStream(lexer)
    // Setup parser
    val parser = CellmataParser(tokenStream)

    // Run the lexer and parser
    val startContext = parser.start()

    val ast = reduce(startContext)

    println(ast)

    LiteralExtractorVisitor().visit(ast)

    val scopeChecker = ScopeCheckVisitor()
    scopeChecker.visit(ast)
    val symbolTable = scopeChecker.getSymbolTable()
    println(symbolTable)

    // Run the type checker
    TypeChecker(symbolTable).visit(ast)

    File("ast.gs").outputStream().use { out -> ASTGrapher(out).visit(ast) }
}