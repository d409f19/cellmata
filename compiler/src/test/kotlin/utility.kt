package dk.aau.cs.d409f19

import dk.aau.cs.d409f19.antlr.CellmataLexer
import dk.aau.cs.d409f19.antlr.CellmataParser
import dk.aau.cs.d409f19.cellumata.CompilerData
import dk.aau.cs.d409f19.cellumata.CompilerSettings
import dk.aau.cs.d409f19.cellumata.ErrorLogger
import dk.aau.cs.d409f19.cellumata.ast.reduce
import dk.aau.cs.d409f19.cellumata.compile
import dk.aau.cs.d409f19.cellumata.visitors.FlowChecker
import dk.aau.cs.d409f19.cellumata.visitors.SanityChecker
import dk.aau.cs.d409f19.cellumata.visitors.ScopeCheckVisitor
import dk.aau.cs.d409f19.cellumata.visitors.TypeChecker
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

/**
 * Compile a Cellmata program given as string parameter
 */
fun compileTestProgram(program: String, settings: CompilerSettings = CompilerSettings()): CompilerData {
    return compile(CharStreams.fromString(program), settings)
}

/**
 * Compile a Cellmata program insecurely given as string parameter and returns all compiler data.
 * Note that this function may yield strange exceptions and errors
 * as it DOES NOT assert for no errors between compiler phases.
 */
fun compileTestProgramInsecure(program: String): CompilerData {
    val lexer = CellmataLexer(CharStreams.fromString(program))
    val tokenStream = CommonTokenStream(lexer)
    val parser = CellmataParser(tokenStream)

    // Build AST
    val startContext = parser.start()
    val ast = reduce(startContext)

    // Sanity checker
    val sanityChecker = SanityChecker()
    sanityChecker.visit(ast)

    // Flow checking
    val flowChecker = FlowChecker()
    flowChecker.visit(ast)

    // Symbol table and scope
    val scopeChecker = ScopeCheckVisitor()
    scopeChecker.visit(ast)
    val symbolTable = scopeChecker.getSymbolTable()

    // Type checking
    TypeChecker(symbolTable).visit(ast)


    return CompilerData(parser, ast, symbolTable, ErrorLogger.hasErrors())
}

/**
 * Compile a Cellmata program insecurely given as string parameter and returns parser and AST as compiler data.
 * Note that this function may yield strange exceptions and errors
 * as it DOES NOT assert for no errors between compiler phases.
 */
fun compileTestProgramParserASTInsecure(program: String): CompilerData {
    val lexer = CellmataLexer(CharStreams.fromString(program))
    val tokenStream = CommonTokenStream(lexer)
    val parser = CellmataParser(tokenStream)

    // Build AST
    val startContext = parser.start()
    val ast = reduce(startContext)

    // Sanity checker
    val sanityChecker = SanityChecker()
    sanityChecker.visit(ast)
    return CompilerData(parser, ast, hasErrors = ErrorLogger.hasErrors())
}

/**
 * Compile a Cellmata program insecurely given as string parameter and returns only parser as compiler data.
 * Note that this function may yield strange exceptions and errors
 * as it DOES NOT assert for no errors between compiler phases
 */
fun compileTestProgramParserInsecure(program: String): CompilerData {
    val lexer = CellmataLexer(CharStreams.fromString(program))
    val tokenStream = CommonTokenStream(lexer)
    val parser = CellmataParser(tokenStream)

    return CompilerData(parser = parser, hasErrors = ErrorLogger.hasErrors())
}

/**
 * Sets up a Cellmata parser and returns it
 */
fun getParser(program: String): CellmataParser {
    val source = CharStreams.fromString(program)
    val lexer = CellmataLexer(source)
    val tokenStream = CommonTokenStream(lexer)
    return CellmataParser(tokenStream)
}

/**
 * Returns a Cellmata world declaration as a string based on given parameters and with sane defaults
 * Defaults to two dimensional, but can be overridden with the twoDimensional parameter
 */
fun getWorldDeclString(
    dimOneSize: String = "10",
    dimOneType: String = "wrap",
    dimTwoSize: String = "20",
    dimTwoType: String = "wrap",
    tickrate: String = "120",
    cellsize: String = "4",
    twoDimensional: Boolean? = false
): String {
    // If two dimensional, declare second dimension, else empty string
    val twoDimDecl = if (twoDimensional!!) ", $dimTwoSize[$dimTwoType]" else ""
    return """world {
            |  size = $dimOneSize[$dimOneType]$twoDimDecl
            |  tickrate = $tickrate
            |  cellsize = $cellsize
            |}
            |
            | """.trimMargin()
}

/**
 * Returns a Cellmata constant declaration as a string based on given parameters and with sane defaults
 */
fun getConstDeclString(ident: String = "ident", value: String = "false"): String {
    return "const $ident = $value;"
}

/**
 * Returns a Cellmata state array declaration as a string based on given parameters and with sane defaults
 * TODO: stateArray is default empty as compiler on master-branch did not support this yet when branching out
 */
fun getStateDeclString(
    ident: String = "stage",
    stateArray: String = "",
    red: Int = 255,
    green: Int = 200,
    blue: Int = 100,
    body: String = "",
    become: String = "become $ident;"
): String {
    return """state $ident $stateArray ($red, $green, $blue) {
            |  $body
            |  $become
            |}
            |""".trimMargin()
}

/**
 * Returns a boilerplate Cellmata program as a string
 */
fun getBoilerplateProgramString(): String {
    return getWorldDeclString() + getConstDeclString() + "\n\n" + getStateDeclString()
}
