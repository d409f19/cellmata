package dk.aau.cs.d409f19.cellumata.ast

import cs.aau.dk.d409f19.antlr.CellmataParser
import org.antlr.v4.runtime.tree.ParseTree

/**
 * This is a debugging string.
 * If you see this string in running code it means that the variable hasn't been initialized.
 */
const val MAGIC_UNDEFINED_STRING = "<<THIS IS A MAGIC STRING, UNDEFINED>>"

sealed class AST

data class RootNode(val world: WorldNode, val body: List<Decl>) : AST()

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
    val ctx: ParseTree,
    val dimensions: List<WorldDimension> = emptyList(),
    val tickrate: Int? = null,
    val cellSize: Int? = null
) : AST()

/*
 * Expressions
 */
sealed class Expr : AST()

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

data class ArrayLookupExpr(val ctx: CellmataParser.ArrayLookupExprContext, val ident: String = MAGIC_UNDEFINED_STRING, val index: Expr) : Expr()

data class ArrayBodyExpr(val ctx: CellmataParser.ArrayValueExprContext, var type: String? = MAGIC_UNDEFINED_STRING, val values: List<Expr>) : Expr()

data class ParenExpr(val ctx: CellmataParser.ParenExprContext, val expr: Expr) : Expr()

data class VarExpr(val ctx: ParseTree, var ident: String = MAGIC_UNDEFINED_STRING) : Expr()

data class ModuloExpr(val ctx: CellmataParser.ModuloExprContext, val left: Expr, val right: Expr) : Expr()

data class FuncExpr(val ctx: CellmataParser.FuncExprContext, val args: List<Expr>, var ident: String = MAGIC_UNDEFINED_STRING) : Expr()

data class StateIndexExpr(val ctx: CellmataParser.StateIndexExprContext) : Expr()

// Literals
data class IntLiteral(val ctx: CellmataParser.Integer_literalContext, var value: Int = -1) : Expr()

data class BoolLiteral(val ctx: CellmataParser.Bool_literalContext, var value: Boolean = false) : Expr()

data class FloatLiteral(val ctx: CellmataParser.Float_literalContext, var value: Float = 0.0F): Expr()

/*
 * Declarations
 */
sealed class Decl : AST() // state, const, func

data class ConstDecl(val ctx: CellmataParser.Const_declContext, var ident: String = MAGIC_UNDEFINED_STRING, val expr: Expr) : Decl()

data class StateDecl(
    val ctx: CellmataParser.State_declContext,
    var ident: String = MAGIC_UNDEFINED_STRING,
    var red: Short = -1,
    var blue: Short = -1,
    var green: Short = -1,
    val body: List<Stmt>
) : Decl()

data class Coordinate(val ctx: ParseTree, val axes: List<Int> = emptyList())

data class NeighbourhoodDecl(
    val ctx: CellmataParser.Neighbourhood_declContext,
    var ident: String = MAGIC_UNDEFINED_STRING,
    var coords: List<Coordinate> = emptyList()
) : Decl()

data class FunctionArgs(val ident: String, val type: String)

data class FuncDecl(
    val ctx: CellmataParser.Func_declContext,
    var ident: String = MAGIC_UNDEFINED_STRING,
    val args: List<FunctionArgs> = emptyList(),
    val body: List<Stmt> = emptyList(),
    var returnType: String = MAGIC_UNDEFINED_STRING
) : Decl()

/*
 * Statements
 */
sealed class Stmt : AST()

data class AssignStmt(val ctx: CellmataParser.AssignmentContext, var ident: String = MAGIC_UNDEFINED_STRING, val expr: Expr) : Stmt()

data class ConditionalBlock(val ctx: ParseTree, val expr: Expr, val block: List<Stmt>)

data class IfStmt(val ctx: CellmataParser.If_stmtContext, val conditionals: List<ConditionalBlock>, val elseBlock: List<Stmt>?) : Stmt()

data class ForStmt(val ctx: CellmataParser.For_stmtContext, val initPart: AssignStmt, val condition: Expr, val postIterationPart: AssignStmt) : Stmt()

data class BecomeStmt(val ctx: CellmataParser.Become_stmtContext, val state: Expr) : Stmt()

data class PreIncStmt(val ctx: CellmataParser.PreIncStmtContext, val variable: Expr) : Stmt()

data class PostIncStmt(val ctx: CellmataParser.PostIncStmtContext, val variable:  Expr) : Stmt()

data class PreDecStmt(val ctx: CellmataParser.PreDecStmtContext, val variable: Expr) : Stmt()

data class PostDecStmt(val ctx: CellmataParser.PostDecStmtContext, val variable: Expr) : Stmt()

data class ReturnStmt(val ctx: CellmataParser.Return_stmtContext, val value: Expr) : Stmt()
