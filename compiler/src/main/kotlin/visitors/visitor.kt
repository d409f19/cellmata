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

    fun visit(node: BinaryExpr): R

    fun visit(node: EqualityComparisonExpr): R

    fun visit(node: BinaryArithmeticExpr): R

    fun visit(node: BinaryBooleanExpr): R

    fun visit(node: NumericComparisonExpr): R

    fun visit(node: OrExpr): R

    fun visit(node: AndExpr): R

    fun visit(node: InequalityExpr): R

    fun visit(node: EqualityExpr): R

    fun visit(node: GreaterThanExpr): R

    fun visit(node: GreaterOrEqExpr): R

    fun visit(node: LessThanExpr): R

    fun visit(node: LessOrEqExpr): R

    fun visit(node: AdditionExpr): R

    fun visit(node: SubtractionExpr): R

    fun visit(node: MultiplicationExpr): R

    fun visit(node: DivisionExpr): R

    fun visit(node: NegationExpr): R

    fun visit(node: NotExpr): R

    fun visit(node: ArrayLookupExpr): R

    fun visit(node: SizedArrayExpr): R

    fun visit(node: Identifier): R

    fun visit(node: ModuloExpr): R

    fun visit(node: FuncCallExpr): R

    fun visit(node: ArrayLiteralExpr): R

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

    fun visit(node: FunctionArgument): R

    fun visit(node: WorldNode): R

    fun visit(node: WorldDimension): R

    fun visit(node: ForLoopStmt): R

    fun visit(node: BreakStmt): R

    fun visit(node: ContinueStmt): R

    fun visit(node: CodeBlock): R
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
            is FunctionArgument -> visit(node)
            is Coordinate -> visit(node)
            is ConditionalBlock -> visit(node)
            is CodeBlock -> visit(node)
            is ErrorAST -> visit(node)
        }
    }

    override fun visit(node: WorldNode) {
        node.dimensions.forEach { visit(it) }
        if (node.edge != null) {
            visit(node.edge)
        }
    }

    override fun visit(node: WorldDimension) {
        // no-op
    }

    override fun visit(node: FunctionArgument) {
        // no-op
    }

    override fun visit(node: RootNode) {
        // We visit the world declaration last, as it might contain identifier which is not declared yet. For instance
        // dimensions with edges refers to a state, but that state is declared after the world declaration.
        node.body.forEach { visit(it) }
        visit(node.world)
    }

    override fun visit(node: Decl) {
        when (node) {
            is ConstDecl -> visit(node)
            is StateDecl -> visit(node)
            is NeighbourhoodDecl -> visit(node)
            is FuncDecl -> visit(node)
            is ErrorDecl -> visit(node)
        }
    }

    override fun visit(node: ConstDecl) {
        visit(node.expr)
    }

    override fun visit(node: StateDecl) {
        visit(node.body)
    }

    override fun visit(node: NeighbourhoodDecl) {
        node.coords.forEach { visit(it) }
    }

    override fun visit(node: Coordinate) {
        // no-op
    }

    override fun visit(node: FuncDecl) {
        node.args.forEach { visit(it) }
        visit(node.body)
    }

    override fun visit(node: Expr) {
        when (node) {
            is BinaryExpr -> visit(node)
            is NegationExpr -> visit(node)
            is NotExpr -> visit(node)
            is ArrayLookupExpr -> visit(node)
            is SizedArrayExpr -> visit(node)
            is Identifier -> visit(node)
            is FuncCallExpr -> visit(node)
            is StateIndexExpr -> visit(node)
            is IntLiteral -> visit(node)
            is FloatLiteral -> visit(node)
            is BoolLiteral -> visit(node)
            is ArrayLiteralExpr -> visit(node)
            else -> throw AssertionError()
        }
    }
    
    override fun visit(node: BinaryExpr) {
        when (node) {
            is EqualityComparisonExpr -> visit(node)
            is BinaryArithmeticExpr -> visit(node)
            is BinaryBooleanExpr -> visit(node)
            is NumericComparisonExpr -> visit(node)
        }
    }

    override fun visit(node: EqualityComparisonExpr) {
        when (node) {
            is InequalityExpr -> visit(node)
            is EqualityExpr -> visit(node)
        }
    }

    override fun visit(node: BinaryArithmeticExpr) {
        when (node) {
            is AdditionExpr -> visit(node)
            is SubtractionExpr -> visit(node)
            is MultiplicationExpr -> visit(node)
            is DivisionExpr -> visit(node)
            is ModuloExpr -> visit(node)
        }
    }
    
    override fun visit(node: BinaryBooleanExpr) {
        when (node) {
            is OrExpr -> visit(node)
            is AndExpr -> visit(node)
        }
    }

    override fun visit(node: NumericComparisonExpr) {
        when (node) {
            is GreaterThanExpr -> visit(node)
            is GreaterOrEqExpr -> visit(node)
            is LessThanExpr -> visit(node)
            is LessOrEqExpr -> visit(node)
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

    override fun visit(node: GreaterThanExpr) {
        visit(node.left)
        visit(node.right)
    }

    override fun visit(node: GreaterOrEqExpr) {
        visit(node.left)
        visit(node.right)
    }

    override fun visit(node: LessThanExpr) {
        visit(node.left)
        visit(node.right)
    }

    override fun visit(node: LessOrEqExpr) {
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

    override fun visit(node: NegationExpr) {
        visit(node.value)
    }

    override fun visit(node: NotExpr) {
        visit(node.value)
    }

    override fun visit(node: ArrayLookupExpr) {
        visit(node.arr)
        visit(node.index)
    }

    override fun visit(node: SizedArrayExpr) {
        if (node.body == null) {
            return
        }
        visit(node.body)
    }

    override fun visit(node: ArrayLiteralExpr) {
        node.values.forEach { visit(it) }
    }

    override fun visit(node: Identifier) {
        // no-op
    }

    override fun visit(node: ModuloExpr) {
        visit(node.left)
        visit(node.right)
    }

    override fun visit(node: FuncCallExpr) {
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
        when (node) {
            is AssignStmt -> visit(node)
            is IfStmt -> visit(node)
            is BecomeStmt -> visit(node)
            is ReturnStmt -> visit(node)
            is ForLoopStmt -> visit(node)
            is BreakStmt -> visit(node)
            is ContinueStmt -> visit(node)
            is ErrorStmt -> visit(node)
        }
    }

    override fun visit(node: AssignStmt) {
        visit(node.expr)
    }

    override fun visit(node: IfStmt) {
        node.conditionals.forEach { visit(it) }
        if (node.elseBlock != null) {
            visit(node.elseBlock)
        }
    }

    override fun visit(node: ConditionalBlock) {
        visit(node.expr)
        visit(node.block)
    }

    override fun visit(node: BecomeStmt) {
        visit(node.state)
    }

    override fun visit(node: ReturnStmt) {
        visit(node.expr)
    }

    override fun visit(node: ForLoopStmt) {
        node.initPart?.let { visit(it) }
        visit(node.condition)
        node.postIterationPart?.let { visit(it) }
        visit(node.body)
    }

    override fun visit(node: BreakStmt) {
        // no-op
    }

    override fun visit(node: ContinueStmt) {
        // no-op
    }

    override fun visit(node: CodeBlock) {
        node.body.forEach { visit(it) }
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

    override fun visit(node: ForLoopStmt) {
        symbolTableSession.openScope()
        node.initPart?.let { visit(it) }
        visit(node.condition)
        symbolTableSession.openScope()
        visit(node.body)
        symbolTableSession.closeScope()
        node.postIterationPart?.let { visit(it) }
        symbolTableSession.closeScope()
    }
}
