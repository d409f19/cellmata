package dk.aau.cs.d409f19.cellumata.ast

import dk.aau.cs.d409f19.antlr.CellmataParser
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode

/*
 * Parse Tree to Abstract Syntax Tree transformer/mapper
 *
 * This file implements a recursive traversal of the the parse tree,
 * where each step of the traversal emit a node in the AST if that node
 * in the parse tree is relevant to preserve.
 * It's important to note that this mapper only handles the process of
 * creating the tree part of the AST, it doesn't extract the values from
 * the parse tree nodes, that is handled by ParseTreeValueWalker.
 * This file handles the structure of the AST, and ParseTreeValueWalker
 * handles the value of each node in the AST.
 */

/**
 * Attempts to recursively transform an ANTLR parse tree to an abstract syntax tree.
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
        is CellmataParser.MoreEqExprContext -> MoreEqExpr(
            ctx = node,
            left = reduceExpr(node.left),
            right = reduceExpr(node.right)
        )
        is CellmataParser.MoreExprContext -> MoreThanExpr(
            ctx = node,
            left = reduceExpr(node.left),
            right = reduceExpr(node.right)
        )
        is CellmataParser.LessEqExprContext -> LessEqExpr(
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
        is CellmataParser.PreIncExprContext -> PreIncExpr(
            ctx = node,
            value = reduceExpr(node.value)
        )
        is CellmataParser.PreDecExprContext -> PreDecExpr(
            ctx = node,
            value = reduceExpr(node.value)
        )
        is CellmataParser.PositiveExprContext -> PositiveExpr(
            ctx = node,
            value = reduceExpr(node.value)
        )
        is CellmataParser.NegativeExprContext -> NegativeExpr(
            ctx = node,
            value = reduceExpr(node.value)
        )
        is CellmataParser.InverseExprContext -> InverseExpr(
            ctx = node,
            value = reduceExpr(node.value)
        )
        is CellmataParser.PostIncExprContext -> PostIncExpr(
            ctx = node,
            value = reduceExpr(node.value)
        )
        is CellmataParser.PostDecExprContext -> PostDecExpr(
            ctx = node,
            value = reduceExpr(node.value)
        )
        is CellmataParser.ArrayLookupExprContext -> ArrayLookupExpr(
            ctx = node,
            index = reduceExpr(node.index)
        )
        is CellmataParser.ParenExprContext -> ParenExpr(
            ctx = node,
            expr = reduceExpr(node.expr())
        ) // ToDo: Should we flatten this?
        is CellmataParser.LiteralExprContext -> reduceExpr(node.value)
        is CellmataParser.VarExprContext -> NamedExpr(
            ctx = node,
            ident = node.ident.text
        )
        is CellmataParser.FuncExprContext -> FuncExpr(
            ctx = node,
            args = node.value.expr().map(::reduceExpr)
        )
        is CellmataParser.StateIndexExprContext -> StateIndexExpr(ctx = node)
        is CellmataParser.ArrayValueExprContext -> ArrayBodyExpr(
            ctx = node,
            values = node.array_value().array_body().expr().map(::reduceExpr),
            declaredType = typeFromCtx(node.value.type_ident())
        )
        // Some literals has to be expanded before we reach the actual literal
        is CellmataParser.NumberLiteralContext -> reduceExpr(node.value)
        is CellmataParser.BoolLiteralContext -> reduceExpr(node.value)
        is CellmataParser.Bool_literalContext -> BoolLiteral(ctx = node)
        is CellmataParser.Number_literalContext -> reduceExpr(node.getChild(0))
        is CellmataParser.IntegerLiteralContext -> reduceExpr(node.value)
        is CellmataParser.FloatLiteralContext -> reduceExpr(node.value)
        is CellmataParser.Integer_literalContext -> IntLiteral(ctx = node)
        is CellmataParser.Float_literalContext -> FloatLiteral(ctx = node)
        is CellmataParser.Modifiable_identContext -> reduceExpr(node.getChild(0))
        is CellmataParser.Var_identContext -> NamedExpr(ctx = node)
        else -> throw AssertionError("Unexpected tree node")
    }
}

/**
 * Attempts to recursively transform an ANTLR parse tree to an abstract syntax tree.
 *
 * @throws AssertionError thrown when encountering an unexpected node in the parse tree.
 */
