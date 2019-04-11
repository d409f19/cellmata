package dk.aau.cs.d409f19.cellumata.visitors

import dk.aau.cs.d409f19.cellumata.ast.*

/**
 * The base interface for an implementation of the visitor pattern on the abstract syntax tree
 */
interface ASTVisitor<R> {
    fun visit(node: RootNode): R

    fun visit(node: Decl): R

    fun visit(node: ConstDecl): R

    fun visit(node: StateDecl): R

    fun visit(node: NeighbourhoodDecl): R

    fun visit(node: Coordinate): R

    fun visit(node: FuncDecl): R

    fun visit(node: Expr): R

    fun visit(node: OrExpr): R

    fun visit(node: AndExpr): R

    fun visit(node: InequalityExpr): R

    fun visit(node: EqualityExpr): R

    fun visit(node: MoreThanExpr): R

    fun visit(node: MoreEqExpr): R

    fun visit(node: LessThanExpr): R

    fun visit(node: LessEqExpr): R

    fun visit(node: AdditionExpr): R

    fun visit(node: SubtractionExpr): R

    fun visit(node: MultiplicationExpr): R

    fun visit(node: DivisionExpr): R

    fun visit(node: NegativeExpr): R

    fun visit(node: InverseExpr): R

    fun visit(node: ArrayLookupExpr): R

    fun visit(node: ArrayBodyExpr): R

    fun visit(node: ParenExpr): R

    fun visit(node: NamedExpr): R

    fun visit(node: ModuloExpr): R

    fun visit(node: FuncExpr): R

    fun visit(node: StateIndexExpr): R

    fun visit(node: IntLiteral): R

    fun visit(node: BoolLiteral): R

    fun visit(node: Stmt): R

    fun visit(node: AssignStmt): R

    fun visit(node: IfStmt): R

    fun visit(node: BecomeStmt): R
  
    fun visit(node: ReturnStmt): R
    fun visit(node: AST): R
    fun visit(node: FloatLiteral): R
    fun visit(node: ConditionalBlock): R
    fun visit(node: FunctionArgs): R

    fun visit(node: WorldNode): R

    fun visit(node: WorldDimension): R

    fun visit(node: ForStmt): R

    fun visit(node: BreakStmt): R

    fun visit(node: ContinueStmt): R
}

/**
 * A basic implementation of an visitor pattern for the abstract syntax tree.
 * By default does a in-order walk of the abstract syntax tree.
 */
abstract class BaseASTVisitor: ASTVisitor<Unit> {
    override fun visit(node: AST) {
        when (node) {
            is RootNode -> visit(node)
            is WorldNode -> visit(node)
            is Expr -> visit(node)
            is Decl -> visit(node)
            is Stmt -> visit(node)
            is FunctionArgs -> visit(node)
        }
    }

    override fun visit(node: WorldNode) {
        node.dimensions.forEach { visit(it) }
    }

    override fun visit(node: WorldDimension) {
        // no-op
    }

    override fun visit(node: FunctionArgs) {
        // no-op
    }

    override fun visit(node: RootNode) {
        visit(node.world)
        node.body.forEach { visit(it) }
    }

    override fun visit(node: Decl) {
        when(node) {
            is ConstDecl -> visit(node)
            is StateDecl -> visit(node)
            is NeighbourhoodDecl -> visit(node)
            is FuncDecl -> visit(node)
        }
    }

    override fun visit(node: ConstDecl) {
        visit(node.expr)
    }

    override fun visit(node: StateDecl) {
        node.body.forEach { visit(it) }
    }

    override fun visit(node: NeighbourhoodDecl) {
        node.coords.forEach { visit(it) }
    }

    override fun visit(node: Coordinate) {
        // no-op
    }

    override fun visit(node: FuncDecl) {
        node.args.forEach { visit(it) }
        node.body.forEach { visit(it) }
    }

    override fun visit(node: Expr) {
        when(node) {
            is OrExpr -> visit(node)
            is AndExpr -> visit(node)
            is InequalityExpr -> visit(node)
            is EqualityExpr -> visit(node)
            is MoreThanExpr -> visit(node)
            is MoreEqExpr -> visit(node)
            is LessThanExpr -> visit(node)
            is LessEqExpr -> visit(node)
            is AdditionExpr -> visit(node)
            is SubtractionExpr -> visit(node)
            is MultiplicationExpr -> visit(node)
            is DivisionExpr -> visit(node)
            is NegativeExpr -> visit(node)
            is InverseExpr -> visit(node)
            is ArrayLookupExpr -> visit(node)
            is ArrayBodyExpr -> visit(node)
            is ParenExpr -> visit(node)
            is NamedExpr -> visit(node)
            is ModuloExpr -> visit(node)
            is FuncExpr -> visit(node)
            is StateIndexExpr -> visit(node)
            is IntLiteral -> visit(node)
            is FloatLiteral -> visit(node)
            is BoolLiteral -> visit(node)
            else -> throw AssertionError()
        }
    }

