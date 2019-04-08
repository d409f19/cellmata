package dk.aau.cs.d409f19.cellumata.ast

import dk.aau.cs.d409f19.cellumata.ErrorFromContext
import dk.aau.cs.d409f19.cellumata.ErrorLogger
import org.antlr.v4.runtime.ParserRuleContext
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
class SymbolRedefinitionError(ctx: ParserRuleContext, val ident: String) : ErrorFromContext(ctx, "\"$ident\" is already defined")

/**
 * List of language keywords that can't be used as identifiers
 */
val RESERVED_SYMBOLS: List<String> = listOf(
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

@Deprecated("Use CreatingSymbolTableSession")
class SymbolTable {
    private val root: Table = Table()
    private val scopeStack: Stack<Table> = Stack()

    init {
        scopeStack.push(root)
    }

    fun insertSymbol(ident: String, node: AST) {
        val table = scopeStack.peek()

        if (RESERVED_SYMBOLS.contains(ident) || table.symbols.containsKey(ident)) {
            ErrorLogger.registerError(SymbolRedefinitionError(node.ctx, ident))
        } else {
            table.symbols[ident] = node
        }
    }

    fun getSymbol(name: String): AST? {
        for (table in scopeStack) {
            if (table.symbols.containsKey(name)) {
                return table.symbols.get(name)
            }
        }
        return null
    }

    fun getSymbolType(name: String): Type? {
        val symbol = getSymbol(name) ?: return null

        return when(symbol) {
            is TypedNode -> symbol.getType()
            is StateDecl -> StateType
            is ConstDecl -> symbol.type
            is FuncDecl -> symbol.returnType
            else -> null
        }
    }

    fun createScope() {
        val newScope = Table()
        scopeStack.peek().tables.add(newScope)
        scopeStack.push(newScope)
    }

    fun closeScope() {
        assert(scopeStack.size > 1) { "Tried to remove root scope from symbol table" }
        scopeStack.pop()
    }

    fun openScope(index: Int) {
        scopeStack.push(scopeStack.peek().tables[index])
    }
}

/**
 * Assists in building and filling a symbol table
 */
class CreatingSymbolTableSession(symbolTable: Table) {
    private val scopeStack: Stack<Table> = Stack()

    init {
        scopeStack.push(Table(tables = mutableListOf(symbolTable))) // God scope
        scopeStack.push(symbolTable)
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

        if (RESERVED_SYMBOLS.contains(ident) || table.symbols.containsKey(ident)) {
            ErrorLogger.registerError(SymbolRedefinitionError(node.ctx, ident))
        }

        table.symbols[ident] = node
    }

    /**
     * Find a symbol in the current scope or in one of the above ones
     */
    fun getSymbol(name: String): AST? {
        for (table in scopeStack) {
            if (table.symbols.containsKey(name)) {
                return table.symbols.get(name)
            }
        }
        return null
    }

    /**
     * Return the type of the symbol identified by name
     *
     * @see getSymbol
     */
    fun getSymbolType(name: String): Type? {
        val symbol = getSymbol(name) ?: return null

        return when(symbol) {
            is TypedNode -> symbol.getType()
            is StateDecl -> StateType
            is ConstDecl -> symbol.type
            is FuncDecl -> symbol.returnType
            else -> null
        }
    }

    /**
     * Return the table holding the global scope
     */
    fun getRootTable(): Table {
        // Get global scope from god scope
        return scopeStack[0].tables[0]
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
        for (table in scopeStack) {
            if (table.symbols.containsKey(name)) {
                return table.symbols.get(name)
            }
        }
        return null
    }

    /**
     * Return the type of the symbol identified by name
     *
     * @see getSymbol
     */
    fun getSymbolType(name: String): Type? {
        val symbol = getSymbol(name) ?: return null

        return when(symbol) {
            is TypedNode -> symbol.getType()
            is StateDecl -> StateType
            is ConstDecl -> symbol.type
            is FuncDecl -> symbol.returnType
            else -> null
        }
    }
}