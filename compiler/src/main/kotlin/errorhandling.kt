package dk.aau.cs.d409f19.cellumata

import org.antlr.v4.runtime.ParserRuleContext

/**
 * Interface for all compiler errors that does not terminate a compiler phase.
 * @see ErrorLogger
 */
abstract class CompileError(msg: String) : java.lang.RuntimeException(msg) {

    /**
     * The description of the error in plain text
     */
    abstract fun description(): String

    /**
     * The line number on which the first character that produced this error, line=1..n
     */
    abstract fun getLine(): Int

    /**
     * The index of the first character of this error relative to the
     * beginning of the line at which it occurs, 0..n-1
     */
    abstract fun getCharPositionInLine(): Int
}

/**
 * An compiler error based on the context from the antlr parser.
 */
open class ErrorFromContext(private val ctx: ParserRuleContext, private val description: String) : CompileError(description) {

    override fun description(): String {
        return description
    }

    override fun getLine(): Int {
        return ctx.start.line
    }

    override fun getCharPositionInLine(): Int {
        return ctx.start.charPositionInLine
    }
}

/**
 * The ErrorLogger holds all compile errors so far.
 */
object ErrorLogger {

    private val errors: MutableList<CompileError> = mutableListOf()

    fun registerError(err: CompileError) {
        errors += err
    }

    /**
     * Assert that there is no errors. Throws a TerminatedCompilationException if there is any errors.
     */
    fun assertNoErrors() {
        if (hasErrors()) {
            throw TerminatedCompilationException("Errors occurred.")
        }
    }

    fun hasErrors(): Boolean {
        return errors.size > 0
    }

    /**
     * Prints all errors in a nicely formatted way.
     */
    fun printAllErrors() {
        for (e in errors) {
            println("Error at (${e.getLine()}, ${e.getCharPositionInLine()}): ${e.description()}")
        }
    }

    fun allErrors(): List<CompileError> {
        return errors
    }

    /**
     * Returns a string consisting of a number of spaces followed by a ^. This string is used to point to the exact
     * position of the error in error messages. E.g. "      ^"
     */
    private fun errorPointerString(charPosition: Int): String {
        val pointerStringBuilder = StringBuilder()
        repeat(charPosition) {
            pointerStringBuilder.append(" ")
        }
        pointerStringBuilder.append("^")
        return pointerStringBuilder.toString()
    }
}

/**
 * When a critical error occurs, this error is thrown to terminate the compilation immediately. This differs from
 * the ErrorLogger and CompileErrors since multiple of these can occur before the compilation terminates.
 */
class TerminatedCompilationException(msg: String) : RuntimeException(msg)