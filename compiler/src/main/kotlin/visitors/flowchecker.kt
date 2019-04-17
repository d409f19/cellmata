package dk.aau.cs.d409f19.cellumata.visitors

import dk.aau.cs.d409f19.cellumata.ErrorFromContext
import dk.aau.cs.d409f19.cellumata.ErrorLogger
import dk.aau.cs.d409f19.cellumata.ast.*

class FlowError(ctx: SourceContext, description: String) : ErrorFromContext(ctx, description)

data class Flow(var containsReturn: Boolean = false, var containsBreak: Boolean = false)

class FlowChecker : ASTVisitor<Flow> {
    override fun visit(node: AST): Flow {
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
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: WorldNode): Flow {
        node.dimensions.forEach { visit(it) }
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: WorldDimension): Flow {
        if (node.edge != null) visit(node.edge)
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: FunctionArgument): Flow {
        // no-op
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: RootNode): Flow {
        visit(node.world)
        node.body.forEach { visit(it) }
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: Decl): Flow {
        when (node) {
            is ConstDecl -> visit(node)
            is StateDecl -> visit(node)
            is NeighbourhoodDecl -> visit(node)
            is FuncDecl -> visit(node)
            is ErrorDecl -> visit(node)
        }
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: ConstDecl): Flow {
        visit(node.expr)
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: StateDecl): Flow {
        visit(node.body)
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: NeighbourhoodDecl): Flow {
        node.coords.forEach { visit(it) }
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: Coordinate): Flow {
        // no-op
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: FuncDecl): Flow {
        node.args.forEach { visit(it) }

        val flow = visit(node.body)
        if (!flow.containsReturn) {
            ErrorLogger.registerError(
                FlowError(
                    node.ctx,
                    "Function ${node.ident} not guaranteed to meet return stmt"
                )
            )
        }
        return flow
    }

    override fun visit(node: Expr): Flow {
        when (node) {
            is OrExpr -> visit(node)
            is AndExpr -> visit(node)
            is InequalityExpr -> visit(node)
            is EqualityExpr -> visit(node)
            is GreaterThanExpr -> visit(node)
            is GreaterOrEqExpr -> visit(node)
            is LessThanExpr -> visit(node)
            is LessOrEqExpr -> visit(node)
            is AdditionExpr -> visit(node)
            is SubtractionExpr -> visit(node)
            is MultiplicationExpr -> visit(node)
            is DivisionExpr -> visit(node)
            is NegationExpr -> visit(node)
            is NotExpr -> visit(node)
            is ArrayLookupExpr -> visit(node)
            is ArrayBodyExpr -> visit(node)
            is Identifier -> visit(node)
            is ModuloExpr -> visit(node)
            is FuncCallExpr -> visit(node)
            is StateIndexExpr -> visit(node)
            is IntLiteral -> visit(node)
            is FloatLiteral -> visit(node)
            is BoolLiteral -> visit(node)
            else -> throw AssertionError()
        }
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: OrExpr): Flow {
        visit(node.left)
        visit(node.right)
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: AndExpr): Flow {
        visit(node.left)
        visit(node.right)
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: InequalityExpr): Flow {
        visit(node.left)
        visit(node.right)
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: EqualityExpr): Flow {
        visit(node.left)
        visit(node.right)
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: GreaterThanExpr): Flow {
        visit(node.left)
        visit(node.right)
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: GreaterOrEqExpr): Flow {
        visit(node.left)
        visit(node.right)
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: LessThanExpr): Flow {
        visit(node.left)
        visit(node.right)
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: LessOrEqExpr): Flow {
        visit(node.left)
        visit(node.right)
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: AdditionExpr): Flow {
        visit(node.left)
        visit(node.right)
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: SubtractionExpr): Flow {
        visit(node.left)
        visit(node.right)
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: MultiplicationExpr): Flow {
        visit(node.left)
        visit(node.right)
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: DivisionExpr): Flow {
        visit(node.left)
        visit(node.right)
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: NegationExpr): Flow {
        visit(node.value)
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: NotExpr): Flow {
        visit(node.value)
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: ArrayLookupExpr): Flow {
        visit(node.arr)
        visit(node.index)
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: ArrayBodyExpr): Flow {
        node.values.forEach { visit(it) }
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: Identifier): Flow {
        // no-op
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: ModuloExpr): Flow {
        visit(node.left)
        visit(node.right)
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: FuncCallExpr): Flow {
        node.args.forEach { visit(it) }
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: StateIndexExpr): Flow {
        // no-op
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: IntLiteral): Flow {
        // no-op
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: BoolLiteral): Flow {
        // no-op
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: FloatLiteral): Flow {
        // no-op
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: Stmt): Flow {
        when (node) {
            is AssignStmt -> visit(node)
            is IfStmt -> return visit(node)
            is BecomeStmt -> visit(node)
            is ReturnStmt -> return visit(node)
            is ForLoopStmt -> visit(node)
            is BreakStmt -> visit(node)
            is ContinueStmt -> visit(node)
            is ErrorStmt -> visit(node)
        }
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: AssignStmt): Flow {
        visit(node.expr)
        return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: IfStmt): Flow {
        var elseMustContainReturn = false
        var containsReturn = false

        node.conditionals.forEach { if (visit(it).containsReturn) elseMustContainReturn = true }
        if (node.elseBlock != null) {
            containsReturn = visit(node.elseBlock).containsReturn
        }
        return Flow(elseMustContainReturn && containsReturn, false)
    }

    override fun visit(node: ConditionalBlock): Flow {
        visit(node.expr)
        return visit(node.block)
    }

    override fun visit(node: BecomeStmt): Flow {
        visit(node.state)
        return Flow(containsReturn = false, containsBreak = false)
    }


    override fun visit(node: ReturnStmt): Flow {
        visit(node.value)
        return Flow(containsReturn = true, containsBreak = false)
    }

    override fun visit(node: ForLoopStmt): Flow {
        visit(node.initPart)
        visit(node.condition)
        visit(node.postIterationPart)
        return visit(node.body)
        //return Flow(containsReturn = false, containsBreak = false)
    }

    override fun visit(node: BreakStmt): Flow {
        // no-op
        return Flow(containsReturn = false, containsBreak = true)
    }

    override fun visit(node: ContinueStmt): Flow {
        // no-op
        return Flow(containsReturn = false, containsBreak = true)
    }

    override fun visit(node: CodeBlock): Flow {
        var containsReturn = false

        node.body.forEach {
            if (visit(it).containsReturn) {
                containsReturn = true
            }
        }
        return Flow(containsReturn, false)
    }
}