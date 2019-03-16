package dk.aau.cs.d409f19.cellumata.ast

import cs.aau.dk.d409f19.antlr.CellmataParser
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode
import kotlin.streams.toList

private fun visitExpr(node: ParseTree): Expr {
    return when (node) {
        is CellmataParser.OrExprContext -> OrExpr(
            ctx = node,
            left = visitExpr(node.left),
            right = visitExpr(node.right)
        )
        is CellmataParser.AndExprContext -> AndExpr(
            ctx = node,
            left = visitExpr(node.left),
            right = visitExpr(node.right)
        )
        is CellmataParser.NotEqExprContext -> InequalityExpr(
            ctx = node,
            left = visitExpr(node.left),
            right = visitExpr(node.right)
        )
        is CellmataParser.EqExprContext -> EqualityExpr(
            ctx = node,
            left = visitExpr(node.left),
            right = visitExpr(node.right)
        )
        is CellmataParser.MoreEqExprContext -> MoreEqExpr(
            ctx = node,
            left = visitExpr(node.left),
            right = visitExpr(node.right)
        )
        is CellmataParser.MoreExprContext -> MoreThanExpr(
            ctx = node,
            left = visitExpr(node.left),
            right = visitExpr(node.right)
        )
        is CellmataParser.LessEqExprContext -> LessEqExpr(
            ctx = node,
            left = visitExpr(node.left),
            right = visitExpr(node.right)
        )
        is CellmataParser.LessExprContext -> LessThanExpr(
            ctx = node,
            left = visitExpr(node.left),
            right = visitExpr(node.right)
        )
        is CellmataParser.AdditionExprContext -> AdditionExpr(
            ctx = node,
            left = visitExpr(node.left),
            right = visitExpr(node.right)
        )
        is CellmataParser.SubstractionExprContext -> SubtractionExpr(
            ctx = node,
            left = visitExpr(node.left),
            right = visitExpr(node.right)
        )
        is CellmataParser.MultiplictionExprContext -> MultiplicationExpr(
            ctx = node,
            left = visitExpr(node.left),
            right = visitExpr(node.right)
        )
        is CellmataParser.DivisionExprContext -> DivisionExpr(
            ctx = node,
            left = visitExpr(node.left),
            right = visitExpr(node.right)
        )
        is CellmataParser.ModuloExprContext -> ModuloExpr(
            ctx = node,
            left = visitExpr(node.left),
            right = visitExpr(node.right)
        )
        is CellmataParser.PreIncExprContext -> PreIncExpr(
            ctx = node,
            value = visitExpr(node.value)
        )
        is CellmataParser.PreDecExprContext -> PreDecExpr(
            ctx = node,
            value = visitExpr(node.value)
        )
        is CellmataParser.PositiveExprContext -> PositiveExpr(
            ctx = node,
            value = visitExpr(node.value)
        )
        is CellmataParser.NegativeExprContext -> NegativeExpr(
            ctx = node,
            value = visitExpr(node.value)
        )
        is CellmataParser.InverseExprContext -> InverseExpr(
            ctx = node,
            value = visitExpr(node.value)
        )
        is CellmataParser.PostIncExprContext -> PostIncExpr(
            ctx = node,
            value = visitExpr(node.value)
        )
        is CellmataParser.PostDecExprContext -> PostDecExpr(
            ctx = node,
            value = visitExpr(node.value)
        )
        is CellmataParser.ArrayLookupExprContext -> ArrayLookupExpr(
            ctx = node
        )
        is CellmataParser.ParenExprContext -> ParenExpr(
            ctx = node,
            expr = visitExpr(node.expr())
        ) // ToDo: Should we flatten this?
        is CellmataParser.LiteralExprContext -> when (node.literal()) {
            is CellmataParser.NumberLiteralContext -> IntLiteral(ctx = node)
            is CellmataParser.BoolLiteralContext -> BoolLiteral(ctx = node)
            else -> throw AssertionError()
        }
        is CellmataParser.VarExprContext -> VarExpr(
            ctx = node,
            ident = node.ident.text
        )
        is CellmataParser.FuncExprContext -> FuncExpr(
            ctx = node.value,
            args = node.value.expr().map { visitExpr(it) }
        )
        is CellmataParser.StateIndexExprContext -> StateIndexExpr(ctx = node)
        is CellmataParser.ArrayValueExprContext -> ArrayBodyExpr(
            ctx = node.array_value(),
            values = node.array_value().array_body().expr().map(::visitExpr)
        )
        else -> throw AssertionError("Unexpected tree node")
    }
}

private fun visitStmt(node: ParseTree): Stmt {
    return when (node) {
        is CellmataParser.Assign_stmtContext -> AssignStmt(ctx = node, expr = visitExpr(node.expr()))
        is CellmataParser.If_stmtContext -> IfStmt(
            ctx = node,
            conditionals = listOf( // Create list of list of ConditionalBlocks, then flatten to list of ConditionalBlocks
                listOf(
                    ConditionalBlock( // If clause
                        ctx = node,
                        expr = visitExpr(node.if_stmt_if().if_stmt_block().if_stmt_condition().expr()),
                        block = visitCodeBlock(node.if_stmt_if().if_stmt_block().code_block())
                    )
                ),
                node.if_stmt_elif().map { // Elif clauses
                    ConditionalBlock(
                        ctx = node,
                        expr = visitExpr(it.if_stmt_block().if_stmt_condition().expr()),
                        block = visitCodeBlock(it.if_stmt_block().code_block())
                    )
                }
            ).flatten(),
            elseBlock = when (node.if_stmt_else()) {
                // null???
                is CellmataParser.If_stmt_elseContext -> visitCodeBlock(node.if_stmt_else().code_block())
                else -> throw AssertionError("Unexpected tree node")
            }
        )
        is CellmataParser.Become_stmtContext -> BecomeStmt(ctx = node)
        is CellmataParser.PreIncStmtContext -> PreIncStmt(ctx = node)
        is CellmataParser.PostIncStmtContext -> PostIncStmt(ctx = node)
        is CellmataParser.PreDecStmtContext -> PreDecStmt(ctx = node)
        is CellmataParser.PostDecStmtContext -> PostDecStmt(ctx = node)
        is CellmataParser.StmtContext -> visitStmt(node.getChild(0))
        is CellmataParser.Return_stmtContext -> ReturnStmt(ctx = node, value = visitExpr(node.expr()))
        else -> throw AssertionError("Unexpected tree node")
    }
}

private fun visitDecl(node: ParseTree): Decl {
    return when (node) {
        is CellmataParser.Const_declContext -> ConstDecl(ctx = node, expr = visitExpr(node.expr()))
        is CellmataParser.State_declContext -> StateDecl(
            ctx = node,
            body = visitCodeBlock(node.children // Get the body/code block
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
            body = visitCodeBlock(node.code_block())
        )
        else -> throw AssertionError("Unexpected tree node")
    }
}

fun visitCodeBlock(block: CellmataParser.Code_blockContext): List<Stmt> {
    return block.children
        .filter { it !is TerminalNode } // Remove terminals
        .filter { !(it is CellmataParser.StmtContext && it.childCount == 0) }
        .filter { !(it is CellmataParser.StmtContext && it.getChild(0) is TerminalNode ) }
        .map { visitStmt(it) }
}

fun visit(node: ParseTree): AST {
    return when (node) {
        is CellmataParser.StartContext -> RootNode(
            world = WorldNode(ctx = node.world_dcl()),
            body = node.body().children.map(::visitDecl)
        )
        else -> throw AssertionError("Unexpected tree node. Have the grammar been changed without updating the AST mapper?")
    }
}