package dk.aau.cs.d409f19.cellumata.visitors

import dk.aau.cs.d409f19.cellumata.ErrorFromContext
import dk.aau.cs.d409f19.cellumata.ErrorLogger
import dk.aau.cs.d409f19.cellumata.ast.*
import org.antlr.v4.runtime.ParserRuleContext

/**
 * Logged when a undefined symbol is encountered. This exception indicates there is a use-before-declaration scenario.
 */
class UndeclaredNameException(ctx: ParserRuleContext, val ident: String) : ErrorFromContext(ctx, "\"$ident\" is undeclared.")

/**
 * Walks through the abstract syntax tree, extracts symbols, and checks for use-before-declaration.
 */
class ScopeCheckVisitor(symbolTable: Table = Table()) : BaseASTVisitor() {
    private val symbolTableSession: CreatingSymbolTableSession = CreatingSymbolTableSession(symbolTable = symbolTable)

    /**
     * @return The filed symbol table
     */
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

    override fun visit(node: Identifier) {

        // Check if the name is in the symbol table
        val symb = symbolTableSession.getSymbol(node.spelling)
        if (symb == null) {
            ErrorLogger.registerError(UndeclaredNameException(node.ctx, node.spelling))
        }
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
        if (node.ctx.STMT_LET() == null) { // Check if this is a variable declaration, and not just an assignment
            // assignment
            val symb = symbolTableSession.getSymbol(node.ident)
            if (symb == null) {
                ErrorLogger.registerError(UndeclaredNameException(node.ctx, node.ident))
            }
        } else {
            // declaration
            symbolTableSession.insertSymbol(node.ident, node)
        }

        super.visit(node)
    }

    override fun visit(node: ConditionalBlock) {
        symbolTableSession.openScope()
        super.visit(node)
        symbolTableSession.closeScope()
    }

    override fun visit(node: FuncExpr) {
        if (symbolTableSession.getSymbol(node.ident) == null) {
            ErrorLogger.registerError(UndeclaredNameException(node.ctx, node.ident))
        }
        super.visit(node)
    }
}
