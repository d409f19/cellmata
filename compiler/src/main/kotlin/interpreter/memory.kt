package dk.aau.cs.d409f19.cellumata.interpreter

import java.util.*
import kotlin.collections.HashMap

class UndeclaredSymbol(symbol: String) : InterpretRuntimeException("'$symbol' was undefined!")
class RedeclaredSymbol(symbol: String) : InterpretRuntimeException("'$symbol' already declared!")

class MemoryStack {

    private class ValueTable : HashMap<String, Any>()
    private class Scope : Stack<ValueTable>()
    private val stack = Stack<Scope>()
    private val globalValues = ValueTable()

    fun declareGlobal(identifier: String, value: Any) {
        if (globalValues.containsKey(identifier)) {
            throw RedeclaredSymbol(identifier)
        }
        globalValues[identifier] = value
    }

    fun declare(identifier: String, value: Any) {
        if (stack.peek().peek().containsKey(identifier)) {
            throw RedeclaredSymbol(identifier)
        }
        stack.peek().peek()[identifier] = value
    }

    operator fun set(identifier: String, value: Any) {
        for (table in stack.peek()) {
            if (table.containsKey(identifier)) {
                table[identifier] = value
                return
            }
        }
        throw UndeclaredSymbol(identifier)
    }

    operator fun get(identifier: String): Any {
        for (table in stack.peek()) {
            table[identifier]?.let { return it }
        }
        globalValues[identifier]?.let { return it }
        throw UndeclaredSymbol(identifier)
    }

    fun pushStack() {
        stack.push(Scope())
    }

    fun popStack() {
        stack.pop()
    }

    fun openScope() {
        stack.peek().push(ValueTable())
    }

    fun closeScope() {
        stack.peek().pop()
    }
}