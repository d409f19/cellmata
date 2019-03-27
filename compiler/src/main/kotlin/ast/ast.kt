package dk.aau.cs.d409f19.cellumata.ast

import dk.aau.cs.d409f19.antlr.CellmataParser
import org.antlr.v4.runtime.tree.ParseTree

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
sealed class AST

/**
 * RootNode represents a .cell file
 */
data class RootNode(val world: WorldNode, val body: List<Decl>) : AST()

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
    val ctx: ParseTree,
    val dimensions: List<WorldDimension> = emptyList(),
    val tickrate: Int? = null,
    val cellSize: Int? = null
) : AST()

/*
 * Expressions
 */
/**
 * An expression is either a literal, or some operation that produces a value.
 */
abstract class Expr(type: Type? = UncheckedType) : AST(), TypedNode {
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

data class OrExpr(val ctx: CellmataParser.OrExprContext, val left: Expr, val right: Expr) : Expr()

data class AndExpr(val ctx: CellmataParser.AndExprContext, val left: Expr, val right: Expr) : Expr()

data class InequalityExpr(val ctx: CellmataParser.NotEqExprContext, val left: Expr, val right: Expr) : Expr()

data class EqualityExpr(val ctx: CellmataParser.EqExprContext, val left: Expr, val right: Expr) : Expr()

data class MoreThanExpr(val ctx: CellmataParser.MoreExprContext, val left: Expr, val right: Expr) : Expr()

data class MoreEqExpr(val ctx: CellmataParser.MoreEqExprContext, val left: Expr, val right: Expr) : Expr()

data class LessThanExpr(val ctx: CellmataParser.LessExprContext, val left: Expr, val right: Expr) : Expr()

data class LessEqExpr(val ctx: CellmataParser.LessEqExprContext, val left: Expr, val right: Expr) : Expr()

data class AdditionExpr(val ctx: CellmataParser.AdditionExprContext, val left: Expr, val right: Expr) : Expr()

data class SubtractionExpr(val ctx: CellmataParser.SubstractionExprContext, val left: Expr, val right: Expr) : Expr()

data class MultiplicationExpr(val ctx: CellmataParser.MultiplictionExprContext, val left: Expr, val right: Expr) : Expr()

data class DivisionExpr(val ctx: CellmataParser.DivisionExprContext, val left: Expr, val right: Expr) : Expr()

data class PreIncExpr(val ctx: CellmataParser.PreIncExprContext, val value: Expr) : Expr()

data class PreDecExpr(val ctx: CellmataParser.PreDecExprContext, val value: Expr) : Expr()

data class PostIncExpr(val ctx: CellmataParser.PostIncExprContext, val value: Expr) : Expr()

data class PostDecExpr(val ctx: CellmataParser.PostDecExprContext, val value: Expr) : Expr()

data class PositiveExpr(val ctx: CellmataParser.PositiveExprContext, val value: Expr) : Expr()

data class NegativeExpr(val ctx: CellmataParser.NegativeExprContext, val value: Expr) : Expr()

data class InverseExpr(val ctx: CellmataParser.InverseExprContext, val value: Expr) : Expr()

data class ArrayLookupExpr(
    val ctx: CellmataParser.ArrayLookupExprContext,
    val ident: String = MAGIC_UNDEFINED_STRING,
    val index: Expr
) : Expr()

/**
 * @param ctx Node in the parse tree this node corresponds to.
 * @param values Elements of the array.
 * @param declaredType Type listed before the body/values of the array.
 */
data class ArrayBodyExpr(
    val ctx: CellmataParser.ArrayValueExprContext,
    val values: List<Expr>,
    val declaredType: Type? = UncheckedType
) : Expr()

data class ParenExpr(val ctx: CellmataParser.ParenExprContext, val expr: Expr) : Expr()

data class NamedExpr(val ctx: ParseTree, var ident: String = MAGIC_UNDEFINED_STRING) : Expr()

data class ModuloExpr(val ctx: CellmataParser.ModuloExprContext, val left: Expr, val right: Expr) : Expr()

/**
 * Presents a function call in abstract syntax tree
 */
data class FuncExpr(
    val ctx: CellmataParser.FuncExprContext,
    val args: List<Expr>,
    var ident: String = MAGIC_UNDEFINED_STRING
) : Expr()

data class StateIndexExpr(val ctx: CellmataParser.StateIndexExprContext) : Expr(type = IntegerType)

// Literals
/**
 * Represents a concrete integer value in the abstract syntax tree
 */
data class IntLiteral(val ctx: CellmataParser.Integer_literalContext, var value: Int = -1) : Expr(type = IntegerType)

/**
 * Represents a concrete boolean value in the abstract syntax tree
 */
data class BoolLiteral(val ctx: CellmataParser.Bool_literalContext, var value: Boolean = false) : Expr(type = BooleanType)

/**
 * Represent a concrete float value in the abstract syntax tree
 */
data class FloatLiteral(val ctx: CellmataParser.Float_literalContext, var value: Float = 0.0F) : Expr(type = FloatType)

// Type conversion
/**
 * Internal node used to present an implicit conversion from integer to float.
 */
data class IntToFloatConversion(val expr: Expr) : Expr()

/**
 * Internal node used to represent an implicit conversion from a array of states to a neighbourhood.
 */
data class StateArrayToActualNeighbourhoodConversion(val expr: Expr) : Expr()

/*
 * Declarations
 */
/**
 * Represents global declarations like states, constants and functions
 */
sealed class Decl : AST() // state, const, func

/**
 * Represents constants declarations
 */
data class ConstDecl(
    val ctx: CellmataParser.Const_declContext,
    var ident: String = MAGIC_UNDEFINED_STRING,
    val expr: Expr,
    var type: Type? = UncheckedType
) : Decl()

/**
 * Represent a state declaration
 */
data class StateDecl(
    val ctx: CellmataParser.State_declContext,
    var ident: String = MAGIC_UNDEFINED_STRING,
    var red: Short = -1,
    var blue: Short = -1,
    var green: Short = -1,
    val body: List<Stmt>
) : Decl()

/**
 * Represents a single point in a neighbourhood declaration
 */
data class Coordinate(val ctx: ParseTree, val axes: List<Int> = emptyList())

/**
 * Represents a neighbourhood declaration
 */
data class NeighbourhoodDecl(
    val ctx: CellmataParser.Neighbourhood_declContext,
    var ident: String = MAGIC_UNDEFINED_STRING,
    var coords: List<Coordinate> = emptyList()
) : Decl()

/**
 * Represent a single argument in a function declaration
 *
 * @see FuncDecl
 */
data class FunctionArgs(val ident: String, private var type: Type?) : AST(), TypedNode {
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
    val ctx: CellmataParser.Func_declContext,
    var ident: String = MAGIC_UNDEFINED_STRING,
    var args: List<FunctionArgs> = emptyList(),
    val body: List<Stmt> = emptyList(),
    var returnType: Type = UncheckedType
) : Decl()

/*
 * Statements
 */
/**
 * Base class for classes represents a single statement/action in a block of code
 */
sealed class Stmt : AST()

/**
 * Represent a variable declaration and/or assignment
 *
 * @param ctx The node in the parse tree that this node corresponds to
 * @param ident Name of the variable
 * @param expr New value of the variable
 * @param type Type of the variable
 */
data class AssignStmt(
    val ctx: CellmataParser.AssignmentContext,
    var ident: String = MAGIC_UNDEFINED_STRING,
    val expr: Expr,
    private var type: Type? = UncheckedType
) : Stmt(), TypedNode {
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
data class ConditionalBlock(val ctx: ParseTree, val expr: Expr, val block: List<Stmt>)

/**
 * Represent an entire if statement, including the if, the elif and the else blocks.
 */
data class IfStmt(
    val ctx: CellmataParser.If_stmtContext,
    val conditionals: List<ConditionalBlock>,
    val elseBlock: List<Stmt>?
) : Stmt()

data class ForStmt(val ctx: CellmataParser.For_stmtContext, val initPart: AssignStmt, val condition: Expr, val postIterationPart: AssignStmt) : Stmt()

data class BreakStmt(val ctx: CellmataParser.Break_stmtContext) : Stmt()

data class ContinueStmt(val ctx: CellmataParser.Continue_stmtContext) : Stmt()

/**
 * Represents a become statement.
 * The become statements terminates a state block, and changes the state of the cell being evaluated.
 */
data class BecomeStmt(val ctx: CellmataParser.Become_stmtContext, val state: Expr) : Stmt()

data class PreIncStmt(val ctx: CellmataParser.PreIncStmtContext, val variable: Expr) : Stmt()

data class PostIncStmt(val ctx: CellmataParser.PostIncStmtContext, val variable: Expr) : Stmt()

data class PreDecStmt(val ctx: CellmataParser.PreDecStmtContext, val variable: Expr) : Stmt()

data class PostDecStmt(val ctx: CellmataParser.PostDecStmtContext, val variable: Expr) : Stmt()

/**
 * Represents a return statement.
 * The return statement terminates a function call and returns a value to the caller.
 */
data class ReturnStmt(val ctx: CellmataParser.Return_stmtContext, val value: Expr) : Stmt()
