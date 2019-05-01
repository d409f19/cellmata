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

val DEFAULT_CELLSIZE = 8
val DEFAULT_TICKRATE = 10

/**
 * Get the size declared as part of the sized arrays type declaration.
 *
 * @return Returns a list of sizes from the type declaration, and null for any dimension which has to be inferred. Example "[10][][30]bool{}" would return 10, null, 30
 */
private fun getArrayDeclaredSize(array_decl: CellmataParser.Array_declContext): MutableList<Int?> {
    val l: MutableList<Int?> = if (array_decl.array_prefix().index != null) {
        mutableListOf(array_decl.array_prefix().index.text.toInt())
    } else {
        mutableListOf(null)
    }

    // Assumption: Array is the only structured type
    if (array_decl.type_ident() is CellmataParser.TypeArrayContext) {
        l.addAll(getArrayDeclaredSize((array_decl.type_ident() as CellmataParser.TypeArrayContext).array_decl()))
    }

    return l
}

/**
 * Attempts to recursively transform an expression node in the parse tree to create a subtree of the AST.
 *
 * @throws AssertionError thrown when encountering an unexpected node in the parse tree.
 */
private fun reduceExpr(node: ParseTree): Expr {
    return when (node) {
        is CellmataParser.OrExprContext -> OrExpr(
            ctx = SourceContext(node),
            left = reduceExpr(node.left),
            right = reduceExpr(node.right)
        )
        is CellmataParser.AndExprContext -> AndExpr(
            ctx = SourceContext(node),
            left = reduceExpr(node.left),
            right = reduceExpr(node.right)
        )
        is CellmataParser.NotEqExprContext -> InequalityExpr(
            ctx = SourceContext(node),
            left = reduceExpr(node.left),
            right = reduceExpr(node.right)
        )
        is CellmataParser.EqExprContext -> EqualityExpr(
            ctx = SourceContext(node),
            left = reduceExpr(node.left),
            right = reduceExpr(node.right)
        )
        is CellmataParser.MoreEqExprContext -> GreaterOrEqExpr(
            ctx = SourceContext(node),
            left = reduceExpr(node.left),
            right = reduceExpr(node.right)
        )
        is CellmataParser.MoreExprContext -> GreaterThanExpr(
            ctx = SourceContext(node),
            left = reduceExpr(node.left),
            right = reduceExpr(node.right)
        )
        is CellmataParser.LessEqExprContext -> LessOrEqExpr(
            ctx = SourceContext(node),
            left = reduceExpr(node.left),
            right = reduceExpr(node.right)
        )
        is CellmataParser.LessExprContext -> LessThanExpr(
            ctx = SourceContext(node),
            left = reduceExpr(node.left),
            right = reduceExpr(node.right)
        )
        is CellmataParser.AdditionExprContext -> AdditionExpr(
            ctx = SourceContext(node),
            left = reduceExpr(node.left),
            right = reduceExpr(node.right)
        )
        is CellmataParser.SubstractionExprContext -> SubtractionExpr(
            ctx = SourceContext(node),
            left = reduceExpr(node.left),
            right = reduceExpr(node.right)
        )
        is CellmataParser.MultiplictionExprContext -> MultiplicationExpr(
            ctx = SourceContext(node),
            left = reduceExpr(node.left),
            right = reduceExpr(node.right)
        )
        is CellmataParser.DivisionExprContext -> DivisionExpr(
            ctx = SourceContext(node),
            left = reduceExpr(node.left),
            right = reduceExpr(node.right)
        )
        is CellmataParser.ModuloExprContext -> ModuloExpr(
            ctx = SourceContext(node),
            left = reduceExpr(node.left),
            right = reduceExpr(node.right)
        )
        is CellmataParser.NegationExprContext -> NegationExpr(
            ctx = SourceContext(node),
            value = reduceExpr(node.value)
        )
        is CellmataParser.NotExprContext -> NotExpr(
            ctx = SourceContext(node),
            value = reduceExpr(node.value)
        )
        is CellmataParser.ArrayLookupExprContext -> ArrayLookupExpr(
            ctx = SourceContext(node),
            arr = reduceExpr(node.value),
            index = reduceExpr(node.index)
        )
        is CellmataParser.ParenExprContext -> reduceExpr(node.expr())
        is CellmataParser.VarExprContext -> Identifier(
            ctx = SourceContext(node),
            spelling = node.ident.text
        )
        is CellmataParser.FuncExprContext -> FuncCallExpr(
            ctx = SourceContext(node),
            args = node.value.expr().map(::reduceExpr),
            ident = node.value.ident.text
        )
        is CellmataParser.StateIndexExprContext -> StateIndexExpr(ctx = SourceContext(node))
        is CellmataParser.ArraySizedValueExprContext -> reduceExpr(node.value)
        is CellmataParser.Array_value_sizedContext -> SizedArrayExpr(
            ctx = SourceContext(node),
            declaredType = ArrayType(typeFromCtx(node.array_decl().type_ident())),
            body = if(node.array_value_literal() == null) { null } else { reduceExpr(node.array_value_literal()) as ArrayLiteralExpr },
            declaredSize = getArrayDeclaredSize(node.array_decl())
        )
        is CellmataParser.ArrayLiteralExprContext -> reduceExpr(node.value)
        is CellmataParser.Array_value_literalContext -> ArrayLiteralExpr(SourceContext(node), node.expr().map(::reduceExpr))
        is CellmataParser.LiteralExprContext -> reduceExpr(node.value)
        is CellmataParser.BoolLiteralContext -> BoolLiteral(
            ctx = SourceContext(node),
            value = when (node.value) {
                is CellmataParser.TrueLiteralContext -> true
                is CellmataParser.FalseLiteralContext -> false
                else -> throw TerminatedCompilationException("Could not parse boolean from '${node.value}'")
            })
        is CellmataParser.IntegerLiteralContext -> IntLiteral(SourceContext(node), node.value.text.toInt())
        is CellmataParser.FloatLiteralContext -> FloatLiteral(SourceContext(node), node.value.text.toFloat())
        is CellmataParser.Var_identContext -> Identifier(SourceContext(node), node.text)
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
            ctx = SourceContext(node),
            expr = reduceExpr(node.expr()),
            ident = node.var_ident().text, // TODO LHS Array, e.g.: "a[0] = 5;"
            isDeclaration = node.STMT_LET() != null
        )
        is CellmataParser.If_stmtContext -> IfStmt(
            ctx = SourceContext(node),
            conditionals = listOf( // Create list of list of ConditionalBlocks, then flatten to list of ConditionalBlocks
                listOf(
                    ConditionalBlock( // If clause
                        ctx = SourceContext(node),
                        expr = reduceExpr(node.if_stmt_if().if_stmt_block().if_stmt_condition().expr()),
                        block = reduceCodeBlock(node.if_stmt_if().if_stmt_block().code_block())
                    )
                ),
                node.if_stmt_elif().map { // Elif clauses
                    ConditionalBlock(
                        ctx = SourceContext(it),
                        expr = reduceExpr(it.if_stmt_block().if_stmt_condition().expr()),
                        block = reduceCodeBlock(it.if_stmt_block().code_block())
                    )
                }
            ).flatten(),
            elseBlock = if (node.if_stmt_else() == null) null else reduceCodeBlock(node.if_stmt_else().code_block())
        )
        is CellmataParser.For_stmtContext -> ForLoopStmt(
            ctx = SourceContext(node),
            initPart = reduceStmt(node.for_init().assignment()) as AssignStmt,
            condition = reduceExpr(node.for_condition().expr()),
            postIterationPart = reduceStmt(node.for_post_iteration().assignment()) as AssignStmt,
            body = reduceCodeBlock(node.code_block())
        )
        is CellmataParser.Break_stmtContext -> BreakStmt(ctx = SourceContext(node))
        is CellmataParser.Continue_stmtContext -> ContinueStmt(ctx = SourceContext(node))
        is CellmataParser.Become_stmtContext -> BecomeStmt(ctx = SourceContext(node), state = reduceExpr(node.state))
        is CellmataParser.StmtContext -> reduceStmt(node.getChild(0))
        is CellmataParser.Return_stmtContext -> ReturnStmt(ctx = SourceContext(node), value = reduceExpr(node.expr()))
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
            ctx = SourceContext(node),
            ident = node.const_ident().text,
            expr = reduceExpr(node.expr())
        )
        is CellmataParser.State_declContext -> StateDecl(
            ctx = SourceContext(node),
            ident = node.state_ident().text,
            red = parseColor(node.state_rgb().red),
            green = parseColor(node.state_rgb().green),
            blue = parseColor(node.state_rgb().blue),
            body = reduceCodeBlock(node.code_block())
        )
        is CellmataParser.Neighbourhood_declContext -> NeighbourhoodDecl(
            ctx = SourceContext(node),
            ident = node.neighbourhood_ident().text,
            coords = node.neighbourhood_code().coords_decl().map {
                Coordinate(
                    ctx = SourceContext(it),
                    axes = it.integer_literal().map { intCtx -> intCtx.text.toInt() } // Coordinates' dimensions are checked in sanity checker
                )
            }
        )
        is CellmataParser.Func_declContext -> FuncDecl(
            ctx = SourceContext(node),
            ident = node.func_ident().text,
            returnType = typeFromCtx(node.type_ident()),
            args = node.func_decl_arg().map { FunctionArgument(SourceContext(it), it.IDENT().text, typeFromCtx(it.type_ident())) }.toList(),
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
            ErrorFromContext(SourceContext(intCtx), "'${intCtx.text}' is not a valid colour. It must be an integer between 0 and 255.")
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
    return CodeBlock(SourceContext(block), block.children
        .filter { it !is TerminalNode } // Remove terminals
        .map { reduceStmt(it) })
}

