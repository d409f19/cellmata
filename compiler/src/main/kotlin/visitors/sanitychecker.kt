package dk.aau.cs.d409f19.cellumata.visitors


import dk.aau.cs.d409f19.cellumata.CompileError
import dk.aau.cs.d409f19.cellumata.CompileWarning
import dk.aau.cs.d409f19.cellumata.ErrorLogger
import dk.aau.cs.d409f19.cellumata.ast.*

/**
 * The error thrown when a become, continue, break or return statement is in a place where it should not be
 */
class SanityError(ctx: SourceContext, description: String) : CompileError(ctx, description)

/**
 * The error for incorrect number of dimensions for coordinates
 */
class DimensionsError(ctx: SourceContext, description: String) : CompileError(ctx, description)

/**
 * A class to check that a become, return, break and continue statement does not appear in a place it should not be
 */
class SanityChecker : BaseASTVisitor() {

    var inAFunction = false
    var dimensions: Int = 0
    var worldHasEdge = false

    override fun visit(node: WorldNode) {
        super.visit(node)
        dimensions = node.dimensions.size
        if (worldHasEdge && node.edge == null) {
            ErrorLogger += SanityError(node.ctx, "A dimension is an [edge], but the edge state was not declared.")
        }
    }

    override fun visit(node: WorldDimension) {
        super.visit(node)
        if (node.type == WorldType.EDGE) {
            worldHasEdge = true
        }
    }

    override fun visit(node: FuncDecl) {
        inAFunction = true
        super.visit(node)
        inAFunction = false
    }

    override fun visit(node: Coordinate) {
        if (dimensions != node.axes.size) {
            ErrorLogger += DimensionsError(node.ctx, "Coordinate does not match the number of dimensions declared in the world-declaration.")
        }
        super.visit(node)
    }

    // Throws an error if there is a become in a function
    override fun visit(node: BecomeStmt) {
        super.visit(node)
        if (inAFunction)
            ErrorLogger += SanityError(node.ctx, "Become statements cannot be in functions")
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
            ErrorLogger += SanityError(node.ctx, "Return statements cannot be in states")
    }

    // Throws an error if a # is found outside a state
    override fun visit(node: StateIndexExpr) {
        super.visit(node)
        if (!inAState)
            ErrorLogger += SanityError(node.ctx, "# is only allowed in states")
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
            ErrorLogger += SanityError(node.ctx, "Break statements are only allowed in loops")
    }

    // Throws an error if a continue is found outside a loop
    override fun visit(node: ContinueStmt) {
        super.visit(node)
        if (!inALoop)
            ErrorLogger += SanityError(node.ctx, "Continue statements are only allowed in loops")
    }

    // Checks if there are 0 or 1 state,
    // if there is 0 states it throws an error
    // if there is 1 state it gives a warning
    override fun visit(node: RootNode) {
        super.visit(node)

        if (numberOfStates == 0)
            ErrorLogger += SanityError(node.ctx, "A state is needed")
        else if (numberOfStates == 1)
            ErrorLogger += CompileWarning(null, "There is only one state")
    }
}
