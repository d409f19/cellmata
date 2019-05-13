package dk.aau.cs.d409f19.cellumata

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.SystemExitException
import com.xenomachina.argparser.default
import java.nio.file.Path

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
    ).default("minimal").addValidator {
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