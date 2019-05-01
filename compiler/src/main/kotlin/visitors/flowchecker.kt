package dk.aau.cs.d409f19.cellumata.visitors

import dk.aau.cs.d409f19.cellumata.ErrorFromContext
import dk.aau.cs.d409f19.cellumata.ErrorLogger
import dk.aau.cs.d409f19.cellumata.ast.*

class FlowError(ctx: SourceContext, description: String) : ErrorFromContext(ctx, description)


//Flow is used to show which branch contains returns/break/continue. containsBreak includes both break and continue
data class Flow(
    var containsReturn: Boolean = false,
    var containsBreak: Boolean = false
)


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
            ErrorLogger.registerError(
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

    override fun visit(node: ArrayBodyExpr): Flow {
        node.values.forEach { visit(it) }
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

    //default Flow() returns false on all fields. The specific nodes that can have true on some fields,
    //returns the value directly from the visit, such that the true values 'flows up' in the tree
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
        //flags used to determine whether or not the IfStmt should return true on its Flow() fields.
        //If any conditional (if-elif) has a return/break/continue the elseMustContainXXX is set to true
        //as the else (if it exists) should also has a return/break/continue for the entire IfStmt to return true on this
        //The containsXXX is used to show if the else has a return/break/continue
        var elseMustContainReturn = false
        var elseMustContainBreak = false

        var containsReturn = false
        var containsBreak = false
        var guaranteedExit = true


        node.conditionals.forEach {
            val flow = visit(it)

            if (flow.containsReturn) {
                elseMustContainReturn = true
            }

            if (flow.containsBreak) {
                elseMustContainBreak = true
            }

            if (!flow.containsReturn && !flow.containsBreak) {
                guaranteedExit = false
            }

        }

        if (node.elseBlock != null) {
            val flow = visit(node.elseBlock)
            containsReturn = flow.containsReturn

            containsBreak = flow.containsBreak
        } else guaranteedExit = false

        //the AND of the elseMustContainXXX and containsXXX must be true, for the IfStmt is to always meet this
        //Stmt
        return Flow(
            elseMustContainReturn && containsReturn,
            elseMustContainBreak && containsBreak || guaranteedExit
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
        visit(node.initPart)
        visit(node.condition)
        visit(node.postIterationPart)
        return visit(node.body)
    }

    //if a breakStmt is met, return true. This field represents both break and continue
    override fun visit(node: BreakStmt): Flow {
        // no-op
        return Flow(containsBreak = true)
    }

    //if a ContinueStmt is met, return true. This field represents both break and continue
    override fun visit(node: ContinueStmt): Flow {
        // no-op
        return Flow(containsBreak = true)
    }

    override fun visit(node: CodeBlock): Flow {

        var containsReturn = false
        var containsBreak = false
        var returnPossible = false

        //visit all Stmts in the body
        for (i in 0 until node.body.size) {

            //save values to be used to determine if any warnings should be produced
            //val isForLoop = node.body[i] is ForLoopStmt
            val flow = visit(node.body[i])

            //codeAfterThisBlock asserts that there is at least 2 Stmts in the body. No warnings will be
            //produced if the current Stmt is the last
            val codeAfterThisBlock = i < node.body.size - 1 || i == 0 && 1 < node.body.size

            //saves the values from the visit()
            if (flow.containsBreak) {
                containsBreak = true
            }

            if (flow.containsReturn) {
                //returnPossible is to be used to determine if the entire body has the possibility to return
                returnPossible = true
                //containsReturn can be changed in the for-loop
                containsReturn = true
            }

            //makes sure that the current Stmt is not the last, else no Stmts will be blocked by a
            //return/break/continue
            if (codeAfterThisBlock) when {
                //if the visited node has a return that blocks further code in the block, produce error
                //the block producing this warnings might be nested, so the containsReturn value is set to false,
                //as to not produce more warnings referring to the same returnStmt
                containsReturn -> {
                    println(
                        "Warning ${node.body[i].ctx.lineNumber}:${node.body[i].ctx.charPositionInLine} " +
                                ": Code after statement never met, reason: return statement"
                    )
                    return if (i == 0) Flow(containsReturn = true) else Flow(containsReturn = false)
                }
                //if the visisted node has a break/continue that blocks further code in the block, produce error
                //the block producing this warnings might be nested, so the containsBreak value is set to false,
                //as to not produce more warnings referring to the same break/continueStmt
                containsBreak && node.body[i] !is ForLoopStmt -> {
                    println(
                        "Warning ${node.body[i].ctx.lineNumber}:${node.body[i].ctx.charPositionInLine} " +
                                ": Code after statement never met, reason: break/continue statement"
                    )
                    return Flow(containsReturn = returnPossible, containsBreak = false)
                }
            }
        }
        return Flow(returnPossible, containsBreak)
    }


    /*
        override fun visit(node: CodeBlock): Flow {

        var containsReturn = false
        var containsBreak = false
        var returnPossible = false

        //visit all Stmts in the body
        for (i in 0 until node.body.size) {

            //save values to be used to determine if any warnings should be produced
            //val isForLoop = node.body[i] is ForLoopStmt
            val flow = visit(node.body[i])

            //codeAfterThisBlock asserts that there is at least 2 Stmts in the body. No warnings will be
            //produced if the current Stmt is the last
            val codeAfterThisBlock = i < node.body.size - 1 || i == 0 && 1 < node.body.size

            //saves the values from the visit()
            if (flow.containsBreak) {
                containsBreak = true
            }

            if (flow.containsReturn) {
                //returnPossible is to be used to determine if the entire body has the possibility to return
                returnPossible = true
                //containsReturn can be changed in the for-loop
                containsReturn = true
            }

            //makes sure that the current Stmt is not the last, else no Stmts will be blocked by a
            //return/break/continue
            if (codeAfterThisBlock) when {
                //if the visisted IfStmt is guaranteed to exit, produce error that refers to start of entire
                // IfStmt, instead of a specific return/break/continue
                flow.guaranteeExit -> {
                    println(
                        "Warning ${node.body[i].ctx.lineNumber}:${node.body[i].ctx.charPositionInLine} " +
                                ": Code in the block after if block on line ${node.body[i].ctx.lineNumber} never met"
                    )
                }
                //if the visisted node has a return that blocks further code in the block, produce error
                //the block producing this warnings might be nested, so the containsReturn value is set to false,
                //as to not produce more warnings referring to the same returnStmt
                containsReturn -> {
                    println(
                        "Warning ${node.body[i].ctx.lineNumber}:${node.body[i].ctx.charPositionInLine} " +
                                ": Code after statement never met, reason: return"
                    )
                    containsReturn = false
                }
                //if the visisted node has a break/continue that blocks further code in the block, produce error
                //the block producing this warnings might be nested, so the containsBreak value is set to false,
                //as to not produce more warnings referring to the same break/continueStmt
                containsBreak && node.body[i] !is ForLoopStmt -> {
                    println(
                        "Warning ${node.body[i].ctx.lineNumber}:${node.body[i].ctx.charPositionInLine} " +
                                ": Code after statement never met, reason: break"
                    )
                    containsBreak = false
                }
            }
        }

        return Flow(returnPossible, containsBreak)
    }
     */
}