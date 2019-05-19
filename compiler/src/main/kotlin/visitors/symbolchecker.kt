package dk.aau.cs.d409f19.cellumata.visitors

import dk.aau.cs.d409f19.cellumata.CompileError
import dk.aau.cs.d409f19.cellumata.ErrorLogger
import dk.aau.cs.d409f19.cellumata.ast.*

/**
 * Logged when a undefined symbol is encountered. This exception indicates there is a use-before-declaration scenario.
 */
class UndeclaredNameException(ctx: SourceContext, val ident: String) : CompileError(ctx, "\"$ident\" is undeclared.")

/**
 * Walks through the abstract syntax tree, extracts symbols, and checks for use-before-declaration.
 */
class ScopeCheckVisitor(symbolTable: Table = Table()) : BaseScopedASTVisitor() {
    private val symbolTableSession: CreatingSymbolTableSession = CreatingSymbolTableSession(symbolTable = symbolTable)

    /**
     * @return The filled symbol table
     */
    fun getSymbolTable(): Table {
        return symbolTableSession.getRootTable()
    }

    override fun openScope() {
        symbolTableSession.openScope()
    }

    override fun closeScope() {
        symbolTableSession.closeScope()
    }

    override fun visit(node: RootNode) {
        // Extract all states, functions, and neighbourhoods names first
        node.body.filter { it is StateDecl }.forEach { symbolTableSession.insertSymbol((it as StateDecl).ident, it) }
        node.body.filter { it is FuncDecl }.forEach { symbolTableSession.insertSymbol((it as FuncDecl).ident, it) }
        node.body.filter { it is NeighbourhoodDecl }.forEach { symbolTableSession.insertSymbol((it as NeighbourhoodDecl).ident, it) }

        // Visit constant declaration first
        node.body.filter { it is ConstDecl }.forEach { visit(it) }

        // Visit the other declarations
        node.body.filter { it !is ConstDecl }.forEach { visit(it) }

        super.visit(node.world)
    }

    override fun visit(node: Identifier) {

        // Check if the name is in the symbol table
        val symb = symbolTableSession.getSymbol(node.spelling)
        if (symb == null) {
            ErrorLogger += UndeclaredNameException(node.ctx, node.spelling)
        }
        super.visit(node)
    }

    override fun visit(node: ConstDecl) {
        super.visit(node)
        symbolTableSession.insertSymbol(node.ident, node)
    }

    override fun visitFuncDeclPreNode(node: FuncDecl) {
        node.args.forEach { symbolTableSession.insertSymbol(it.ident, it) }
    }

    override fun visit(node: AssignStmt) {
        if (node.isDeclaration) { // Check if this is a variable declaration, and not just an assignment
            // declaration
            symbolTableSession.insertSymbol(node.ident, node)
        } else {
            // assignment
            val symb = symbolTableSession.getSymbol(node.ident)
            if (symb == null) {
                ErrorLogger += UndeclaredNameException(node.ctx, node.ident)
            }
        }

        super.visit(node)
    }

    override fun visit(node: FuncCallExpr) {
        if (symbolTableSession.getSymbol(node.ident) == null) {
            ErrorLogger += UndeclaredNameException(node.ctx, node.ident)
        }
        super.visit(node)
    }
}
