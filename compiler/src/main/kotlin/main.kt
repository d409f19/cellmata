package dk.aau.cs.d409f19.cellumata

import dk.aau.cs.d409f19.antlr.CellmataLexer
import dk.aau.cs.d409f19.antlr.CellmataParser
import dk.aau.cs.d409f19.cellumata.ast.AST
import dk.aau.cs.d409f19.cellumata.ast.Table
import dk.aau.cs.d409f19.cellumata.ast.reduce
import dk.aau.cs.d409f19.cellumata.visitors.ASTGrapher
import dk.aau.cs.d409f19.cellumata.visitors.SanityChecker
import dk.aau.cs.d409f19.cellumata.visitors.ScopeCheckVisitor
import dk.aau.cs.d409f19.cellumata.visitors.TypeChecker
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.nio.file.Path
import java.nio.file.Paths
import java.io.File

val path = Paths.get("src/main/resources/compiling-programs/skeleton.cell")

fun compile(path: Path) {
    try {
        val inputStream = CharStreams.fromPath(path)
        val lexer = CellmataLexer(inputStream)
        val tokenStream = CommonTokenStream(lexer)
        val parser = CellmataParser(tokenStream)

        // Build AST
        val startContext = parser.start()
        val ast = reduce(startContext)
        // Asserts that no errors has been found during the last phase
        ErrorLogger.assertNoErrors()

        // Sanity checker
        val sanityChecker = SanityChecker()
        sanityChecker.visit(ast)

        // Symbol table and scope
        val scopeChecker = ScopeCheckVisitor()
        scopeChecker.visit(ast)
        val symbolTable = scopeChecker.getSymbolTable()
        println(symbolTable)
        ErrorLogger.assertNoErrors()

        // Type checking
        TypeChecker(symbolTable).visit(ast)
        ErrorLogger.assertNoErrors()

        File("ast.gs").outputStream().use { out -> ASTGrapher(out).visit(ast) }
    } catch (e: TerminatedCompilationException) {

        // Compilation failed due to errors in program code
        System.err.println("Compilation failed: ${e.message}")
        ErrorLogger.printAllErrors(path)

    } catch (e: Exception) {

        // Critical error happened, maybe something is be wrong in the compiler
        // Printing stack trace and errors for debugging reasons
        e.printStackTrace()
        System.err.println("Critical error occurred. Maybe something is wrong in the compiler. Emptying ErrorLogger:")
        ErrorLogger.printAllErrors(path)
    }
}

fun main() {
    compile(path)
}

/**
 * Encapsulates all data from the compiler, which may be used for testing
 */
data class CompilerData(
    val parser: CellmataParser,
    val ast: AST,
    val symbolTable: Table
)