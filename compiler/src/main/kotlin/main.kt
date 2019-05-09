package dk.aau.cs.d409f19.cellumata

import dk.aau.cs.d409f19.antlr.CellmataLexer
import dk.aau.cs.d409f19.antlr.CellmataParser
import dk.aau.cs.d409f19.cellumata.ast.AST
import dk.aau.cs.d409f19.cellumata.ast.RootNode
import dk.aau.cs.d409f19.cellumata.ast.Table
import dk.aau.cs.d409f19.cellumata.ast.reduce
import dk.aau.cs.d409f19.cellumata.interpreter.Interpreter
import dk.aau.cs.d409f19.cellumata.visitors.*
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

data class CompilerSettings(
    val verbose: Boolean = false,
    val veryVerbose: Boolean = false,
    val doPrettyPrinting: Boolean = false,
    val doGraphing: Boolean = false,
    val interpret: Boolean = false
)

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

            var verbose = false
            var veryVerbose = false
            var doPrettyPrinting = false
            var doGraph = false
            var interpret = false

            // Read other arguments
            for (i in 1 until args.size) {
                val arg = args[i]
                when (arg) {
                    "-v" -> verbose = true
                    "-vv" -> {
                        verbose = true
                        veryVerbose = true
                    }
                    "--pretty" -> doPrettyPrinting = true
                    "--graph" -> doGraph = true
                    "--interpret" -> interpret = true
                    else -> {
                        println("'$arg' is not a valid option."); return
                    }
                }
            }

            val settings = CompilerSettings(
                verbose,
                veryVerbose,
                doPrettyPrinting,
                doGraph,
                interpret
            )

            prodCompilation(path, settings)
        }
    }
}

/**
 * Compile program from given CharStream as input
 */
fun compile(source: CharStream, settings: CompilerSettings): CompilerData {
    val lexer = CellmataLexer(source)
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

    // Flow checking
    val flowChecker = FlowChecker()
    flowChecker.visit(ast)
    ErrorLogger.assertNoErrors()

    // Symbol table and scope
    val scopeChecker = ScopeCheckVisitor()
    scopeChecker.visit(ast)
    val symbolTable = scopeChecker.getSymbolTable()
    ErrorLogger.assertNoErrors()

    // Type checking
    TypeChecker(symbolTable).visit(ast)
    ErrorLogger.assertNoErrors()

    // Pretty printing
    if (settings.doPrettyPrinting) {
        PrettyPrinter().print(ast)
    }

    // Graph printing TODO add output file to settings
    if (settings.doGraphing) {
        File("ast.gs").outputStream().use { out -> ASTGrapher(out).visit(ast) }
    }

    if (settings.interpret) {
        Interpreter(ast as RootNode).start()
    }

    return CompilerData(parser, ast, symbolTable, ErrorLogger.hasErrors())
}

/**
 * Production compile function, outputs useful information to user on errors in source and possibly the compiler itself
 */
fun prodCompilation(sourcePath: Path, settings: CompilerSettings) {
    try {
        compile(CharStreams.fromPath(sourcePath), settings)
        ErrorLogger.printAllWarnings(if (settings.veryVerbose) Files.lines(sourcePath) else null)

    } catch (e: TerminatedCompilationException) {

        // Compilation failed due to errors in program code
        System.err.println("Compilation failed: ${e.message}")
        ErrorLogger.printAllWarnings(if (settings.veryVerbose) Files.lines(sourcePath) else null)
        ErrorLogger.printAllErrors(if (settings.verbose) Files.lines(sourcePath) else null)

    } catch (e: Exception) {

        // Printing stack trace and errors for debugging purposes
        e.printStackTrace()
        System.err.println("Critical error occurred. Maybe something is wrong in the compiler. Emptying ErrorLogger:")
        ErrorLogger.printAllWarnings(if (settings.veryVerbose) Files.lines(sourcePath) else null)
        ErrorLogger.printAllErrors(if (settings.verbose) Files.lines(sourcePath) else null)
    }
}

/**
 * Encapsulates all data from the compiler, which may be used for testing
 */
data class CompilerData(
    val parser: CellmataParser,
    val ast: AST? = null,
    val symbolTable: Table? = null,
    val hasErrors: Boolean? = null
)
