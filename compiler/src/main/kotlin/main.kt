package dk.aau.cs.d409f19.cellumata

import dk.aau.cs.d409f19.antlr.*
import dk.aau.cs.d409f19.cellumata.ast.reduce
import dk.aau.cs.d409f19.cellumata.ast.SymbolTable
import dk.aau.cs.d409f19.cellumata.walkers.LiteralExtractorVisitor
import dk.aau.cs.d409f19.cellumata.walkers.ScopeCheckVisitor
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

    val symbolTable = SymbolTable()
    ScopeCheckVisitor(symbolTable).visit(ast)
    println(symbolTable)
}