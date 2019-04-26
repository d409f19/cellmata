package dk.aau.cs.d409f19.cellumata

import dk.aau.cs.d409f19.antlr.CellmataLexer
import dk.aau.cs.d409f19.antlr.CellmataParser
import dk.aau.cs.d409f19.cellumata.ast.AST
import dk.aau.cs.d409f19.cellumata.ast.Table
import dk.aau.cs.d409f19.cellumata.ast.reduce
import dk.aau.cs.d409f19.cellumata.visitors.SanityChecker
import dk.aau.cs.d409f19.cellumata.visitors.ScopeCheckVisitor
import dk.aau.cs.d409f19.cellumata.visitors.TypeChecker
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

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
        ErrorLogger.assertNoErrors()

        // Type checking
        TypeChecker(symbolTable).visit(ast)
        ErrorLogger.assertNoErrors()

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

fun main(args: Array<String>) {

    // Check the arguments
    if (args.size < 1) {
        println("Too few arguments. Expected a path to a '.cell' file.")

    } else {
        val path = Paths.get(args[0])

        // Check if file is a .cell file and if it exists
        if (path.extention() != "cell") {
            println("Given path is not a '.cell' file.")

        } else if (!Files.exists(path)) {
            println("Could not find $path. Does the file exist?")

        } else {

            // Read other arguments
            for (i in 1 until args.size) {
                val arg = args[i]
                when (arg) {
                    // Current we have no options
                    else -> { println("'$arg' is not a valid option."); return }
                }
            }

            compile(path)
        }
    }
}

/**
 * Encapsulates all data from the compiler, which may be used for testing
 */
data class CompilerData(
    val parser: CellmataParser,
    val ast: AST,
    val symbolTable: Table
)
