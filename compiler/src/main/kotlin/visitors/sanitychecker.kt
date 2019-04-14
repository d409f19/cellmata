package dk.aau.cs.d409f19.cellumata.visitors


import dk.aau.cs.d409f19.cellumata.ErrorFromContext
import dk.aau.cs.d409f19.cellumata.ErrorLogger
import dk.aau.cs.d409f19.cellumata.ast.*
import org.antlr.v4.runtime.ParserRuleContext

/**
 * The error thrown when a become, continue, break or return statement is in a place where it should not be
 */
class SanityError(ctx: ParserRuleContext, description: String) : ErrorFromContext(ctx, description)

/**
 * A class to check that a become, return, break and continue statement does not appear in a place it should not be
 */
class SanityChecker : BaseASTVisitor() {

    var inAFunction = false

    override fun visit(node: FuncDecl) {
        inAFunction = true
        super.visit(node)
        inAFunction = false
    }

    // Throws an error if there is a become in a function
    override fun visit(node: BecomeStmt) {
        super.visit(node)
        if (inAFunction)
            ErrorLogger.registerError(SanityError(node.ctx, "Become statements cannot be in functions"))
    }

    var inAState = false
    var numberOfStates = 0

    override fun visit(node: StateDecl) {
        inAState = true
        super.visit(node)
        // counts the number of times a state node is visited
        numberOfStates += 1 // TODO Should count more if there is multi-states
        inAState = false
    }

    // Throws an error if there is a return in a state
    override fun visit(node: ReturnStmt) {
        super.visit(node)
        if (inAState)
            ErrorLogger.registerError(SanityError(node.ctx, "Return statements cannot be in states"))
    }

    var inALoop = false

    override fun visit(node: ForLoopStmt) {
        inALoop = true
        super.visit(node)
        inALoop = false
    }

    // Throws an error if a break is found outside a loop
    override fun visit(node: BreakStmt) {
        super.visit(node)
        if (!inALoop)
            ErrorLogger.registerError(SanityError(node.ctx, "Break statements are only allowed in loops"))
    }

    // Throws an error if a continue is found outside a loop
    override fun visit(node: ContinueStmt) {
        super.visit(node)
        if (!inALoop)
            ErrorLogger.registerError(SanityError(node.ctx, "Continue statements are only allowed in loops"))
    }

    // Checks if there are 0 or 1 state,
    // if there is 0 states it throws an error
    // if there is 1 state it gives a warning
    override fun visit(node: RootNode) {
        super.visit(node)

        if (numberOfStates == 0)
            ErrorLogger.registerError(SanityError(node.ctx, "A state is needed"))
        else if (numberOfStates == 1)
            System.err.println("Warning: There is only one state")
    }
}
