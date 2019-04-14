package dk.aau.cs.d409f19.cellumata.ast

import dk.aau.cs.d409f19.antlr.CellmataParser
import dk.aau.cs.d409f19.cellumata.ErrorFromContext
import dk.aau.cs.d409f19.cellumata.ErrorLogger
import dk.aau.cs.d409f19.cellumata.TerminatedCompilationException
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode

/*
 * Parse Tree to Abstract Syntax Tree transformer/mapper
 *
 * This file implements a recursive traversal of the the parse tree,
 * where each step of the traversal emits a node in the AST if that node
 * in the parse tree is relevant to preserve.
 * It's important to note that this mapper only handles the process of
 * creating the tree part of the AST, it doesn't extract the values from
 * the parse tree nodes, that is handled by ParseTreeValueWalker.
 * This file handles the structure of the AST, and ParseTreeValueWalker
 * handles the value of each node in the AST.
 */

/**
 * Attempts to recursively transform an expression node in the parse tree to create a subtree of the AST.
 *
 * @throws AssertionError thrown when encountering an unexpected node in the parse tree.
 */
private fun reduceExpr(node: ParseTree): Expr {
    return when (node) {
        is CellmataParser.OrExprContext -> OrExpr(
            ctx = node,
            left = reduceExpr(node.left),
            right = reduceExpr(node.right)
        )
        is CellmataParser.AndExprContext -> AndExpr(
            ctx = node,
            left = reduceExpr(node.left),
            right = reduceExpr(node.right)
        )
        is CellmataParser.NotEqExprContext -> InequalityExpr(
            ctx = node,
            left = reduceExpr(node.left),
            right = reduceExpr(node.right)
        )
        is CellmataParser.EqExprContext -> EqualityExpr(
            ctx = node,
            left = reduceExpr(node.left),
            right = reduceExpr(node.right)
        )
        is CellmataParser.MoreEqExprContext -> GreaterOrEqExpr(
            ctx = node,
            left = reduceExpr(node.left),
            right = reduceExpr(node.right)
        )
        is CellmataParser.MoreExprContext -> GreaterThanExpr(
            ctx = node,
            left = reduceExpr(node.left),
            right = reduceExpr(node.right)
        )
        is CellmataParser.LessEqExprContext -> LessOrEqExpr(
            ctx = node,
            left = reduceExpr(node.left),
            right = reduceExpr(node.right)
        )
        is CellmataParser.LessExprContext -> LessThanExpr(
            ctx = node,
            left = reduceExpr(node.left),
            right = reduceExpr(node.right)
        )
        is CellmataParser.AdditionExprContext -> AdditionExpr(
            ctx = node,
            left = reduceExpr(node.left),
            right = reduceExpr(node.right)
        )
        is CellmataParser.SubstractionExprContext -> SubtractionExpr(
            ctx = node,
            left = reduceExpr(node.left),
            right = reduceExpr(node.right)
        )
        is CellmataParser.MultiplictionExprContext -> MultiplicationExpr(
            ctx = node,
            left = reduceExpr(node.left),
            right = reduceExpr(node.right)
        )
        is CellmataParser.DivisionExprContext -> DivisionExpr(
            ctx = node,
            left = reduceExpr(node.left),
            right = reduceExpr(node.right)
        )
        is CellmataParser.ModuloExprContext -> ModuloExpr(
            ctx = node,
            left = reduceExpr(node.left),
            right = reduceExpr(node.right)
        )
        is CellmataParser.NegationExprContext -> NegationExpr(
            ctx = node,
            value = reduceExpr(node.value)
        )
        is CellmataParser.NotExprContext -> NotExpr(
            ctx = node,
            value = reduceExpr(node.value)
        )
        is CellmataParser.ArrayLookupExprContext -> ArrayLookupExpr(
            ctx = node,
            arr = reduceExpr(node.value),
            index = reduceExpr(node.index)
        )
        is CellmataParser.ParenExprContext -> reduceExpr(node.expr())
        is CellmataParser.VarExprContext -> Identifier(
            ctx = node.ident,
            spelling = node.ident.text
        )
        is CellmataParser.FuncExprContext -> FuncExpr(
            ctx = node,
            args = node.value.expr().map(::reduceExpr),
            ident = node.value.ident.text
        )
        is CellmataParser.StateIndexExprContext -> StateIndexExpr(ctx = node)
        is CellmataParser.ArrayValueExprContext -> ArrayBodyExpr(
            ctx = node,
            values = node.array_value().array_body().expr().map(::reduceExpr),
            declaredType = typeFromCtx(node.value.type_ident())
        )
        is CellmataParser.LiteralExprContext -> reduceExpr(node.value)
        is CellmataParser.BoolLiteralContext -> BoolLiteral(
            ctx = node.value,
            value = when (node.value) {
                is CellmataParser.TrueLiteralContext -> true
                is CellmataParser.FalseLiteralContext -> false
                else -> throw TerminatedCompilationException("Could not parse boolean from '${node.value}'")
            })
        is CellmataParser.IntegerLiteralContext -> IntLiteral(node.value, node.value.text.toInt())
        is CellmataParser.FloatLiteralContext -> FloatLiteral(node.value, node.value.text.toFloat())
        is CellmataParser.Var_identContext -> Identifier(node, node.text)
        // Errors
        is ParserRuleContext -> { registerReduceError(node); ErrorExpr() }
        else -> throw TerminatedCompilationException("Statement ${node.javaClass} had no parsing context.")
    }
}

