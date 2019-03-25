package dk.aau.cs.d409f19.cellumata.walkers

import dk.aau.cs.d409f19.cellumata.ast.*
import java.lang.Exception

class SymbolException(val ident: String) : Exception("\"$ident\" was used before it was declared")

class ScopeCheckVisitor(val symbolTable: SymbolTable) : BaseASTVisitor() {
    override fun visit(node: RootNode) {
        // Extract all states, functions, and neighbourhoods names first
        node.body.filter { it is StateDecl }.forEach { symbolTable.insertSymbol((it as StateDecl).ident, it) }
        node.body.filter { it is FuncDecl }.forEach { symbolTable.insertSymbol((it as FuncDecl).ident, it) }
        node.body.filter { it is NeighbourhoodDecl }.forEach { symbolTable.insertSymbol((it as NeighbourhoodDecl).ident, it) }

        // Visit constant declaration first
        node.body.filter { it is ConstDecl }.forEach { visit(it) }

        // Visit the other declarations
        node.body.filter { it !is ConstDecl }.forEach { visit(it) }
    }

    override fun visit(node: VarExpr) {
        if (symbolTable.getSymbol(node.ident) == null) {
            throw SymbolException(node.ident)
        }
        super.visit(node)
    }

    override fun visit(node: ConstDecl) {
        super.visit(node)
        symbolTable.insertSymbol(node.ident, node)
    }

    override fun visit(node: StateDecl) {
        symbolTable.createScope()
        super.visit(node)
        symbolTable.closeScope()
    }

    override fun visit(node: FuncDecl) {
        symbolTable.createScope()
        node.args.forEach { symbolTable.insertSymbol(it.ident, it) }

        super.visit(node)
        symbolTable.closeScope()
    }

    override fun visit(node: AssignStmt) {
        super.visit(node)

        if (node.ctx.STMT_LET() != null) { // Check if this is a variable declaration, and not just an assignment
            symbolTable.insertSymbol(node.ident, node)
        }
    }

    override fun visit(node: ConditionalBlock) {
        symbolTable.createScope()
        super.visit(node)
        symbolTable.closeScope()
    }

    override fun visit(node: FuncExpr) {
        if (symbolTable.getSymbol(node.ident) == null) {
            throw SymbolException(node.ident)
        }
        super.visit(node)
    }
}
