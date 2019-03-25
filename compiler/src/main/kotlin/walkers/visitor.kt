package dk.aau.cs.d409f19.cellumata.walkers

import dk.aau.cs.d409f19.cellumata.ast.*

interface ASTVisitor {
    fun visit(node: RootNode)

    fun visit(node: Decl)

    fun visit(node: ConstDecl)

    fun visit(node: StateDecl)

    fun visit(node: NeighbourhoodDecl)

    fun visit(node: FuncDecl)

    fun visit(node: Expr)

    fun visit(node: OrExpr)

    fun visit(node: AndExpr)

    fun visit(node: InequalityExpr)

    fun visit(node: EqualityExpr)

    fun visit(node: MoreThanExpr)

    fun visit(node: MoreEqExpr)

    fun visit(node: LessThanExpr)

    fun visit(node: LessEqExpr)

    fun visit(node: AdditionExpr)

    fun visit(node: SubtractionExpr)

    fun visit(node: MultiplicationExpr)

    fun visit(node: DivisionExpr)

    fun visit(node: PreIncExpr)

    fun visit(node: PreDecExpr)

    fun visit(node: PostIncExpr)

    fun visit(node: PostDecExpr)

    fun visit(node: PositiveExpr)

    fun visit(node: NegativeExpr)

    fun visit(node: InverseExpr)

    fun visit(node: ArrayLookupExpr)

    fun visit(node: ArrayBodyExpr)

    fun visit(node: ParenExpr)

    fun visit(node: VarExpr)

    fun visit(node: ModuloExpr)

    fun visit(node: FuncExpr)

    fun visit(node: StateIndexExpr)

    fun visit(node: IntLiteral)

    fun visit(node: BoolLiteral)

    fun visit(node: Stmt)

    fun visit(node: AssignStmt)

    fun visit(node: IfStmt)

    fun visit(node: BecomeStmt)

    fun visit(node: PreIncStmt)

    fun visit(node: PostIncStmt)

    fun visit(node: PreDecStmt)

    fun visit(ndoe: PostDecStmt)

    fun visit(node: ReturnStmt)
    fun visit(node: AST)
    fun visit(node: FloatLiteral)
    fun visit(node: ConditionalBlock)
}

abstract class BaseASTVisitor: ASTVisitor {
    override fun visit(node: AST) {
        when (node) {
            is RootNode -> visit(node)
            is WorldNode -> throw NotImplementedError()
            is Expr -> visit(node)
            is Decl -> visit(node)
            is Stmt -> visit(node)
        }
    }

    override fun visit(node: RootNode) {
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
        // no-op
    }

    override fun visit(node: FuncDecl) {
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
            is PreIncExpr -> visit(node)
            is PreDecExpr -> visit(node)
            is PostIncExpr -> visit(node)
            is PostDecExpr -> visit(node)
            is PositiveExpr -> visit(node)
            is NegativeExpr -> visit(node)
            is InverseExpr -> visit(node)
            is ArrayLookupExpr -> visit(node)
            is ArrayBodyExpr -> visit(node)
            is ParenExpr -> visit(node)
            is VarExpr -> visit(node)
            is ModuloExpr -> visit(node)
            is FuncExpr -> visit(node)
            is StateIndexExpr -> visit(node)
            is IntLiteral -> visit(node)
            is BoolLiteral -> visit(node)
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

    override fun visit(node: PreIncExpr) {
        visit(node.value)
    }

    override fun visit(node: PreDecExpr) {
        visit(node.value)
    }

    override fun visit(node: PostIncExpr) {
        visit(node.value)
    }

    override fun visit(node: PostDecExpr) {
        visit(node.value)
    }

    override fun visit(node: PositiveExpr) {
        visit(node.value)
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

    override fun visit(node: VarExpr) {
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
            is PreIncStmt -> visit(node)
            is PostIncStmt -> visit(node)
            is PreDecStmt -> visit(node)
            is PostDecStmt -> visit(node)
            is ReturnStmt -> visit(node)
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
        node.block.forEach { stmt -> visit(stmt) }
    }

    override fun visit(node: BecomeStmt) {
        visit(node.state)
    }

    override fun visit(node: PreIncStmt) {
        // no-op
    }

    override fun visit(node: PostIncStmt) {
        // no-op
    }

    override fun visit(node: PreDecStmt) {
        // no-op
    }

    override fun visit(ndoe: PostDecStmt) {
        // no-op
    }

    override fun visit(node: ReturnStmt) {
        visit(node.value)
    }
}