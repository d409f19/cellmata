package dk.aau.cs.d409f19.cellumata

import com.xenomachina.argparser.*
import dk.aau.cs.d409f19.antlr.CellmataLexer
import dk.aau.cs.d409f19.antlr.CellmataParser
import dk.aau.cs.d409f19.cellumata.ast.AST
import dk.aau.cs.d409f19.cellumata.ast.Table
import dk.aau.cs.d409f19.cellumata.ast.reduce
import dk.aau.cs.d409f19.cellumata.visitors.*
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File
import java.io.OutputStreamWriter
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

enum class CompileTarget {
    KOTLIN,
    CELLMATA
}

enum class GraphPhases {
    REDUCE,
    SANITY,
    FLOW,
    SCOPE,
    TYPE
}

enum class LogLevel {
    SILENT,
    MINIMAL,
    DEBUG,
    VERBOSE
}

class Arguments(parser: ArgParser) {
    val verbose by parser.flagging(
        "-v", "--verbose",
        help = "enable verbose mode"
    )

    val logLevel by parser.storing(
        "--log-level",
        help = "level of details to log [silent,minimal,debug,verbose]"
    ).default("silent").addValidator {
        if(!listOf("silent", "minimal", "debug", "verbose").contains(this.value)) {
            throw SystemExitException("Invalid log level", 1)
        }
    }

    val debugAstPhases by parser.storing(
        "--debug-ast-phase",
        help = "comma seperated list of phases to save AST graph from [reduce,sanity,scope,type]"
    ){ split(",") }
        .default(listOf())
        .addValidator {
        this.value.forEach {
            if(!listOf("reduce", "sanity","flow", "scope", "type").contains(it)) {
                throw SystemExitException("Invalid phase specified for graphing", 1)
            }
        }
    }

    val debugInfoDir by parser.storing(
        "--debug-info-dir",
        help = "directory to save debug info in"
    ).default("./debug")

    val outputDir by parser.storing(
        "-o", "--output",
        help = "location to save compiled program to"
    )

    val target by parser.storing(
        "-t", "--target",
        help = "target to compile to"
    ).default("kotlin").addValidator {
        if(!listOf("kotlin", "cellmata").contains(this.value)) {
            throw SystemExitException("Invalid target", 1)
        }
    }

    val source by parser.positional(
        "SOURCE",
        help = "source filename"
    )
}

data class CompilerSettings(
    val logLevel: LogLevel = LogLevel.SILENT,
    val graphPhases: List<GraphPhases> = listOf(),
    val target: CompileTarget,
    val source: Path,
    val output: Path
)

fun main(args: Array<String>) {
    try {
        ArgParser(
            args,
            helpFormatter = DefaultHelpFormatter(),
            mode = ArgParser.Mode.GNU
        ).parseInto(::Arguments)
            .run {
                val settings = CompilerSettings(
                    target = when(target) {
                        "kotlin" -> CompileTarget.KOTLIN
                        "cellmata" -> CompileTarget.CELLMATA
                        else -> throw SystemExitException("Internal error", 255)
                    },
                    source = Paths.get(source),
                    output = Paths.get(outputDir)
                )

                if (!Files.exists(settings.source)) {
                    throw SystemExitException("source file doesn't exist", 2)
                }


                prodCompilation(settings)
            }
    } catch (e: ShowHelpException) {
        val writer = OutputStreamWriter(System.out)
        e.printUserMessage(writer, "cellmatac", 0)
        writer.flush()
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

    graphAst(settings, "reduce", GraphPhases.REDUCE, ast)

    // Sanity checker
    val sanityChecker = SanityChecker()
    sanityChecker.visit(ast)

    graphAst(settings, "sanity-checker", GraphPhases.SANITY, ast)

    // Flow checking
    val flowChecker = FlowChecker()
    flowChecker.visit(ast)
    ErrorLogger.assertNoErrors()

    graphAst(settings, "flow-checker", GraphPhases.FLOW, ast)

    // Symbol table and scope
    val scopeChecker = ScopeCheckVisitor()
    scopeChecker.visit(ast)
    val symbolTable = scopeChecker.getSymbolTable()
    ErrorLogger.assertNoErrors()

    graphAst(settings, "scope-checker", GraphPhases.SCOPE, ast)

    // Type checking
    TypeChecker(symbolTable).visit(ast)
    ErrorLogger.assertNoErrors()

    graphAst(settings, "type-checker", GraphPhases.TYPE, ast)

    // Codegen
    when (settings.target) {
        CompileTarget.KOTLIN -> {
            val compiled = KotlinCodegen().visit(ast)
            Files.write(
                settings.output,
                compiled.toByteArray(Charset.forName("UTF-8")),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
            )
        }
        CompileTarget.CELLMATA -> {
            PrettyPrinter().print(ast)
        }
    }

    return CompilerData(parser, ast, symbolTable, ErrorLogger.hasErrors())
}

fun graphAst(settings: CompilerSettings, name: String, phase: GraphPhases, ast: AST) {
    if (settings.graphPhases.contains(phase)) {
        File(Paths.get(settings.output.toAbsolutePath().toString(), "ast-$name.gs").toUri())
            .outputStream()
            .use { out -> ASTGrapher(out).visit(ast) }
    }
}

/**
 * Production compile function, outputs useful information to user on errors in source and possibly the compiler itself
 */
fun prodCompilation(settings: CompilerSettings) {
    try {
        compile(CharStreams.fromPath(settings.source), settings)
        ErrorLogger.printAllWarnings(if (settings.logLevel == LogLevel.DEBUG) Files.lines(settings.source) else null)

    } catch (e: TerminatedCompilationException) {

        // Compilation failed due to errors in program code
        System.err.println("Compilation failed: ${e.message}")
        ErrorLogger.printAllWarnings(if (settings.logLevel == LogLevel.DEBUG) Files.lines(settings.source) else null)
        ErrorLogger.printAllErrors(if (settings.logLevel == LogLevel.MINIMAL) Files.lines(settings.source) else null)

    } catch (e: Exception) {

        // Printing stack trace and errors for debugging purposes
        e.printStackTrace()
        System.err.println("Critical error occurred. Maybe something is wrong in the compiler. Emptying ErrorLogger:")
        ErrorLogger.printAllWarnings(if (settings.logLevel == LogLevel.DEBUG) Files.lines(settings.source) else null)
        ErrorLogger.printAllErrors(if (settings.logLevel == LogLevel.MINIMAL) Files.lines(settings.source) else null)
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
