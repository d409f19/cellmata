package dk.aau.cs.d409f19.cellumata.visitors

import dk.aau.cs.d409f19.cellumata.CompileError
import dk.aau.cs.d409f19.cellumata.CompileWarning
import dk.aau.cs.d409f19.cellumata.ErrorLogger
import dk.aau.cs.d409f19.cellumata.ast.*

class FlowWarning(ctx: SourceContext, description: String) : CompileWarning(ctx, description)
class FlowError(ctx: SourceContext, description: String) : CompileError(ctx, description)

//Flow is used to show which branch contains returns/break/continue. containsBreak includes both break and continue
data class Flow(
    var containsReturn: Boolean = false,
    var containsBreak: Boolean = false
)


class FlowChecker : ASTVisitor<Flow> {

    override fun visit(node: BinaryExpr): Flow {
        when (node) {
            is BinaryArithmeticExpr -> visit(node)
            is BinaryBooleanExpr -> visit(node)
            is NumericComparisonExpr -> visit(node)
        }
        return Flow()
    }

    override fun visit(node: BinaryArithmeticExpr): Flow {
        when (node) {
            is AdditionExpr -> visit(node)
            is SubtractionExpr -> visit(node)
            is MultiplicationExpr -> visit(node)
            is DivisionExpr -> visit(node)
            is ModuloExpr -> visit(node)
        }
        return Flow()
    }

    override fun visit(node: BinaryBooleanExpr): Flow {
        when (node) {
            is OrExpr -> visit(node)
            is AndExpr -> visit(node)
        }
        return Flow()
    }

    override fun visit(node: NumericComparisonExpr): Flow {
        when (node) {
            is InequalityExpr -> visit(node)
            is EqualityExpr -> visit(node)
            is GreaterThanExpr -> visit(node)
            is GreaterOrEqExpr -> visit(node)
            is LessThanExpr -> visit(node)
            is LessOrEqExpr -> visit(node)
        }
        return Flow()
    }

    override fun visit(node: SizedArrayExpr): Flow {
        if (node.body == null) {
            return Flow()
        }
        return visit(node.body)
    }

