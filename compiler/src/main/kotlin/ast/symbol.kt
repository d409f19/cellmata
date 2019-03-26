package dk.aau.cs.d409f19.cellumata.ast

import dk.aau.cs.d409f19.cellumata.ErrorFromContext
import dk.aau.cs.d409f19.cellumata.ErrorLogger
import org.antlr.v4.runtime.ParserRuleContext
import java.util.*

data class Table(
    val symbols: MutableMap<String, AST> = mutableMapOf(),
    val tables: MutableList<Table> = mutableListOf()
)

class SymbolRedefinitionException(ctx: ParserRuleContext, val ident: String) : ErrorFromContext(ctx, "\"$ident\" is already defined")

private val RESERVED_SYMBOLS: List<String> = listOf(
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
    "break",
    "rand",
    "abs",
    "floor",
    "ceil",
    "sqrt",
    "pow"
)

class SymbolTable {
    private val root: Table = Table()
    private val scopeStack: Stack<Table> = Stack()

    init {
        scopeStack.push(root)
    }

    fun insertSymbol(ident: String, node: AST) {
        val table = scopeStack.peek()

        if (RESERVED_SYMBOLS.contains(ident) || table.symbols.containsKey(ident)) {
            ErrorLogger.registerError(SymbolRedefinitionException(node.ctx, ident))
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