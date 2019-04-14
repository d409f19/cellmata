package dk.aau.cs.d409f19.cellumata.ast

import dk.aau.cs.d409f19.antlr.CellmataParser
import org.antlr.v4.runtime.ParserRuleContext

/**
 * This is a debugging string.
 * If you see this string in running code it means that the variable hasn't been initialized.
 */
const val MAGIC_UNDEFINED_STRING = "<<THIS IS A MAGIC STRING, UNDEFINED>>"

/**
 * TODO
 */
interface NodeFromContext<out T : ParserRuleContext> {
    fun getContext() : T
}

/**
 * Nodes that hold a value or that can produce a value should implement TypedNode to expose the type of that value
 */
interface TypedNode {
    fun getType(): Type?
    fun setType(type: Type?)
}

/**
 * This class is the basis for all nodes in the abstract syntax tree.
 * It's sealed such that it's possible to use Kotlin's when statement without an else.
 */
sealed class AST

/**
 * RootNode represents a .cell file
 */
class RootNode(
    val world: WorldNode,
    val body: List<Decl>
) : AST()


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

data class WorldDimension(val size: Int, val type: WorldType, val edge: Identifier?)

/**
 * WorldNode represent the world definition.
 */
class WorldNode(
    val ctx: CellmataParser.World_dclContext,
    var dimensions: List<WorldDimension>,
    var tickrate: Int?,
    var cellSize: Int?
) : AST(), NodeFromContext<CellmataParser.World_dclContext> {
    override fun getContext() = ctx
}

/*
 * Expressions
 */
/**
 * An expression is either a literal, an identifier, or some operation that produces a value.
 */
abstract class Expr(private var type: Type? = UncheckedType) : AST(), TypedNode {
    override fun getType(): Type? = type
    override fun setType(type: Type?) { this.type = type }
}

class OrExpr(
    val ctx: CellmataParser.OrExprContext,
    val left: Expr,
    val right: Expr
) : Expr(), NodeFromContext<CellmataParser.OrExprContext> {
    override fun getContext() = ctx
}

class AndExpr(
    val ctx: CellmataParser.AndExprContext,
    val left: Expr,
    val right: Expr
) : Expr(), NodeFromContext<CellmataParser.AndExprContext> {
    override fun getContext() = ctx
}

class InequalityExpr(
    val ctx: CellmataParser.NotEqExprContext,
    val left: Expr,
    val right: Expr
) : Expr(), NodeFromContext<CellmataParser.NotEqExprContext> {
    override fun getContext() = ctx
}

class EqualityExpr(
    val ctx: CellmataParser.EqExprContext,
    val left: Expr,
    val right: Expr
) : Expr(), NodeFromContext<CellmataParser.EqExprContext> {
    override fun getContext() = ctx
}

class GreaterThanExpr(
    val ctx: CellmataParser.MoreExprContext,
    val left: Expr,
    val right: Expr
) : Expr(), NodeFromContext<CellmataParser.MoreExprContext> {
    override fun getContext() = ctx
}

class GreaterOrEqExpr(
    val ctx: CellmataParser.MoreEqExprContext,
    val left: Expr,
    val right: Expr
) : Expr(), NodeFromContext<CellmataParser.MoreEqExprContext> {
    override fun getContext() = ctx
}

class LessThanExpr(
    val ctx: CellmataParser.LessExprContext,
    val left: Expr,
    val right: Expr
) : Expr(), NodeFromContext<CellmataParser.LessExprContext> {
    override fun getContext() = ctx
}

class LessOrEqExpr(
    val ctx: CellmataParser.LessEqExprContext,
    val left: Expr,
    val right: Expr
) : Expr(), NodeFromContext<CellmataParser.LessEqExprContext> {
    override fun getContext() = ctx
}

class AdditionExpr(
    val ctx: CellmataParser.AdditionExprContext,
    val left: Expr,
    val right: Expr
) : Expr(), NodeFromContext<CellmataParser.AdditionExprContext> {
    override fun getContext() = ctx
}

class SubtractionExpr(
    val ctx: CellmataParser.SubstractionExprContext,
    val left: Expr,
    val right: Expr
) : Expr(), NodeFromContext<CellmataParser.SubstractionExprContext> {
    override fun getContext() = ctx
}

class MultiplicationExpr(
    val ctx: CellmataParser.MultiplictionExprContext,
    val left: Expr,
    val right: Expr
) : Expr(), NodeFromContext<CellmataParser.MultiplictionExprContext> {
    override fun getContext() = ctx
}

