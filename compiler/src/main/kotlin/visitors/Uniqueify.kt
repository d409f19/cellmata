package dk.aau.cs.d409f19.cellumata.visitors

import com.ibm.icu.text.SymbolTable
import com.sun.xml.internal.fastinfoset.algorithm.BuiltInEncodingAlgorithm
import dk.aau.cs.d409f19.cellumata.ast.*
import dk.aau.cs.d409f19.cellumata.visitors.codegen.InvalidIdentLookupError
import java.util.Stack

/**
 * Makes all identifiers unique. This elements need keep track of scoping in some cases.
 *
 * Prerequisites: The ScopeChecker must be run prior to this phase. We to make sure that redeclaration doesn't occur.
 */
class Uniqueify(oldSymbolTable: Table, newSymbolTable: Table = Table()): BaseScopedASTVisitor() {
    private var viewingSymbolTableSession = ViewingSymbolTableSession(oldSymbolTable)
    private var creatingSymbolTabelSession = CreatingSymbolTableSession(newSymbolTable)
    private val mappings: Stack<Map<String, String>> = Stack()
    private var labelCounter = 0

    override fun openScope() {
        viewingSymbolTableSession.openScope()
        creatingSymbolTabelSession.openScope()
    }

    override fun closeScope() {
        viewingSymbolTableSession.closeScope()
        creatingSymbolTabelSession.closeScope()
    }

    init {
        mappings.push(mapOf()) // Global scope
    }

    private fun nextLabel(): String {
        return "label_${labelCounter++}"
    }

    /**
     * @param ident Identifier
     * @return The mapped label for the identifier
     */
    private fun addMapping(ident: String): String {
        // Change the map object at the top of the stack
        val label = nextLabel()
        creatingSymbolTabelSession.insertSymbol(
            label,
            viewingSymbolTableSession.getSymbol(ident)!!
        )
        mappings.push(mappings.pop() + Pair(ident, label))
        System.out.println("Uniqueify: mapped $ident to $label") // ToDo use a proper logging system
        return label
    }

    private fun getMappedLabel(ident: String): String {
        for (scope in mappings.reversed()) {
            if (scope.containsKey(ident)) {
                val label = scope.getValue(ident)
                System.out.println("Uniqueify: Found mapping from $ident to $label")
                return label
            }
        }

        System.out.println("Uniqueify: No mapping for $ident")
        throw InvalidIdentLookupError()
    }

    fun getSymbolTable(): Table {
        return creatingSymbolTabelSession.getRootTable()
    }

    override fun visit(node: RootNode) {
        node.body.forEach {
            when (it) {
                is ConstDecl -> it.ident = addMapping(it.ident)
                is StateDecl -> it.ident = addMapping(it.ident)
                is NeighbourhoodDecl -> it.ident = addMapping(it.ident)
                is FuncDecl -> it.ident = addMapping(it.ident)
            }
        }

        super.visit(node)
    }

    override fun visit(node: Identifier) {
        node.spelling = getMappedLabel(node.spelling)

        super.visit(node)
    }

    override fun visit(node: FuncCallExpr) {
        // Ignore builtin functions, their names are already unique
        if (viewingSymbolTableSession.getSymbol(node.ident) !is BuiltinFunc) {
            node.ident = getMappedLabel(node.ident)
        }

        super.visit(node)
    }

    override fun visit(node: AssignStmt) {
        node.ident = if (node.isDeclaration) {
            addMapping(node.ident)
        } else {
            getMappedLabel(node.ident)
        }

        super.visit(node)
    }

    override fun visit(node: FunctionArgument) {
        node.ident = addMapping(node.ident)

        super.visit(node)
    }
}