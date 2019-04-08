package dk.aau.cs.d409f19.cellumata

import org.antlr.v4.runtime.ParserRuleContext
import java.nio.file.Files
import java.nio.file.Path

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
    abstract fun getLineNumber(): Int

    /**
     * The index of the first character of this error relative to the
     * beginning of the line at which it occurs, 0..n-1
     */
    abstract fun getCharPositionInLine(): Int
}

/**
 * An compiler error based on the context from the antlr parser.
 */
open class ErrorFromContext(val ctx: ParserRuleContext, private val description: String) : CompileError(description) {

    override fun description(): String {
        return description
    }

    override fun getLineNumber(): Int {
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
    fun printAllErrors(path: Path) {

        val lines = Files.lines(path)
        val sortedErrors = errors.sortedWith(compareBy<CompileError> { it.getLineNumber() }.thenBy { it.getCharPositionInLine() })

        // Line index initialised to 1, as lines are not zero-indexed
        var currentLineIndex = 1
        var currentErrorIndex = 0

        // Find the line where the errors occurred so they can be printed
        for (line in lines) {
            var error = sortedErrors[currentErrorIndex]

            while (error.getLineNumber() == currentLineIndex) {

                // Print the error
                System.err.println("Error at (${error.getLineNumber()}, ${error.getCharPositionInLine()}): ${error.description()}")
                System.err.println(line) // print the line
                System.err.println(errorPointerString(error.getCharPositionInLine()))

                // Check next error. It might be on the same line
                currentErrorIndex++

                if (currentErrorIndex == sortedErrors.size) {
                    return // No more errors. We are done
                }

                error = sortedErrors[currentErrorIndex]
            }
            currentLineIndex++
        }
    }

    fun printAllErrors() {
        val sortedErrors = errors.sortedWith(compareBy<CompileError> { it.getLineNumber() }.thenBy { it.getCharPositionInLine() })

        sortedErrors.forEach() {
            System.err.println("Error at (${it.getLineNumber()}, ${it.getCharPositionInLine()}): ${it.description()}")
        }

    }

    fun allErrors(): List<CompileError> {
        return errors
    }

    fun reset() {
        errors.clear()
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