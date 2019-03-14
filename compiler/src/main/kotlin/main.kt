package dk.aau.cs.d409f19.cellumata

import cs.aau.dk.d409f19.antlr.*
import dk.aau.cs.d409f19.cellumata.ast.visit
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream

fun main() {
    val inputStream = ANTLRInputStream("world {\n" +
            "size = 10[wrap], 10[edge=foo]\n" +
            "tickrate = 10\n" +
            "}\n" +
            "const a = 123\n" +
            "state asdf(1,2,3) {}\n"/* +
            "neighbourhood n {(0,0)}"*/)
    val lexer = CellmataLexer(inputStream)
    val tokenStream = CommonTokenStream(lexer)
    val parser = CellmataParser(tokenStream)

    val startContext = parser.start()

    val ast = visit(startContext)

    println(ast)
}