/**
 * Attempts to recursively transform an statement node in the parse tree to create a subtree of the AST.
 *
 * @throws AssertionError thrown when encountering an unexpected node in the parse tree.
 */
private fun reduceStmt(node: ParseTree): Stmt {
    return when (node) {
        is CellmataParser.Assign_stmtContext -> reduceStmt(node.assignment())
        is CellmataParser.AssignmentContext -> AssignStmt(
            ctx = node,
            expr = reduceExpr(node.expr()),
            ident = node.var_ident().text // TODO LHS Array, e.g.: "a[0] = 5;"
        )
        is CellmataParser.If_stmtContext -> IfStmt(
            ctx = node,
            conditionals = listOf( // Create list of list of ConditionalBlocks, then flatten to list of ConditionalBlocks
                listOf(
                    ConditionalBlock( // If clause
                        ctx = node,
                        expr = reduceExpr(node.if_stmt_if().if_stmt_block().if_stmt_condition().expr()),
                        block = reduceCodeBlock(node.if_stmt_if().if_stmt_block().code_block())
                    )
                ),
                node.if_stmt_elif().map { // Elif clauses
                    ConditionalBlock(
                        ctx = it,
                        expr = reduceExpr(it.if_stmt_block().if_stmt_condition().expr()),
                        block = reduceCodeBlock(it.if_stmt_block().code_block())
                    )
                }
            ).flatten(),
            elseBlock = if (node.if_stmt_else() == null) null else reduceCodeBlock(node.if_stmt_else().code_block())
        )
        is CellmataParser.For_stmtContext -> ForLoopStmt(
            ctx = node,
            initPart = reduceStmt(node.for_init()) as AssignStmt,
            condition = reduceExpr(node.for_condition().expr()),
            postIterationPart = reduceStmt(node.for_post_iteration()) as AssignStmt,
            body = reduceCodeBlock(node.code_block())
        )
        is CellmataParser.Break_stmtContext -> BreakStmt(ctx = node)
        is CellmataParser.Continue_stmtContext -> ContinueStmt(ctx = node)
        is CellmataParser.Become_stmtContext -> BecomeStmt(ctx = node, state = reduceExpr(node.state))
        is CellmataParser.StmtContext -> reduceStmt(node.getChild(0))
        is CellmataParser.Return_stmtContext -> ReturnStmt(ctx = node, value = reduceExpr(node.expr()))
        // Errors
        is ParserRuleContext -> { registerReduceError(node); ErrorStmt() }
        else -> throw TerminatedCompilationException("Statement ${node.javaClass} had no parsing context.")
    }
}

/**
 * Attempts to recursively transform an declaration node in the parse tree to create a subtree of the AST.
 *
 * @throws AssertionError thrown when encountering an unexpected node in the parse tree.
 */