    override fun visit(node: OrExpr) {
        visit(node.left)
        visit(node.right)
    }

    override fun visit(node: AndExpr) {
        visit(node.left)
        visit(node.right)
    }

    override fun visit(node: InequalityExpr) {
        visit(node.left)
        visit(node.right)
    }

    override fun visit(node: EqualityExpr) {
        visit(node.left)
        visit(node.right)
    }

    override fun visit(node: MoreThanExpr) {
        visit(node.left)
        visit(node.right)
    }

    override fun visit(node: MoreEqExpr) {
        visit(node.left)
        visit(node.right)
    }

    override fun visit(node: LessThanExpr) {
        visit(node.left)
        visit(node.right)
    }

    override fun visit(node: LessEqExpr) {
        visit(node.left)
        visit(node.right)
    }

    override fun visit(node: AdditionExpr) {
        visit(node.left)
        visit(node.right)
    }

    override fun visit(node: SubtractionExpr) {
        visit(node.left)
        visit(node.right)
    }

    override fun visit(node: MultiplicationExpr) {
        visit(node.left)
        visit(node.right)
    }

    override fun visit(node: DivisionExpr) {
        visit(node.left)
        visit(node.right)
    }

    override fun visit(node: NegativeExpr) {
        visit(node.value)
    }

    override fun visit(node: InverseExpr) {
        visit(node.value)
    }

    override fun visit(node: ArrayLookupExpr) {
        visit(node.index)
    }

    override fun visit(node: ArrayBodyExpr) {
        node.values.forEach { visit(it) }
    }

    override fun visit(node: ParenExpr) {
        visit(node.expr)
    }

    override fun visit(node: NamedExpr) {
        // no-op
    }

    override fun visit(node: ModuloExpr) {
        visit(node.left)
        visit(node.right)
    }

    override fun visit(node: FuncExpr) {
        node.args.forEach { visit(it) }
    }

    override fun visit(node: StateIndexExpr) {
        // no-op
    }

    override fun visit(node: IntLiteral) {
        // no-op
    }

    override fun visit(node: BoolLiteral) {
        // no-op
    }

    override fun visit(node: FloatLiteral) {
        // no-op
    }

    override fun visit(node: Stmt) {
        when(node) {
            is AssignStmt -> visit(node)
            is IfStmt -> visit(node)
            is BecomeStmt -> visit(node)
            is ReturnStmt -> visit(node)
            is ForStmt -> visit(node)
            is BreakStmt -> visit(node)
            is ContinueStmt -> visit(node)
        }
    }

    override fun visit(node: AssignStmt) {
        visit(node.expr)
    }

    override fun visit(node: IfStmt) {
        node.conditionals.forEach { visit(it) }
        if (node.elseBlock != null) {
            node.elseBlock.forEach { visit(it) }
        }
    }

    override fun visit(node: ConditionalBlock) {
        visit(node.expr)
        node.block.forEach { stmt -> visit(stmt) }
    }

    override fun visit(node: BecomeStmt) {
        visit(node.state)
    }

    override fun visit(node: ReturnStmt) {
        visit(node.value)
    }

    override fun visit(node: ForStmt) {
        visit(node.initPart)
        visit(node.condition)
        visit(node.postIterationPart)
        node.body.forEach { visit(it) }
    }

    override fun visit(node: BreakStmt) {
        // no-op
    }

    override fun visit(node: ContinueStmt) {
        // no-op
    }
}

/**
 * Walks the tree while opening and closing scopes as they are entered and left.
 *
 * @see BaseASTVisitor
 */
open class ScopedASTVisitor(symbolTable: Table): BaseASTVisitor() {
    protected val symbolTableSession = ViewingSymbolTableSession(symbolTable = symbolTable)

    override fun visit(node: StateDecl) {
        symbolTableSession.openScope()
        super.visit(node)
        symbolTableSession.closeScope()
    }

    override fun visit(node: FuncDecl) {
        symbolTableSession.openScope()
        super.visit(node)
        symbolTableSession.closeScope()
    }

    override fun visit(node: ConditionalBlock) {
        symbolTableSession.openScope()
        super.visit(node)
        symbolTableSession.closeScope()
    }
}
