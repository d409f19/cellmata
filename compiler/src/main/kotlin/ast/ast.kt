package dk.aau.cs.d409f19.cellumata.ast

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token

/**
 * The SourceContext data class contains the position of the context in the source code from which an AST node was created.
 * @param lineNumber The line number in the source program. In the range 1..n
 * @param charPositionInLine The position of the first character of the AST node in source program. In the range 0..n-1
 */
data class SourceContext(val lineNumber: Int, val charPositionInLine: Int) {
    constructor(ctx: ParserRuleContext) : this(ctx.start.line, ctx.start.charPositionInLine)
    constructor(token: Token) : this(token.line, token.charPositionInLine)
}

/**
 * A special instance of SourceContext. It is used for nodes that are not from the source program, e.g. it could be
 * from the standard environment like a built-in function.
 */
val EMPTY_CONTEXT = SourceContext(0, 0)

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
sealed class AST(val ctx: SourceContext)

/**
 * RootNode represents a .cell file
 */
class RootNode(
    ctx: SourceContext,
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

data class WorldDimension(val size: Int, val type: WorldType, val edge: Identifier?)

/**
 * WorldNode represent the world definition.
 */
class WorldNode(
    ctx: SourceContext,
    var dimensions: List<WorldDimension>,
    var tickrate: Int?,
    var cellSize: Int?
) : AST(ctx)

/*
 * Expressions
 */
/**
 * An expression is either a literal, an identifier, or some operation that produces a value.
 */
sealed class Expr(
    ctx: SourceContext,
    private var type: Type? = UncheckedType
) : AST(ctx), TypedNode {
    override fun getType(): Type? = type
    override fun setType(type: Type?) { this.type = type }
}

/**
 * A BinaryExpr has exactly two child expressions. It is used to generalize behaviour of binary expressions in various visitors.
 */
sealed class BinaryExpr(ctx: SourceContext, var left: Expr, var right: Expr) : Expr(ctx)

/**
 * A BinaryArithmeticExpr has exactly two child expressions which are both numeric expressions, and the
 * BinaryArithmeticExpr itself also returns a number.
 * It is used to generalize behaviour of binary arithmetic expressions in various visitors.
 */
sealed class BinaryArithmeticExpr(ctx: SourceContext, left: Expr, right: Expr) : BinaryExpr(ctx, left, right)

/**
 * A NumericComparisonExpr has exactly two child expressions which are both numeric expressions, but the
 * NumericComparisonExpr itself also returns a boolean.
 * It is used to generalize behaviour of comparison expressions in various visitors.
 */
sealed class NumericComparisonExpr(ctx: SourceContext, left: Expr, right: Expr) : BinaryExpr(ctx, left, right)

/**
 * A BinaryBooleanExpr has exactly two child expressions which are both boolean expressions, and the
 * BinaryArithmeticExpr itself also returns a number.
 * It is used to generalize behaviour of binary boolean expressions in various visitors.
 */
sealed class BinaryBooleanExpr(ctx: SourceContext, left: Expr, right: Expr) : BinaryExpr(ctx, left, right)

class OrExpr(
    ctx: SourceContext,
    left: Expr,
    right: Expr
) : BinaryBooleanExpr(ctx, left, right)

class AndExpr(
    ctx: SourceContext,
    left: Expr,
    right: Expr
) : BinaryBooleanExpr(ctx, left, right)

class InequalityExpr(
    ctx: SourceContext,
    left: Expr,
    right: Expr
) : NumericComparisonExpr(ctx, left, right)

class EqualityExpr(
    ctx: SourceContext,
    left: Expr,
    right: Expr
) : NumericComparisonExpr(ctx, left, right)

class GreaterThanExpr(
    ctx: SourceContext,
    left: Expr,
    right: Expr
) : NumericComparisonExpr(ctx, left, right)

class GreaterOrEqExpr(
    ctx: SourceContext,
    left: Expr,
    right: Expr
) : NumericComparisonExpr(ctx, left, right)

class LessThanExpr(
    ctx: SourceContext,
    left: Expr,
    right: Expr
) : NumericComparisonExpr(ctx, left, right)

class LessOrEqExpr(
    ctx: SourceContext,
    left: Expr,
    right: Expr
) : NumericComparisonExpr(ctx, left, right)

class AdditionExpr(
    ctx: SourceContext,
    left: Expr,
    right: Expr
) : BinaryArithmeticExpr(ctx, left, right)

class SubtractionExpr(
    ctx: SourceContext,
    left: Expr,
    right: Expr
) : BinaryArithmeticExpr(ctx, left, right)

class MultiplicationExpr(
    ctx: SourceContext,
    left: Expr,
    right: Expr
) : BinaryArithmeticExpr(ctx, left, right)

class DivisionExpr(
    ctx: SourceContext,
    left: Expr,
    right: Expr
) : BinaryArithmeticExpr(ctx, left, right)

class NegationExpr(
    ctx: SourceContext,
    val value: Expr
) : Expr(ctx)

class NotExpr(
    ctx: SourceContext,
    val value: Expr
) : Expr(ctx)

class ArrayLookupExpr(
    ctx: SourceContext,
    val arr: Expr,
    val index: Expr
) : Expr(ctx)

/**
 * @param ctx the context from witch this node was created from
 * @param values Elements of the array.
 * @param declaredType Type listed before the body/values of the array.
 */
class ArrayBodyExpr(
    ctx: SourceContext,
    val values: List<Expr>,
    val declaredType: Type
) : Expr(ctx)

class Identifier(
    ctx: SourceContext,
    var spelling: String
) : Expr(ctx)

class ModuloExpr(
    ctx: SourceContext,
    left: Expr,
    right: Expr
) : BinaryArithmeticExpr(ctx, left, right)

/**
 * Presents a function call in abstract syntax tree
 */
class FuncCallExpr(
    ctx: SourceContext,
    val args: List<Expr>,
    var ident: String
) : Expr(ctx)

/**
 * The special variable '#'
 */
class StateIndexExpr(
    ctx: SourceContext
) : Expr(ctx, IntegerType)

// Literals
/**
 * Represents a concrete integer value in the abstract syntax tree
 */
class IntLiteral(
    ctx: SourceContext,
    var value: Int
) : Expr(ctx, IntegerType)

/**
 * Represents a concrete boolean value in the abstract syntax tree
 */
class BoolLiteral(
    ctx: SourceContext,
    var value: Boolean
) : Expr(ctx, BooleanType)

/**
 * Represent a concrete float value in the abstract syntax tree
 */
class FloatLiteral(
    ctx: SourceContext,
    var value: Float
): Expr(ctx, FloatType)

// Type conversion
/**
 * Internal node used to present an implicit conversion from integer to float.
 */
class IntToFloatConversion(val expr: Expr) : Expr(expr.ctx, FloatType)

/**
 * Internal node used to represent an implicit conversion from a array of states to a neighbourhood.
 */
class StateArrayToLocalNeighbourhoodConversion(val expr: Expr) : Expr(expr.ctx, LocalNeighbourhoodType)

/*
 * Declarations
 */
/**
 * Represents global declarations like states, constants and functions
 */
sealed class Decl(ctx: SourceContext) : AST(ctx) // state, neighbourhood, const, func

/**
 * Represents constants declarations
 */
class ConstDecl(
    ctx: SourceContext,
    var ident: String,
    val expr: Expr,
    var type: Type? = UncheckedType
) : Decl(ctx)

/**
 * Represent a state declaration
 */
class StateDecl(
    ctx: SourceContext,
    var ident: String,
    var red: Int,
    var blue: Int,
    var green: Int,
    val body: CodeBlock
) : Decl(ctx)

/**
 * Represents a single point in a neighbourhood declaration
 */
class Coordinate(
    ctx: SourceContext,
    val axes: List<Int>
) : AST(ctx)

/**
 * Represents a neighbourhood declaration
 */
class NeighbourhoodDecl(
    ctx: SourceContext,
    var ident: String,
    var coords: List<Coordinate>
) : Decl(ctx)

/**
 * Represent a single argument in a function declaration
 *
 * @see FuncDecl
 */
class FunctionArgument(
    ctx: SourceContext,
    val ident: String,
    private var type: Type?
) : AST(ctx), TypedNode {
    override fun getType() = type
    override fun setType(type: Type?) {
        this.type = type
    }
}

/**
 * Represents a function declaration
 */
class FuncDecl(
    ctx: SourceContext,
    var ident: String,
    var args: List<FunctionArgument>,
    val body: CodeBlock,
    var returnType: Type = UncheckedType
) : Decl(ctx)

/*
 * Statements
 */
/**
 * Base class for classes represents a single statement/action in a block of code
 */
sealed class Stmt(ctx: SourceContext) : AST(ctx)

/**
 * Represent a variable declaration and/or assignment
 *
 * @param ctx The node in the parse tree that this node corresponds to
 * @param ident Name of the variable
 * @param expr New value of the variable
 * @param type Type of the variable
 */
class AssignStmt(
    ctx: SourceContext,
    var ident: String,
    val expr: Expr,
    var isDeclaration: Boolean,
    private var type: Type? = UncheckedType
) : Stmt(ctx), TypedNode {
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
    ctx: SourceContext,
    val expr: Expr,
    val block: CodeBlock
) : AST(ctx)

/**
 * Represent an entire if statement, including the if, the elif and the else blocks.
 */
class IfStmt(
    ctx: SourceContext,
    val conditionals: List<ConditionalBlock>,
    val elseBlock: CodeBlock?
) : Stmt(ctx)

class ForLoopStmt(
    ctx: SourceContext,
    val initPart: AssignStmt,
    val condition: Expr,
    val postIterationPart: AssignStmt,
    val body: CodeBlock
) : Stmt(ctx)

class BreakStmt(
    ctx: SourceContext
) : Stmt(ctx)

class ContinueStmt(
    ctx: SourceContext
) : Stmt(ctx)

/**
 * Represents a become statement.
 * The become statements terminates a state block, and changes the state of the cell being evaluated.
 */
class BecomeStmt(
    ctx: SourceContext,
    val state: Expr
) : Stmt(ctx)

/**
 * Represents a return statement.
 * The return statement terminates a function call and returns a value to the caller.
 */
class ReturnStmt(
    ctx: SourceContext,
    val value: Expr
) : Stmt(ctx)

/**
 * Represents a sequence of statements.
 */
class CodeBlock(
    ctx: SourceContext,
    val body: List<Stmt>
): AST(ctx)

/*
 * Error nodes are used when something goes wrong in reduce.kt and is returned by the failing function.
 */
class ErrorAST : AST(EMPTY_CONTEXT)
class ErrorDecl : Decl(EMPTY_CONTEXT)
class ErrorStmt : Stmt(EMPTY_CONTEXT)
class ErrorExpr : Expr(EMPTY_CONTEXT)
