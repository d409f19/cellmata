package dk.aau.cs.d409f19.cellumata.ast

import java.util.*

data class Table(
    val symbols: MutableMap<String, AST> = mutableMapOf(),
    val tables: MutableList<Table> = mutableListOf()
)

class SymbolRedefinitionException(val ident: String): Exception("\"$ident\" is already defined")

class SymbolTable {
    private val root: Table = Table()
    private val scopeStack: Stack<Table> = Stack()

    init {
        scopeStack.push(root)
    }

    fun insertSymbol(ident: String, node: AST) {
        val table = scopeStack.peek()

        if(table.symbols.containsKey(ident)) {
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