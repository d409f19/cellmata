package dk.aau.cs.d409f19.cellumata.interpreter

import java.util.*
import kotlin.collections.HashMap

class UndeclaredSymbol(symbol: String) : InterpretRuntimeException("'$symbol' was undefined!")

class MemoryStack() {

    private class ValueTable : HashMap<String, Any>()
    private val stack = Stack<ValueTable>()

    operator fun get(identifier: String): Any {
        for (table in stack) {
            table[identifier]?.let { return it }
        }
        throw UndeclaredSymbol(identifier)
    }

    fun declare(identifier: String, value: Any) {
        if (stack.peek().containsKey(identifier)) {
            throw UndeclaredSymbol(identifier)
        }
        stack.peek()[identifier] = value
    }

    operator fun set(identifier: String, value: Any) {
        // Does the symbol exist already?
        for (table in stack) {
            if (table.containsKey(identifier)) {
                table[identifier] = value
                return
            }
        }
        throw UndeclaredSymbol(identifier)
    }

    fun push() {
        stack.push(ValueTable())
    }

    fun pop() {
        stack.pop()
    }
}