package dk.aau.cs.d409f19.cellumata.ast

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

data class OrExpr(val ctx: ParseTree, val left: Expr, val right: Expr) : Expr()

data class AndExpr(val ctx: ParseTree, val left: Expr, val right: Expr) : Expr()

data class InequalityExpr(val ctx: ParseTree, val left: Expr, val right: Expr) : Expr()

data class EqualityExpr(val ctx: ParseTree, val left: Expr, val right: Expr) : Expr()

data class MoreThanExpr(val ctx: ParseTree, val left: Expr, val right: Expr) : Expr()

data class MoreEqExpr(val ctx: ParseTree, val left: Expr, val right: Expr) : Expr()

data class LessThanExpr(val ctx: ParseTree, val left: Expr, val right: Expr) : Expr()

data class LessEqExpr(val ctx: ParseTree, val left: Expr, val right: Expr) : Expr()

data class AdditionExpr(val ctx: ParseTree, val left: Expr, val right: Expr) : Expr()

data class SubtractionExpr(val ctx: ParseTree, val left: Expr, val right: Expr) : Expr()

data class MultiplicationExpr(val ctx: ParseTree, val left: Expr, val right: Expr) : Expr()

data class DivisionExpr(val ctx: ParseTree, val left: Expr, val right: Expr) : Expr()

data class PreIncExpr(val ctx: ParseTree, val value: Expr) : Expr()

data class PreDecExpr(val ctx: ParseTree, val value: Expr) : Expr()

data class PostIncExpr(val ctx: ParseTree, val value: Expr) : Expr()

data class PostDecExpr(val ctx: ParseTree, val value: Expr) : Expr()

data class PositiveExpr(val ctx: ParseTree, val value: Expr) : Expr()

data class NegativeExpr(val ctx: ParseTree, val value: Expr) : Expr()

data class InverseExpr(val ctx: ParseTree, val value: Expr) : Expr()

data class ArrayLookupExpr(val ctx: ParseTree, val ident: String = MAGIC_UNDEFINED_STRING, val index: Int = -1) : Expr()

data class ArrayBodyExpr(val ctx: ParseTree, val type: String? = MAGIC_UNDEFINED_STRING, val values: List<Expr>) : Expr()

data class ParenExpr(val ctx: ParseTree, val expr: Expr) : Expr()

data class VarExpr(val ctx: ParseTree, val ident: String) : Expr()

data class ModuloExpr(val ctx: ParseTree, val left: Expr, val right: Expr) : Expr()

data class FuncExpr(val ctx: ParseTree, val args: List<Expr>) : Expr()

data class StateIndexExpr(val ctx: ParseTree) : Expr()

// Literals
data class IntLiteral(val ctx: ParseTree, val literal: Int = -1) : Expr()

data class BoolLiteral(val ctx: ParseTree, val value: Boolean = false) : Expr()

/*
 * Declarations
 */
sealed class Decl : AST() // state, const, func

data class ConstDecl(val ctx: ParseTree, val ident: String = MAGIC_UNDEFINED_STRING, val expr: Expr) : Decl()

data class StateDecl(
    val ctx: ParseTree,
    val ident: String = MAGIC_UNDEFINED_STRING,
    val red: Short = -1,
    val blue: Short = -1,
    val green: Short = -1,
    val body: List<Stmt>
) : Decl()

data class Coordinate(val ctx: ParseTree, val axes: List<Int> = emptyList())

data class NeighbourhoodDecl(
    val ctx: ParseTree,
    val ident: String = MAGIC_UNDEFINED_STRING,
    val coords: List<Coordinate> = emptyList()
) : Decl()

data class FunctionArgs(val ident: String, val type: String)

data class FuncDecl(
    val ctx: ParseTree,
    val ident: String = MAGIC_UNDEFINED_STRING,
    val args: List<FunctionArgs> = emptyList(),
    val body: List<Stmt> = emptyList()
) : Decl()

/*
 * Statements
 */
sealed class Stmt : AST()

data class AssignStmt(val ctx: ParseTree, val ident: String = MAGIC_UNDEFINED_STRING, val expr: Expr) : Stmt()

data class ConditionalBlock(val ctx: ParseTree, val expr: Expr, val block: List<Stmt>)

data class IfStmt(val ctx: ParseTree, val conditionals: List<ConditionalBlock>, val elseBlock: List<Stmt>?) : Stmt()

data class BecomeStmt(val ctx: ParseTree, val state: String = MAGIC_UNDEFINED_STRING) : Stmt()

data class PreIncStmt(val ctx: ParseTree, val variable: String = MAGIC_UNDEFINED_STRING) : Stmt()

data class PostIncStmt(val ctx: ParseTree, val variable: String = MAGIC_UNDEFINED_STRING) : Stmt()

data class PreDecStmt(val ctx: ParseTree, val variable: String = MAGIC_UNDEFINED_STRING) : Stmt()

data class PostDecStmt(val ctx: ParseTree, val variable: String = MAGIC_UNDEFINED_STRING) : Stmt()

data class ReturnStmt(val ctx: ParseTree, val value: Expr) : Stmt()