/**
 * Attempts to recursively transform the parse tree root node to an abstract syntax tree.
 */
fun reduce(node: ParserRuleContext): AST {
    return when (node) {
        is CellmataParser.StartContext -> RootNode(
            ctx = SourceContext(node),
            world = WorldNode(
                ctx = SourceContext(node.world_dcl()),
                dimensions = if (node.world_dcl().size.height != null) {
                    listOf(
                        parseDimension(node.world_dcl().size.width),
                        parseDimension(node.world_dcl().size.height)
                    )
                } else {
                    listOf(parseDimension(node.world_dcl().size.width))
                },
                cellSize = if (node.world_dcl().cellsize != null) node.world_dcl().cellsize.value.text.toIntOrNull() else DEFAULT_CELLSIZE,
                tickrate = if (node.world_dcl().tickrate != null) node.world_dcl().tickrate.value?.text?.toIntOrNull() else DEFAULT_TICKRATE
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
        edge = if (type is CellmataParser.DimFiniteEdgeContext) Identifier(SourceContext(type), type.state.text) else null
    )
}

/**
 * Register a reduce error: Unexpected parsing context. This should only happen if we forget to update the reduce.kt
 * after changing the grammar.
 */
fun registerReduceError(ctx: ParserRuleContext) {
    ErrorLogger.registerError(ErrorFromContext(SourceContext(ctx), "Unexpected parsing context (${ctx.javaClass}). Parse tree could not be mapped to AST."))
}