class DivisionExpr(
    val ctx: CellmataParser.DivisionExprContext,
    val left: Expr,
    val right: Expr
) : Expr(), NodeFromContext<CellmataParser.DivisionExprContext> {
    override fun getContext() = ctx
}

class NegationExpr(
    val ctx: CellmataParser.NegationExprContext,
    val value: Expr
) : Expr(), NodeFromContext<CellmataParser.NegationExprContext> {
    override fun getContext() = ctx
}

class NotExpr(
    val ctx: CellmataParser.NotExprContext,
    val value: Expr
) : Expr(), NodeFromContext<CellmataParser.NotExprContext> {
    override fun getContext() = ctx
}

class ArrayLookupExpr(
    val ctx: CellmataParser.ArrayLookupExprContext,
    val arr: Expr,
    val index: Expr
) : Expr(), NodeFromContext<CellmataParser.ArrayLookupExprContext> {
    override fun getContext() = ctx
}

/**
 * @param ctx Node in the parse tree this node corresponds to.
 * @param values Elements of the array.
 * @param declaredType Type listed before the body/values of the array.
 */
class ArrayBodyExpr(
    val ctx: CellmataParser.ArrayValueExprContext,
    val values: List<Expr>,
    val declaredType: Type
) : Expr(), NodeFromContext<CellmataParser.ArrayValueExprContext> {
    override fun getContext() = ctx
}

class Identifier(
    val ctx: ParserRuleContext,
    var spelling: String
) : Expr(), NodeFromContext<ParserRuleContext> {
    override fun getContext() = ctx
}

class ModuloExpr(
    val ctx: CellmataParser.ModuloExprContext,
    val left: Expr,
    val right: Expr
) : Expr(), NodeFromContext<CellmataParser.ModuloExprContext> {
    override fun getContext() = ctx
}

/**
 * Presents a function call in abstract syntax tree
 */
class FuncExpr(
    val ctx: CellmataParser.FuncExprContext,
    val args: List<Expr>,
    var ident: String
) : Expr(), NodeFromContext<CellmataParser.FuncExprContext> {
    override fun getContext() = ctx
}

/**
 * The special variable '#'
 */
class StateIndexExpr(
    val ctx: CellmataParser.StateIndexExprContext
) : Expr(IntegerType), NodeFromContext<CellmataParser.StateIndexExprContext> {
    override fun getContext() = ctx
}

// Literals
/**
 * Represents a concrete integer value in the abstract syntax tree
 */
class IntLiteral(
    val ctx: CellmataParser.Integer_literalContext,
    var value: Int
) : Expr(IntegerType), NodeFromContext<CellmataParser.Integer_literalContext> {
    override fun getContext() = ctx
}

/**
 * Represents a concrete boolean value in the abstract syntax tree
 */
class BoolLiteral(
    val ctx: CellmataParser.Bool_literalContext,
    var value: Boolean
) : Expr(BooleanType), NodeFromContext<CellmataParser.Bool_literalContext> {
    override fun getContext() = ctx
}

/**
 * Represent a concrete float value in the abstract syntax tree
 */
class FloatLiteral(
    val ctx: CellmataParser.Float_literalContext,
    var value: Float
): Expr(FloatType), NodeFromContext<CellmataParser.Float_literalContext> {
    override fun getContext() = ctx
}

// Type conversion
/**
 * Internal node used to present an implicit conversion from integer to float.
 */
class IntToFloatConversion(val expr: Expr) : Expr(FloatType)

/**
 * Internal node used to represent an implicit conversion from a array of states to a neighbourhood.
 */
class StateArrayToActualNeighbourhoodConversion(val expr: Expr) : Expr(ActualNeighbourhoodType)

/*
 * Declarations
 */
/**
 * Represents global declarations like states, constants and functions
 */
sealed class Decl : AST() // state, neighbourhood, const, func

/**
 * Represents constants declarations
 */
class ConstDecl(
    val ctx: CellmataParser.Const_declContext,
    var ident: String,
    val expr: Expr,
    var type: Type? = UncheckedType
) : Decl(), NodeFromContext<CellmataParser.Const_declContext> {
    override fun getContext() = ctx
}

/**
 * Represent a state declaration
 */
class StateDecl(
    val ctx: CellmataParser.State_declContext,
    var ident: String,
    var red: Short,
    var blue: Short,
    var green: Short,
    val body: CodeBlock
) : Decl(), NodeFromContext<CellmataParser.State_declContext> {
    override fun getContext() = ctx
}

/**
 * Represents a single point in a neighbourhood declaration
 */
