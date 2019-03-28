package dk.aau.cs.d409f19.cellumata.ast

import dk.aau.cs.d409f19.antlr.CellmataParser
import org.antlr.v4.runtime.ParserRuleContext

/**
 * This is a debugging string.
 * If you see this string in running code it means that the variable hasn't been initialized.
 */
const val MAGIC_UNDEFINED_STRING = "<<THIS IS A MAGIC STRING, UNDEFINED>>"

/**
 * This class is the basis for all nodes in the abstract syntax tree.
 *
 * It's sealed such that it's possible to use Kotlin's when statement without an else.
 */
sealed class AST(open val ctx: ParserRuleContext)

/**
 * RootNode represents a .cell file
 */
data class RootNode(
    override val ctx: ParserRuleContext,
    val world: WorldNode,
    val body: List<Decl>
) : AST(ctx)


/*
 * World definition
 */
/**
 * The types of world bounds that are supported.
 */
enum class WorldType {
    /**
     * WRAPPING indicate that if a CA tries read beyond the world limit its lookup will automatically be from the other side of the world.
     */
    WRAPPING,
    /**
     * EDGE indicates that all cells beyond the world limit has a fixed state.
     */
    EDGE,
    /**
     * UNDEFINED is a compiler internal value used to indicate that no type has been parsed yet.
     */
    UNDEFINED
}

/**
 * Nodes that hold a value or that can produce a value should implement TypedNode to expose the type of that value
 */
interface TypedNode {
    fun getType(): Type?
    fun setType(type: Type?)
}

data class WorldDimension(val size: Int = -1, val type: WorldType = WorldType.UNDEFINED)

/**
 * WorldNode represent the world definition.
 */
data class WorldNode(
    override val ctx: CellmataParser.World_dclContext,
    val dimensions: List<WorldDimension> = emptyList(),
    val tickrate: Int? = null,
    val cellSize: Int? = null
) : AST(ctx)

/*
 * Expressions
 */
/**
 * An expression is either a literal, or some operation that produces a value.
 */
abstract class Expr(override val ctx: ParserRuleContext, type: Type? = UncheckedType) : AST(ctx), TypedNode {
    private var atype: Type?

    init {
        atype = type
    }

    override fun getType(): Type? {
        return this.atype
    }

    override fun setType(type: Type?) {
        this.atype = type
    }
}

data class OrExpr(override val ctx: CellmataParser.OrExprContext, val left: Expr, val right: Expr) : Expr(ctx)

data class AndExpr(override val ctx: CellmataParser.AndExprContext, val left: Expr, val right: Expr) : Expr(ctx)

data class InequalityExpr(override val ctx: CellmataParser.NotEqExprContext, val left: Expr, val right: Expr) : Expr(ctx)

data class EqualityExpr(override val ctx: CellmataParser.EqExprContext, val left: Expr, val right: Expr) : Expr(ctx)

data class MoreThanExpr(override val ctx: CellmataParser.MoreExprContext, val left: Expr, val right: Expr) : Expr(ctx)

data class MoreEqExpr(override val ctx: CellmataParser.MoreEqExprContext, val left: Expr, val right: Expr) : Expr(ctx)

data class LessThanExpr(override val ctx: CellmataParser.LessExprContext, val left: Expr, val right: Expr) : Expr(ctx)

data class LessEqExpr(override val ctx: CellmataParser.LessEqExprContext, val left: Expr, val right: Expr) : Expr(ctx)

data class AdditionExpr(override val ctx: CellmataParser.AdditionExprContext, val left: Expr, val right: Expr) : Expr(ctx)

data class SubtractionExpr(override val ctx: CellmataParser.SubstractionExprContext, val left: Expr, val right: Expr) : Expr(ctx)

data class MultiplicationExpr(override val ctx: CellmataParser.MultiplictionExprContext, val left: Expr, val right: Expr) : Expr(ctx)

data class DivisionExpr(override val ctx: CellmataParser.DivisionExprContext, val left: Expr, val right: Expr) : Expr(ctx)

data class NegativeExpr(override val ctx: CellmataParser.NegativeExprContext, val value: Expr) : Expr(ctx)

data class InverseExpr(override val ctx: CellmataParser.InverseExprContext, val value: Expr) : Expr(ctx)

data class ArrayLookupExpr(
    override val ctx: CellmataParser.ArrayLookupExprContext,
    val ident: String = MAGIC_UNDEFINED_STRING,
    val index: Expr
) : Expr(ctx)

/**
 * @param ctx Node in the parse tree this node corresponds to.
 * @param values Elements of the array.
 * @param declaredType Type listed before the body/values of the array.
 */
data class ArrayBodyExpr(
    override val ctx: CellmataParser.ArrayValueExprContext,
    val values: List<Expr>,
    val declaredType: Type? = UncheckedType
) : Expr(ctx)

data class ParenExpr(override val ctx: CellmataParser.ParenExprContext, val expr: Expr) : Expr(ctx)

data class NamedExpr(override val ctx: ParserRuleContext, var ident: String = MAGIC_UNDEFINED_STRING) : Expr(ctx)

data class ModuloExpr(override val ctx: CellmataParser.ModuloExprContext, val left: Expr, val right: Expr) : Expr(ctx)

/**
 * Presents a function call in abstract syntax tree
 */
data class FuncExpr(
    override val ctx: CellmataParser.FuncExprContext,
    val args: List<Expr>,
    var ident: String = MAGIC_UNDEFINED_STRING
) : Expr(ctx)

data class StateIndexExpr(override val ctx: CellmataParser.StateIndexExprContext) : Expr(ctx, IntegerType)

// Literals
/**
 * Represents a concrete integer value in the abstract syntax tree
 */
