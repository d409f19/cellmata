package dk.aau.cs.d409f19.cellumata.ast

import dk.aau.cs.d409f19.antlr.CellmataParser
import org.antlr.v4.runtime.ParserRuleContext

/**
 * This is a debugging string.
 * If you see this string in running code it means that the variable hasn't been initialized.
 */
const val MAGIC_UNDEFINED_STRING = "<<THIS IS A MAGIC STRING, UNDEFINED>>"

sealed class AST(open val ctx: ParserRuleContext)

data class RootNode constructor(
    override val ctx: ParserRuleContext,
    val world: WorldNode,
    val body: List<Decl>
) : AST(ctx)

/*
 * World definition
 */
enum class WorldType {
    WRAPPING,
    EDGE,
    UNDEFINED
}

data class WorldDimension(val size: Int = -1, val type: WorldType = WorldType.UNDEFINED)

data class WorldNode(
    override val ctx: CellmataParser.World_dclContext,
    val dimensions: List<WorldDimension> = emptyList(),
    val tickrate: Int? = null,
    val cellSize: Int? = null
) : AST(ctx)

/*
 * Expressions
 */
abstract class Expr(override val ctx: ParserRuleContext, var type: Type = UncheckedType) : AST(ctx)

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

data class PreIncExpr(override val ctx: CellmataParser.PreIncExprContext, val value: Expr) : Expr(ctx)

data class PreDecExpr(override val ctx: CellmataParser.PreDecExprContext, val value: Expr) : Expr(ctx)

data class PostIncExpr(override val ctx: CellmataParser.PostIncExprContext, val value: Expr) : Expr(ctx)

data class PostDecExpr(override val ctx: CellmataParser.PostDecExprContext, val value: Expr) : Expr(ctx)

data class PositiveExpr(override val ctx: CellmataParser.PositiveExprContext, val value: Expr) : Expr(ctx)

data class NegativeExpr(override val ctx: CellmataParser.NegativeExprContext, val value: Expr) : Expr(ctx)

data class InverseExpr(override val ctx: CellmataParser.InverseExprContext, val value: Expr) : Expr(ctx)

data class ArrayLookupExpr(override val ctx: CellmataParser.ArrayLookupExprContext, val ident: String = MAGIC_UNDEFINED_STRING, val index: Expr) : Expr(ctx)

data class ArrayBodyExpr(override val ctx: CellmataParser.ArrayValueExprContext, val values: List<Expr>) : Expr(ctx)

data class ParenExpr(override val ctx: CellmataParser.ParenExprContext, val expr: Expr) : Expr(ctx)

data class NamedExpr(override val ctx: ParserRuleContext, var ident: String = MAGIC_UNDEFINED_STRING) : Expr(ctx)

data class ModuloExpr(override val ctx: CellmataParser.ModuloExprContext, val left: Expr, val right: Expr) : Expr(ctx)

data class FuncExpr(override val ctx: CellmataParser.FuncExprContext, val args: List<Expr>, var ident: String = MAGIC_UNDEFINED_STRING) : Expr(ctx)

data class StateIndexExpr(override val ctx: CellmataParser.StateIndexExprContext) : Expr(ctx)

// Literals
data class IntLiteral(override val ctx: CellmataParser.Integer_literalContext, var value: Int = -1) : Expr(ctx)

data class BoolLiteral(override val ctx: CellmataParser.Bool_literalContext, var value: Boolean = false) : Expr(ctx)

data class FloatLiteral(override val ctx: CellmataParser.Float_literalContext, var value: Float = 0.0F): Expr(ctx)

// Type conversion
data class IntToFloatConversion(override val ctx: ParserRuleContext, val expr: Expr) : Expr(ctx)

data class StateArrayToActualNeighbourhoodConversion(override val ctx: ParserRuleContext, val expr: Expr) : Expr(ctx)

/*
 * Declarations
 */
sealed class Decl(override val ctx: ParserRuleContext) : AST(ctx) // state, const, func

data class ConstDecl(override val ctx: CellmataParser.Const_declContext, var ident: String = MAGIC_UNDEFINED_STRING, val expr: Expr) : Decl(ctx)

data class StateDecl(
    override val ctx: CellmataParser.State_declContext,
    var ident: String = MAGIC_UNDEFINED_STRING,
    var red: Short = -1,
    var blue: Short = -1,
    var green: Short = -1,
    val body: List<Stmt>
) : Decl(ctx)

data class Coordinate(override val ctx: ParserRuleContext, val axes: List<Int> = emptyList()) : AST(ctx)

data class NeighbourhoodDecl(
    override val ctx: CellmataParser.Neighbourhood_declContext,
    var ident: String = MAGIC_UNDEFINED_STRING,
    var coords: List<Coordinate> = emptyList()
) : Decl(ctx)

data class FunctionArgs(override val ctx: ParserRuleContext, val ident: String, val type: String): AST(ctx)

data class FuncDecl(
    override val ctx: CellmataParser.Func_declContext,
    var ident: String = MAGIC_UNDEFINED_STRING,
    var args: List<FunctionArgs> = emptyList(),
    val body: List<Stmt> = emptyList(),
    var returnType: String = MAGIC_UNDEFINED_STRING
) : Decl(ctx)

/*
 * Statements
 */
sealed class Stmt(override val ctx: ParserRuleContext) : AST(ctx)

data class AssignStmt(override val ctx: CellmataParser.AssignmentContext, var ident: String = MAGIC_UNDEFINED_STRING, val expr: Expr) : Stmt(ctx)

data class ConditionalBlock(override val ctx: ParserRuleContext, val expr: Expr, val block: List<Stmt>) : AST(ctx)

data class IfStmt(override val ctx: CellmataParser.If_stmtContext, val conditionals: List<ConditionalBlock>, val elseBlock: List<Stmt>?) : Stmt(ctx)

data class ForStmt(override val ctx: CellmataParser.For_stmtContext, val initPart: AssignStmt, val condition: Expr, val postIterationPart: AssignStmt) : Stmt(ctx)

data class BreakStmt(override val ctx: CellmataParser.Break_stmtContext) : Stmt(ctx)

data class ContinueStmt(override val ctx: CellmataParser.Continue_stmtContext) : Stmt(ctx)

data class BecomeStmt(override val ctx: CellmataParser.Become_stmtContext, val state: Expr) : Stmt(ctx)

data class PreIncStmt(override val ctx: CellmataParser.PreIncStmtContext, val variable: Expr) : Stmt(ctx)

data class PostIncStmt(override val ctx: CellmataParser.PostIncStmtContext, val variable:  Expr) : Stmt(ctx)

data class PreDecStmt(override val ctx: CellmataParser.PreDecStmtContext, val variable: Expr) : Stmt(ctx)

data class PostDecStmt(override val ctx: CellmataParser.PostDecStmtContext, val variable: Expr) : Stmt(ctx)

data class ReturnStmt(override val ctx: CellmataParser.Return_stmtContext, val value: Expr) : Stmt(ctx)

/*
 * Error nodes are used when something goes wrong in mapper.kt and is returned by the failing function.
 */
class ErrorAST(override val ctx: ParserRuleContext) : AST(ctx)
class ErrorDecl(override val ctx: ParserRuleContext) : Decl(ctx)
class ErrorStmt(override val ctx: ParserRuleContext) : Stmt(ctx)
class ErrorExpr(override val ctx: ParserRuleContext) : Expr(ctx)