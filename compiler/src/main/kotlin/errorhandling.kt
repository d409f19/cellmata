package dk.aau.cs.d409f19.cellumata

import org.antlr.v4.runtime.ParserRuleContext
import java.nio.file.Files
import java.nio.file.Path

/**
 * Interface for all compiler errors that does not terminate a compiler phase.
 * @see ErrorLogger
 */
interface CompileError {

    /**
     * The description of the error in plain text
     */
    fun message(): String

    /**
     * The line number on which the first character that produced this error, line=1..n
     */
    fun getLine(): Int

    /**
     * The index of the first character of this error relative to the
     * beginning of the line at which it occurs, 0..n-1
     */
    fun getCharPositionInLine(): Int
}

/**
 * An compiler error based on the context from the antlr parser.
 */
class ErrorFromContext(private val ctx: ParserRuleContext, private val message: String) : CompileError {

    override fun message(): String {
        return message
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

    fun assertNoErrors() {
        if (hasErrors()) {
            throw TerminatedCompilationException("Errors occurred.")
        }
    }

    fun hasErrors(): Boolean {
        return errors.size > 0
    }

    fun printAllErrors() {
        for (e in errors) {
            println("Error at (${e.getLine()}, ${e.getCharPositionInLine()}): ${e.message()}")
        }
    }
}

/**
 * When a critical error occurs, since error is thrown to terminate the compilation immediately. This differs from
 * ErrorLogger and it's CompileErrors since multiple of these can occur before the compilation terminates.
 */
class TerminatedCompilationException(msg: String) : RuntimeException(msg)