class Coordinate(
    val ctx: CellmataParser.Coords_declContext,
    val axes: List<Int>
) : AST(), NodeFromContext<CellmataParser.Coords_declContext> {
    override fun getContext() = ctx
}

/**
 * Represents a neighbourhood declaration
 */
class NeighbourhoodDecl(
    val ctx: CellmataParser.Neighbourhood_declContext,
    var ident: String,
    var coords: List<Coordinate>
) : Decl(), NodeFromContext<CellmataParser.Neighbourhood_declContext> {
    override fun getContext() = ctx
}

/**
 * Represent a single argument in a function declaration
 *
 * @see FuncDecl
 */
class FunctionArgs(
    val ctx: CellmataParser.Func_decl_argContext,
    val ident: String,
    private var type: Type?
) : AST(), TypedNode, NodeFromContext<CellmataParser.Func_decl_argContext> {
    override fun getContext() = ctx
    override fun getType() = type
    override fun setType(type: Type?) {
        this.type = type
    }
}

/**
 * Represents a function declaration
 */
class FuncDecl(
    val ctx: CellmataParser.Func_declContext,
    var ident: String,
    var args: List<FunctionArgs>,
    val body: CodeBlock,
    var returnType: Type = UncheckedType
) : Decl(), NodeFromContext<CellmataParser.Func_declContext> {
    override fun getContext() = ctx
}

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
class AssignStmt(
    val ctx: CellmataParser.AssignmentContext,
    var ident: String,
    val expr: Expr,
    var isDeclaration: Boolean? = null,
    private var type: Type? = UncheckedType
) : Stmt(), TypedNode, NodeFromContext<CellmataParser.AssignmentContext> {
    override fun getContext() = ctx
    override fun getType() = type
    override fun setType(type: Type?) {
        this.type = type
    }
}

/**
 * Represent a block in a if statement that is to be run if expr evaluates to true.
 * A ConditionalBlock can represent either the if or the elif part of an if statement.
 */
class ConditionalBlock(
    val ctx: ParserRuleContext, // Context can be both if-context and elif-context
    val expr: Expr,
    val block: CodeBlock
) : AST(), NodeFromContext<ParserRuleContext> {
    override fun getContext() = ctx
}

/**
 * Represent an entire if statement, including the if, the elif and the else blocks.
 */
class IfStmt(
    val ctx: CellmataParser.If_stmtContext,
    val conditionals: List<ConditionalBlock>,
    val elseBlock: CodeBlock?
) : Stmt(), NodeFromContext<CellmataParser.If_stmtContext> {
    override fun getContext() = ctx
}

class ForLoopStmt(
    val ctx: CellmataParser.For_stmtContext,
    val initPart: AssignStmt,
    val condition: Expr,
    val postIterationPart: AssignStmt,
    val body: CodeBlock
) : Stmt(), NodeFromContext<CellmataParser.For_stmtContext> {
    override fun getContext() = ctx
}

class BreakStmt(
    val ctx: CellmataParser.Break_stmtContext
) : Stmt(), NodeFromContext<CellmataParser.Break_stmtContext> {
    override fun getContext() = ctx
}

class ContinueStmt(
    val ctx: CellmataParser.Continue_stmtContext
) : Stmt(), NodeFromContext<CellmataParser.Continue_stmtContext> {
    override fun getContext() = ctx
}

/**
 * Represents a become statement.
 * The become statements terminates a state block, and changes the state of the cell being evaluated.
 */
class BecomeStmt(
    val ctx: CellmataParser.Become_stmtContext,
    val state: Expr
) : Stmt(), NodeFromContext<CellmataParser.Become_stmtContext> {
    override fun getContext() = ctx
}

/**
 * Represents a return statement.
 * The return statement terminates a function call and returns a value to the caller.
 */
class ReturnStmt(
    val ctx: CellmataParser.Return_stmtContext,
    val value: Expr
) : Stmt(), NodeFromContext<CellmataParser.Return_stmtContext> {
    override fun getContext() = ctx
}

/**
 * Represents a sequence of statements.
 */
class CodeBlock(
    val ctx: CellmataParser.Code_blockContext,
    val body: List<Stmt>
): AST(), NodeFromContext<CellmataParser.Code_blockContext> {
    override fun getContext() = ctx
}

/*
 * Error nodes are used when something goes wrong in reduce.kt and is returned by the failing function.
 */
class ErrorAST : AST()
class ErrorDecl : Decl()
class ErrorStmt : Stmt()
class ErrorExpr : Expr()