    override fun visit(node: ArrayLiteralExpr): Flow {
        node.values.forEach { visit(it) }
        return Flow()
    }

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
        return Flow()
    }

    override fun visit(node: WorldNode): Flow {
        node.dimensions.forEach { visit(it) }
        return Flow()
    }

    override fun visit(node: WorldDimension): Flow {
        if (node.edge != null) visit(node.edge)
        return Flow()
    }

    override fun visit(node: FunctionArgument): Flow {
        // no-op
        return Flow()
    }

    override fun visit(node: RootNode): Flow {
        visit(node.world)
        node.body.forEach { visit(it) }
        return Flow()
    }

    override fun visit(node: Decl): Flow {
        when (node) {
            is ConstDecl -> visit(node)
            is StateDecl -> visit(node)
            is NeighbourhoodDecl -> visit(node)
            is FuncDecl -> visit(node)
            is ErrorDecl -> visit(node)
        }
        return Flow()
    }

    override fun visit(node: ConstDecl): Flow {
        return visit(node.expr)
    }

    override fun visit(node: StateDecl): Flow {
        return visit(node.body)
    }

    override fun visit(node: NeighbourhoodDecl): Flow {
        node.coords.forEach { visit(it) }
        return Flow()
    }

    override fun visit(node: Coordinate): Flow {
        // no-op
        return Flow()
    }

    override fun visit(node: FuncDecl): Flow {
        node.args.forEach { visit(it) }
        val flow = visit(node.body)
        //If the body of the function is not guaranteed to meet return, register error
        if (!flow.containsReturn) {
            ErrorLogger += (
                    FlowError(
                        node.ctx,
                        "Function ${node.ident} not guaranteed to meet return statement"
                    )
                    )
        }
        return Flow()
    }

    override fun visit(node: Expr): Flow {
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
        return Flow()
    }

    override fun visit(node: OrExpr): Flow {
        visit(node.left)
        visit(node.right)
        return Flow()
    }

    override fun visit(node: AndExpr): Flow {
        visit(node.left)
        visit(node.right)
        return Flow()
    }

    override fun visit(node: InequalityExpr): Flow {
        visit(node.left)
        visit(node.right)
        return Flow()
    }

    override fun visit(node: EqualityExpr): Flow {
        visit(node.left)
        visit(node.right)
        return Flow()
    }

    override fun visit(node: GreaterThanExpr): Flow {
        visit(node.left)
        visit(node.right)
        return Flow()
    }

    override fun visit(node: GreaterOrEqExpr): Flow {
        visit(node.left)
        visit(node.right)
        return Flow()
    }

    override fun visit(node: LessThanExpr): Flow {
        visit(node.left)
        visit(node.right)
        return Flow()
    }

    override fun visit(node: LessOrEqExpr): Flow {
        visit(node.left)
        visit(node.right)
        return Flow()
    }

    override fun visit(node: AdditionExpr): Flow {
        visit(node.left)
        visit(node.right)
        return Flow()
    }

    override fun visit(node: SubtractionExpr): Flow {
        visit(node.left)
        visit(node.right)
        return Flow()
    }

    override fun visit(node: MultiplicationExpr): Flow {
        visit(node.left)
        visit(node.right)
        return Flow()
    }

    override fun visit(node: DivisionExpr): Flow {
        visit(node.left)
        visit(node.right)
        return Flow()
    }

    override fun visit(node: NegationExpr): Flow {
        return visit(node.value)
    }

    override fun visit(node: NotExpr): Flow {
        return visit(node.value)
    }

    override fun visit(node: ArrayLookupExpr): Flow {
        visit(node.arr)
        visit(node.index)
        return Flow()
    }

    override fun visit(node: Identifier): Flow {
        // no-op
        return Flow()
    }

    override fun visit(node: ModuloExpr): Flow {
        visit(node.left)
        visit(node.right)
        return Flow()
    }

    override fun visit(node: FuncCallExpr): Flow {
        node.args.forEach { visit(it) }
        return Flow()
    }

    override fun visit(node: StateIndexExpr): Flow {
        // no-op
        return Flow()
    }

    override fun visit(node: IntLiteral): Flow {
        // no-op
        return Flow()
    }

    override fun visit(node: BoolLiteral): Flow {
        // no-op
        return Flow()
    }

    override fun visit(node: FloatLiteral): Flow {
        // no-op
        return Flow()
    }

    /*default Flow() returns false on all fields. The specific nodes that can have true on some fields,
    returns the value directly from the visit, such that the true values 'flows up' in the tree*/
    override fun visit(node: Stmt): Flow {
        when (node) {
            is AssignStmt -> visit(node)
            is IfStmt -> return visit(node)
            is BecomeStmt -> visit(node)
            is ReturnStmt -> return visit(node)
            is ForLoopStmt -> return visit(node)
            is BreakStmt -> return visit(node)
            is ContinueStmt -> return visit(node)
            is ErrorStmt -> visit(node)
        }
        return Flow()
    }

    override fun visit(node: AssignStmt): Flow {
        return visit(node.expr)
    }

    override fun visit(node: IfStmt): Flow {
        /*flags used to determine whether or not the IfStmt should return true on its Flow() fields.
        guaranteedExit used because an IfStmt can be guaranteed to exit a function with either Break/return,
        while neither containsReturn or containsBreak are true.
        Assumes all variables to be true, and set to false if a branch without return/break/continue is met*/
        var containsReturn = true
        var containsBreak = true
        var guaranteedExit = true


        node.conditionals.forEach {
            val flow = visit(it)

            if (!flow.containsReturn) {
                containsReturn = false
            }

            if (!flow.containsBreak) {
                containsBreak = false
            }

            if (!flow.containsReturn && !flow.containsBreak) {
                guaranteedExit = false
            }
        }

        if (node.elseBlock != null) {
            val flow = visit(node.elseBlock)

            if (!flow.containsReturn) {
                containsReturn = false
            }

            if (!flow.containsBreak) {
                containsBreak = false
            }
        } else {
            //if no elseBlock exists, the IfStmt can not guarantee to exit with either return or break
            guaranteedExit = false
            containsReturn = false
            containsBreak = false
        }

        return Flow(
            containsReturn,
            containsBreak || guaranteedExit
        )
    }

    override fun visit(node: ConditionalBlock): Flow {
        visit(node.expr)
        return visit(node.block)
    }

    override fun visit(node: BecomeStmt): Flow {
        return visit(node.state)
    }

    //if a ReturnStmt is met, return true
    override fun visit(node: ReturnStmt): Flow {
        visit(node.value)
        return Flow(containsReturn = true)
    }

    override fun visit(node: ForLoopStmt): Flow {
        node.initPart?.let { visit(it) }
        visit(node.condition)
        node.postIterationPart?.let { visit(it) }
        visit(node.body)
        return Flow()
    }

    //if a breakStmt is met, return true. containsBreak field represents both break and continue
    override fun visit(node: BreakStmt): Flow {
        // no-op
        return Flow(containsBreak = true)
    }

    //if a ContinueStmt is met, return true. containsBreak field represents both break and continue
    override fun visit(node: ContinueStmt): Flow {
        // no-op
        return Flow(containsBreak = true)
    }

    override fun visit(node: CodeBlock): Flow {
        //variables used to
        var containsReturn = false
        var containsBreak = false

        //visit all Stmts in the body, index used to print line number
        for (i in 0 until node.body.size) {

            val flow = visit(node.body[i])

            /*codeAfterThisBlock asserts that there is at least 2 Stmts in the body. No warnings will be
            produced if the current Stmt is the last, as it can not possibly block any further code inside the block*/
            val codeAfterThisBlock = i < node.body.size - 1

            //saves the values from the visit()
            if (flow.containsBreak) {
                containsBreak = true
            }

            if (flow.containsReturn) {
                containsReturn = true
            }

            /*makes sure that the current Stmt is not the last, else no Stmts will be blocked by a
            return/break/continue*/
            if (codeAfterThisBlock) when {
                //if the visited node has a return that blocks further code in the block, produce warning
                containsReturn -> {
                    ErrorLogger += FlowWarning(
                        node.body[i].ctx,
                        "Code after statement never met, reason: return statement"
                    )
                    return Flow(containsReturn)
                }
                //if the visited node has a break/continue that blocks further code in the block, produce error
                containsBreak && node.body[i] !is ForLoopStmt -> {
                    ErrorLogger += FlowWarning(
                        node.body[i].ctx,
                        "Code after statement never met, reason: break/continue statement"
                    )
                    return Flow(containsReturn, containsBreak)
                }
            }
        }
        return Flow(containsReturn, containsBreak)
    }
}