package dk.aau.cs.d409f19.cellumata.walkers

import dk.aau.cs.d409f19.cellumata.ast.*
import java.lang.Exception

class SymbolException(val ident: String) : Exception("\"$ident\" was used before it was declared")

class ScopeCheckVisitor(symbolTable: Table = Table()) : BaseASTVisitor() {
    private val symbolTableSession: CreatingSymbolTableSession = CreatingSymbolTableSession(symbolTable = symbolTable)

    fun getSymbolTable(): Table {
        return symbolTableSession.getRootTable()
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
    }

    override fun visit(node: NamedExpr) {
        node.setType(symbolTableSession.getSymbolType(node.ident) ?: throw SymbolException(node.ident))
        super.visit(node)
    }

    override fun visit(node: ConstDecl) {
        super.visit(node)
        symbolTableSession.insertSymbol(node.ident, node)
    }

    override fun visit(node: StateDecl) {
        symbolTableSession.openScope()
        super.visit(node)
        symbolTableSession.closeScope()
    }

    override fun visit(node: FuncDecl) {
        symbolTableSession.openScope()
        node.args.forEach { symbolTableSession.insertSymbol(it.ident, it) }

        super.visit(node)
        symbolTableSession.closeScope()
    }

    override fun visit(node: AssignStmt) {
        super.visit(node)

        if (node.ctx.STMT_LET() != null) { // Check if this is a variable declaration, and not just an assignment
            symbolTableSession.insertSymbol(node.ident, node)
        }
    }

    override fun visit(node: ConditionalBlock) {
        symbolTableSession.openScope()
        super.visit(node)
        symbolTableSession.closeScope()
    }

    override fun visit(node: FuncExpr) {
        if (symbolTableSession.getSymbol(node.ident) == null) {
            throw SymbolException(node.ident)
        }
        super.visit(node)
    }
}
