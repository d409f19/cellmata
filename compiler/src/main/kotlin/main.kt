package dk.aau.cs.d409f19.cellumata

import dk.aau.cs.d409f19.antlr.CellmataLexer
import dk.aau.cs.d409f19.antlr.CellmataParser
import dk.aau.cs.d409f19.cellumata.ast.reduce
import dk.aau.cs.d409f19.cellumata.walkers.LiteralExtractorVisitor
import dk.aau.cs.d409f19.cellumata.walkers.ScopeCheckVisitor
import dk.aau.cs.d409f19.cellumata.walkers.TypeChecker
import org.antlr.v4.runtime.ANTLRFileStream
import org.antlr.v4.runtime.CommonTokenStream

fun main() {
    val inputStream = ANTLRFileStream("src/main/resources/stress.cell")
    val lexer = CellmataLexer(inputStream)
    val tokenStream = CommonTokenStream(lexer)
    val parser = CellmataParser(tokenStream)

    val startContext = parser.start()

    val ast = reduce(startContext)

    println(ast)

    LiteralExtractorVisitor().visit(ast)

    val scopeChecker = ScopeCheckVisitor()
    scopeChecker.visit(ast)
    val symbolTable = scopeChecker.getSymbolTable()
    println(symbolTable)

    TypeChecker(symbolTable).visit(ast)
}