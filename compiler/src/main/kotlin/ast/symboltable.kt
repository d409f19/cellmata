package dk.aau.cs.d409f19.cellumata.ast

import dk.aau.cs.d409f19.cellumata.CompileError
import dk.aau.cs.d409f19.cellumata.ErrorLogger
import java.util.*

/**
 * A single table in the symbol table.
 * Table implements a nested design for a symbol table.
 *
 * @param symbols List of symbols defined in this scope
 * @param tables List of tables representing subscopes.
 */
data class Table(
    val symbols: MutableMap<String, AST> = mutableMapOf(),
    val tables: MutableList<Table> = mutableListOf()
)

/**
 * An error logged when there is an attempt to redefine an already defined symbol in the code that is being compiled.
 */
class SymbolRedefinitionError(ctx: SourceContext, val ident: String) :
    CompileError(ctx, "\"$ident\" is already defined")

/**
 * An error logged when there is an attempt to redefine a builtin function
 */
class BuiltinFunctionRedefinitionError(ctx: SourceContext, val ident: String) :
    CompileError(ctx, "Redefinition of builtin function: \"$ident\" is not allowed")

/**
 * An error logged when there is an attempt to use a reserved keyword
 */
class ReservedKeywordError(ctx: SourceContext, val ident: String) :
    CompileError(ctx, "Incorrect use of reserved keyword: \"$ident\"")

/**
 * List of language keywords that can't be used as identifiers
 */

val RESERVED_WORDS: List<String> = listOf(
    "world",
    "neighbourhood",
    "state",
    "function",
    "int",
    "float",
    "bool",
    "become",
    "return",
    "if",
    "elif",
    "else",
    "let",
    "for",
    "continue",
    "break"
)

/**
 * Assists in building and filling a symbol table
 */
class CreatingSymbolTableSession(symbolTable: Table) {
    private val scopeStack: Stack<Table> = Stack()

    init {
        scopeStack.push(Table(tables = mutableListOf(symbolTable))) // God scope
        scopeStack.push(symbolTable)
        preloadBuiltinFunctions()
    }

    /**
     * Create a new subscope in the current scope, and enter it
     */
    fun openScope() {
        val newScope = Table()
        scopeStack.peek().tables.add(newScope)
        scopeStack.push(newScope)
    }

    /**
     * Close the current scope and move to the parent scope
     */
    fun closeScope() {
        assert(scopeStack.size > 1) { "Tried to remove root scope from symbol table" }
        scopeStack.pop()
    }

    /**
     * Insert a new symbol in the current scope
     *
     * @throws SymbolRedefinitionError logged if identifier is already in use in the current scope or det identifier is a keyword
     */
    fun insertSymbol(ident: String, node: AST) {
        val table = scopeStack.peek()

        when {
            RESERVED_WORDS.contains(ident) -> ErrorLogger += ReservedKeywordError(node.ctx, ident)
            getSymbol(ident) is BuiltinFunc -> ErrorLogger += BuiltinFunctionRedefinitionError(node.ctx, ident)
            table.symbols.containsKey(ident) -> ErrorLogger += SymbolRedefinitionError(node.ctx, ident)
        }

        table.symbols[ident] = node
    }

    /**
     * Find a symbol in the current scope or in one of the above ones
     */
    fun getSymbol(name: String): AST? {
        for (table in scopeStack) {
            if (table.symbols.containsKey(name)) {
                return table.symbols[name]
            }
        }
        return null
    }

    /**
     * Return the table holding the global scope
     */
    fun getRootTable(): Table {
        // Get global scope from god scope
        return scopeStack[0].tables[0]
    }

    /**
     * Preload table at top of stack with builtin functions
     */
    private fun preloadBuiltinFunctions() {
        insertSymbol(BuiltinFuncCount.ident, BuiltinFuncCount)
        insertSymbol(BuiltinFuncRandi.ident, BuiltinFuncRandi)
        insertSymbol(BuiltinFuncRandf.ident, BuiltinFuncRandf)
        insertSymbol(BuiltinFuncAbsi.ident, BuiltinFuncAbsi)
        insertSymbol(BuiltinFuncAbsf.ident, BuiltinFuncAbsf)
        insertSymbol(BuiltinFuncFloor.ident, BuiltinFuncFloor)
        insertSymbol(BuiltinFuncCeil.ident, BuiltinFuncCeil)
        insertSymbol(BuiltinFuncRoot.ident, BuiltinFuncRoot)
        insertSymbol(BuiltinFuncPow.ident, BuiltinFuncPow)
    }
}

/**
 * A symbol table session that allows the user to walk through the scopes in order
 */
class ViewingSymbolTableSession(val symbolTable: Table) {
    private val indexStack: Stack<Int> = Stack()
    private val scopeStack: Stack<Table> = Stack()

    init {
        indexStack.push(0)
        scopeStack.push(Table(tables = mutableListOf(symbolTable))) // God scope

        // Enter global scope
        indexStack.push(0)
        scopeStack.push(symbolTable)
    }

    /**
     * Open the next subscope in the current scope, or the first scope if we haven't visited the any subscopes of the current scope
     */
    fun openScope() {
        val index = indexStack.peek()
        scopeStack.push(scopeStack.peek().tables[index])
        indexStack.push(0)
    }

    /**
     * Close the current scope
     */
    fun closeScope() {
        indexStack.pop()
        scopeStack.pop()
        indexStack.push(indexStack.pop() + 1)
    }

    /**
     * A symbol table session that allows the user to walk through the scopes in order.
     */
    fun getSymbol(name: String): AST? {
        for (table in scopeStack.reversed()) {
            if (table.symbols.containsKey(name)) {
                return table.symbols[name]
            }
        }
        return null
    }
}