private fun reduceDecl(node: ParseTree): Decl {
    return when (node) {
        is CellmataParser.Const_declContext -> ConstDecl(
            ctx = node,
            ident = node.const_ident().text,
            expr = reduceExpr(node.expr())
        )
        is CellmataParser.State_declContext -> StateDecl(
            ctx = node,
            ident = node.state_ident().text,
            red = parseColor(node.state_rgb().red),
            green = parseColor(node.state_rgb().green),
            blue = parseColor(node.state_rgb().blue),
            body = reduceCodeBlock(node.code_block())
        )
        is CellmataParser.Neighbourhood_declContext -> NeighbourhoodDecl(
            ctx = node,
            ident = node.neighbourhood_ident().text,
            coords = node.neighbourhood_code().coords_decl().map {
                Coordinate(
                    ctx = it,
                    axes = it.integer_literal().map { intCtx -> intCtx.text.toInt() } // TODO: Insanity checker, make sure coordinates have the same amount of axes
                )
            }
        )
        is CellmataParser.Func_declContext -> FuncDecl(
            ctx = node,
            ident = node.func_ident().text,
            args = node.func_decl_arg().map { FunctionArgs(it, it.IDENT().text, typeFromCtx(it.type_ident())) }.toList(),
            body = reduceCodeBlock(node.code_block())
        )
        // Errors
        is ParserRuleContext -> { registerReduceError(node); ErrorDecl() }
        else -> throw TerminatedCompilationException("Statement ${node.javaClass} had no parsing context.")
    }
}

/**
 * Parses an Integer_literalContext to a Short between 0 and 256 from a string. Used for state-declaration's colors
 */
fun parseColor(intCtx: CellmataParser.Integer_literalContext): Short {
    val value = intCtx.text.toShortOrNull()
    if (value == null || value < 0 || 255 < value) {
        ErrorLogger.registerError(
            ErrorFromContext(intCtx, "'${intCtx.text}' is not a valid colour. It must be an integer between 0 and 255.")
        )
    }
    return value ?: 0
}

/**
 * Transform all statements in a code block into abstract syntax tree nodes.
 *
 * @throws AssertionError thrown when encountering an unexpected node in the parse tree.
 */
fun reduceCodeBlock(block: CellmataParser.Code_blockContext): CodeBlock {
    return CodeBlock(block, block.children
        .filter { it !is TerminalNode } // Remove terminals
        .map { reduceStmt(it) })
}

/**
 * Attempts to recursively transform the parse tree root node to an abstract syntax tree.
 */
fun reduce(node: ParserRuleContext): AST {
    return when (node) {
        is CellmataParser.StartContext -> RootNode(
            world = WorldNode(
                ctx = node.world_dcl(),
                dimensions = if (node.world_dcl().size.height != null) {
                    listOf(
                        parseDimension(node.world_dcl().size.width),
                        parseDimension(node.world_dcl().size.height)
                    )
                } else {
                    listOf(parseDimension(node.world_dcl().size.width))
                },
                cellSize = node.world_dcl().cellsize.value.text.toIntOrNull(),
                tickrate = node.world_dcl().tickrate.value.text.toIntOrNull()
            ),
            body = node.body().children?.map(::reduceDecl) ?: listOf()
        )
        else -> { registerReduceError(node); ErrorAST() }
    }
}

/**
 * Parses a single dimension from the world size declaration list
 */
fun parseDimension(dim: CellmataParser.World_size_dimContext): WorldDimension {
    val type = dim.type
    return WorldDimension(
        size = dim.size.text.toInt(),
        type = when (type) {
            is CellmataParser.DimFiniteEdgeContext -> WorldType.EDGE
            is CellmataParser.DimFiniteWrappingContext -> WorldType.WRAPPING
            else -> throw AssertionError()
        },
        edge = if (type is CellmataParser.DimFiniteEdgeContext) Identifier(type, type.state.text) else null
    )
}

/**
 * Register a reduce error: Unexpected parsing context. This should only happen if we forget to update the reduce.kt
 * after changing the grammar.
 */
fun registerReduceError(ctx: ParserRuleContext) {
    ErrorLogger.registerError(ErrorFromContext(ctx, "Unexpected parsing context (${ctx.javaClass}). Parse tree could not be mapped to AST."))
}
