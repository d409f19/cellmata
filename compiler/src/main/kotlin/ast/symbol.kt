package dk.aau.cs.d409f19.cellumata.ast

import java.util.*

data class Table(
    val symbols: MutableMap<String, AST> = mutableMapOf(),
    val tables: MutableList<Table> = mutableListOf()
)

class SymbolRedefinitionException(val ident: String) : Exception("\"$ident\" is already defined")

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

@Deprecated("Use CreatingSymbolTableSession")
class SymbolTable {
    private val root: Table = Table()
    private val scopeStack: Stack<Table> = Stack()

    init {
        scopeStack.push(root)
    }

    fun insertSymbol(ident: String, node: AST) {
        val table = scopeStack.peek()

        if (RESERVED_SYMBOLS.contains(ident)) {
            throw SymbolRedefinitionException(ident = ident)
        }

        if (table.symbols.containsKey(ident)) {
            throw SymbolRedefinitionException(ident = ident)
        }

        table.symbols[ident] = node
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

class CreatingSymbolTableSession(symbolTable: Table) {
    private val scopeStack: Stack<Table> = Stack()

    init {
        scopeStack.push(Table(tables = mutableListOf(symbolTable))) // God scope
        scopeStack.push(symbolTable)
    }

    fun openScope() {
        val newScope = Table()
        scopeStack.peek().tables.add(newScope)
        scopeStack.push(newScope)
    }

    fun closeScope() {
        assert(scopeStack.size > 1) { "Tried to remove root scope from symbol table" }
        scopeStack.pop()
    }

    fun insertSymbol(ident: String, node: AST) {
        val table = scopeStack.peek()

        if (RESERVED_SYMBOLS.contains(ident)) {
            throw SymbolRedefinitionException(ident = ident)
        }

        if (table.symbols.containsKey(ident)) {
            throw SymbolRedefinitionException(ident = ident)
        }

        table.symbols[ident] = node
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

    fun getRootTable(): Table {
        // Get global scope from god scope
        return scopeStack[0].tables[0]
    }
}

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

    fun openScope() {
        val index = indexStack.peek()
        scopeStack.push(scopeStack.peek().tables[index])
        indexStack.push(0)
    }

    fun closeScope() {
        indexStack.pop()
        scopeStack.pop()
        indexStack.push(indexStack.pop() + 1)
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
}