private fun reduceStmt(node: ParseTree): Stmt {
    return when (node) {
        is CellmataParser.Assign_stmtContext -> reduceStmt(node.assignment())
        is CellmataParser.AssignmentContext -> AssignStmt(
            ctx = node,
            expr = reduceExpr(node.expr())
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
            elseBlock = when (node.if_stmt_else()) {
                null -> null
                is CellmataParser.If_stmt_elseContext -> reduceCodeBlock(node.if_stmt_else().code_block())
                else -> throw AssertionError("Unexpected tree node")
            }
        )
        is CellmataParser.For_stmtContext -> ForStmt(
            ctx = node,
            initPart = AssignStmt(node.for_init().assignment(), expr = reduceExpr(node.for_init().assignment().expr())),
            condition = reduceExpr(node.for_condition().expr()),
            postIterationPart = AssignStmt(node.for_post_iteration().assignment(), expr = reduceExpr(node.for_post_iteration().assignment().expr()))
        )
        is CellmataParser.Break_stmtContext -> BreakStmt(ctx = node)
        is CellmataParser.Continue_stmtContext -> ContinueStmt(ctx = node)
        is CellmataParser.Become_stmtContext -> BecomeStmt(ctx = node, state = reduceExpr(node.state))
        is CellmataParser.PreIncStmtContext -> PreIncStmt(ctx = node, variable = reduceExpr(node.modifiable_ident()))
        is CellmataParser.PostIncStmtContext -> PostIncStmt(ctx = node, variable = reduceExpr(node.modifiable_ident()))
        is CellmataParser.PreDecStmtContext -> PreDecStmt(ctx = node, variable = reduceExpr(node.modifiable_ident()))
        is CellmataParser.PostDecStmtContext -> PostDecStmt(ctx = node, variable = reduceExpr(node.modifiable_ident()))
        is CellmataParser.StmtContext -> reduceStmt(node.getChild(0))
        is CellmataParser.Return_stmtContext -> ReturnStmt(ctx = node, value = reduceExpr(node.expr()))
        else -> throw AssertionError("Unexpected tree node")
    }
}

/**
 * Attempts to recursively transform an ANTLR parse tree to an abstract syntax tree.
 *
 * @throws AssertionError thrown when encountering an unexpected node in the parse tree.
 */
private fun reduceDecl(node: ParseTree): Decl {
    return when (node) {
        is CellmataParser.Const_declContext -> ConstDecl(
            ctx = node,
            expr = reduceExpr(node.expr())
        )
        is CellmataParser.State_declContext -> StateDecl(
            ctx = node,
            body = reduceCodeBlock(node.children // Get the body/code block
                .stream()
                .filter { it is CellmataParser.Code_blockContext }
                .findFirst().orElseThrow() as CellmataParser.Code_blockContext)
        )
        is CellmataParser.Neighbourhood_declContext -> NeighbourhoodDecl(
            ctx = node,
            coords = node.neighbourhood_code().coords_decl().map { Coordinate(ctx = it) }
        )
        is CellmataParser.Func_declContext -> FuncDecl(
            ctx = node,
            body = reduceCodeBlock(node.code_block())
        )
        else -> throw AssertionError("Unexpected tree node")
    }
}

/**
 * Transform all statements in a code block into abstract syntax tree nodes.
 *
 * @throws AssertionError thrown when encountering an unexpected node in the parse tree.
 */
fun reduceCodeBlock(block: CellmataParser.Code_blockContext): List<Stmt> {
    return block.children
        .filter { it !is TerminalNode } // Remove terminals
        .map { reduceStmt(it) }
}

/**
 * Attempts to recursively transform an ANTLR parse tree to an abstract syntax tree.
 *
 * @throws AssertionError thrown when encountering an unexpected node in the parse tree.
 */
fun reduce(node: ParseTree): AST {
    return when (node) {
        is CellmataParser.StartContext -> RootNode(
            world = WorldNode(ctx = node.world_dcl()),
            body = node.body().children.map(::reduceDecl)
        )
        else -> throw AssertionError("Unexpected tree node. Have the grammar been changed without updating the AST mapper?")
    }
}