data class IntLiteral(override val ctx: CellmataParser.Integer_literalContext, var value: Int = -1) : Expr(ctx, IntegerType)

/**
 * Represents a concrete boolean value in the abstract syntax tree
 */
data class BoolLiteral(override val ctx: CellmataParser.Bool_literalContext, var value: Boolean = false) : Expr(ctx, BooleanType)

/**
 * Represent a concrete float value in the abstract syntax tree
 */
data class FloatLiteral(override val ctx: CellmataParser.Float_literalContext, var value: Float = 0.0F): Expr(ctx, FloatType)

// Type conversion
/**
 * Internal node used to present an implicit conversion from integer to float.
 */
data class IntToFloatConversion(override val ctx: ParserRuleContext, val expr: Expr) : Expr(ctx)

/**
 * Internal node used to represent an implicit conversion from a array of states to a neighbourhood.
 */
data class StateArrayToActualNeighbourhoodConversion(override val ctx: ParserRuleContext, val expr: Expr) : Expr(ctx)

/*
 * Declarations
 */
/**
 * Represents global declarations like states, constants and functions
 */
sealed class Decl(override val ctx: ParserRuleContext) : AST(ctx) // state, const, func

/**
 * Represents constants declarations
 */
data class ConstDecl(
    override val ctx: CellmataParser.Const_declContext,
    var ident: String = MAGIC_UNDEFINED_STRING,
    val expr: Expr,
    var type: Type? = UncheckedType
) : Decl(ctx)

/**
 * Represent a state declaration
 */
data class StateDecl(
    override val ctx: CellmataParser.State_declContext,
    var ident: String = MAGIC_UNDEFINED_STRING,
    var red: Short = -1,
    var blue: Short = -1,
    var green: Short = -1,
    val body: List<Stmt>
) : Decl(ctx)

/**
 * Represents a single point in a neighbourhood declaration
 */
data class Coordinate(override val ctx: ParserRuleContext, val axes: List<Int> = emptyList()) : AST(ctx)

/**
 * Represents a neighbourhood declaration
 */
data class NeighbourhoodDecl(
    override val ctx: CellmataParser.Neighbourhood_declContext,
    var ident: String = MAGIC_UNDEFINED_STRING,
    var coords: List<Coordinate> = emptyList()
) : Decl(ctx)

/**
 * Represent a single argument in a function declaration
 *
 * @see FuncDecl
 */
data class FunctionArgs(override val ctx: ParserRuleContext, val ident: String, private var type: Type?) : AST(ctx), TypedNode {
    override fun getType(): Type? {
        return type
    }

    override fun setType(type: Type?) {
        this.type = type
    }
}

/**
 * Represents a function declaration
 */
data class FuncDecl(
    override val ctx: CellmataParser.Func_declContext,
    var ident: String = MAGIC_UNDEFINED_STRING,
    var args: List<FunctionArgs> = emptyList(),
    val body: List<Stmt> = emptyList(),
    var returnType: Type = UncheckedType
) : Decl(ctx)

/*
 * Statements
 */
/**
 * Base class for classes represents a single statement/action in a block of code
 */
sealed class Stmt(override val ctx: ParserRuleContext) : AST(ctx)

/**
 * Represent a variable declaration and/or assignment
 *
 * @param ctx The node in the parse tree that this node corresponds to
 * @param ident Name of the variable
 * @param expr New value of the variable
 * @param type Type of the variable
 */
data class AssignStmt(
    override val ctx: CellmataParser.AssignmentContext,
    var ident: String = MAGIC_UNDEFINED_STRING,
    val expr: Expr,
    private var type: Type? = UncheckedType
) : Stmt(ctx), TypedNode {
    override fun getType(): Type? {
        return type
    }

    override fun setType(type: Type?) {
        this.type = type
    }
}

/**
 * Represent a block in a if statement that is to be run if expr evaluates to true.
 * A ConditionalBlock can represent either the if or the elif part of an if statement.
 */
data class ConditionalBlock(override val ctx: ParserRuleContext, val expr: Expr, val block: List<Stmt>) : AST(ctx)

/**
 * Represent an entire if statement, including the if, the elif and the else blocks.
 */
data class IfStmt(
    override val ctx: CellmataParser.If_stmtContext,
    val conditionals: List<ConditionalBlock>,
    val elseBlock: List<Stmt>?
) : Stmt(ctx)

data class ForStmt(override val ctx: CellmataParser.For_stmtContext, val initPart: AssignStmt, val condition: Expr, val postIterationPart: AssignStmt) : Stmt(ctx)

data class BreakStmt(override val ctx: CellmataParser.Break_stmtContext) : Stmt(ctx)

data class ContinueStmt(override val ctx: CellmataParser.Continue_stmtContext) : Stmt(ctx)

/**
 * Represents a become statement.
 * The become statements terminates a state block, and changes the state of the cell being evaluated.
 */
data class BecomeStmt(override val ctx: CellmataParser.Become_stmtContext, val state: Expr) : Stmt(ctx)

/**
 * Represents a return statement.
 * The return statement terminates a function call and returns a value to the caller.
 */
data class ReturnStmt(override val ctx: CellmataParser.Return_stmtContext, val value: Expr) : Stmt(ctx)

/*
 * Error nodes are used when something goes wrong in reduce.kt and is returned by the failing function.
 */
class ErrorAST(override val ctx: ParserRuleContext) : AST(ctx)
class ErrorDecl(override val ctx: ParserRuleContext) : Decl(ctx)
class ErrorStmt(override val ctx: ParserRuleContext) : Stmt(ctx)
class ErrorExpr(override val ctx: ParserRuleContext) : Expr(ctx)