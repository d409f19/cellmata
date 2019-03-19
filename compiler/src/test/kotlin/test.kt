package dk.aau.cs.d409f19

import dk.aau.cs.d409f19.antlr.CellmataLexer
import dk.aau.cs.d409f19.antlr.CellmataParser
import dk.aau.cs.d409f19.cellumata.ast.visit
import org.antlr.v4.runtime.ANTLRFileStream
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.jupiter.api.Test

class CompilerTests {

    @Test
    fun `Run compiler`() {
        val inputStream = ANTLRFileStream("src/main/resources/stress.cell")
        val lexer = CellmataLexer(inputStream)
        val tokenStream = CommonTokenStream(lexer)
        val parser = CellmataParser(tokenStream)

        val startContext = parser.start()

        val ast = visit(startContext)
    }
}