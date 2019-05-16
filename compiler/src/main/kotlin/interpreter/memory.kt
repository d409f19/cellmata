package dk.aau.cs.d409f19.cellumata.interpreter

import java.util.*
import kotlin.collections.HashMap

class UndeclaredSymbol(symbol: String) : InterpretRuntimeException("'$symbol' was undefined!")
class RedeclaredSymbol(symbol: String) : InterpretRuntimeException("'$symbol' already declared!")

/**
 * The MemoryStack represents a combination of block scopes and the process stack. It is used to map identifiers to
 * values. The scope and stack is controlled with open/close scope and push/pop stack functions. Stacks are created
 * by function calls, and the code in a stack frame cannot see identifiers from other stack frames. Scopes are created
 * by blocks, and the code in a block can see the values of scopes surrounding it.
 */
class MemoryStack {

    // The actual data structures of the MemoryStack
    private class ValueTable : HashMap<String, Any>()
    private class Scope : Stack<ValueTable>()
    private val stack = Stack<Scope>()

    /**
     * The global value table always visible, no matter the current stack and scope.
     */
    private val globalValues = ValueTable()

    /**
     * Add a new identifier in the global scope
     */
    fun declareGlobal(identifier: String, value: Any) {
        if (globalValues.containsKey(identifier)) {
            throw RedeclaredSymbol(identifier)
        }
        globalValues[identifier] = value
    }

    /**
     * Add a new identifier in the current stack/scope
     */
    fun declare(identifier: String, value: Any) {
        if (stack.peek().peek().containsKey(identifier)) {
            throw RedeclaredSymbol(identifier)
        }
        stack.peek().peek()[identifier] = value
    }

    /**
     * Change the value associated with the given identifier
     */
    operator fun set(identifier: String, value: Any) {
        for (table in stack.peek()) {
            if (table.containsKey(identifier)) {
                table[identifier] = value
                return
            }
        }
        throw UndeclaredSymbol(identifier)
    }

    /**
     * Retrieve the value associated with the given identifier
     */
    operator fun get(identifier: String): Any {
        for (table in stack.peek()) {
            table[identifier]?.let { return it }
        }
        // Identifier was not in scope, maybe it is in the global table?
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