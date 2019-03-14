package dk.aau.cs.d409f19.cellumata.ast

import cs.aau.dk.d409f19.antlr.CellmataParser
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode
import kotlin.streams.toList

private fun visitExpr(node: ParseTree): Expr {
    return when (node) {
        is CellmataParser.XorExprContext -> XorExpr(
            ctx = node,
            left = visitExpr(node.expr_1()),
            right = visitExpr(node.expr_2())
        )
        is CellmataParser.OrExprContext -> OrExpr(
            ctx = node,
            left = visitExpr(node.expr_2()),
            right = visitExpr(node.expr_3())
        )
        is CellmataParser.AndExprContext -> AndExpr(
            ctx = node,
            left = visitExpr(node.expr_3()),
            right = visitExpr(node.expr_4())
        )
        is CellmataParser.NotEqExprContext -> InequalityExpr(
            ctx = node,
            left = visitExpr(node.expr_4()),
            right = visitExpr(node.expr_5())
        )
        is CellmataParser.EqExprContext -> EqualityExpr(
            ctx = node,
            left = visitExpr(node.expr_4()),
            right = visitExpr(node.expr_5())
        )
        is CellmataParser.MoreEqExprContext -> MoreEqExpr(
            ctx = node,
            left = visitExpr(node.expr_5()),
            right = visitExpr(node.expr_6())
        )
        is CellmataParser.MoreExprContext -> MoreThanExpr(
            ctx = node,
            left = visitExpr(node.expr_5()),
            right = visitExpr(node.expr_6())
        )
        is CellmataParser.LessEqExprContext -> LessEqExpr(
            ctx = node,
            left = visitExpr(node.expr_5()),
            right = visitExpr(node.expr_6())
        )
        is CellmataParser.LessExprContext -> LessThanExpr(
            ctx = node,
            left = visitExpr(node.expr_5()),
            right = visitExpr(node.expr_6())
        )
        is CellmataParser.AdditionExprContext -> AdditionExpr(
            ctx = node,
            left = visitExpr(node.expr_6()),
            right = visitExpr(node.expr_7())
        )
        is CellmataParser.SubstractionExprContext -> SubtractionExpr(
            ctx = node,
            left = visitExpr(node.expr_6()),
            right = visitExpr(node.expr_7())
        )
        is CellmataParser.MultiplictionExprContext -> MultiplicationExpr(
            ctx = node,
            left = visitExpr(node.expr_7()),
            right = visitExpr(node.expr_8())
        )
        is CellmataParser.DivisionExprContext -> DivisionExpr(
            ctx = node,
            left = visitExpr(node.expr_7()),
            right = visitExpr(node.expr_8())
        )
        is CellmataParser.PreIncExprContext -> PreIncExpr(
            ctx = node,
            value = visitExpr(node.expr_9())
        )
        is CellmataParser.PreDecExprContext -> PreDecExpr(
            ctx = node,
            value = visitExpr(node.expr_9())
        )
        is CellmataParser.PositiveExprContext -> PositiveExpr(
            ctx = node,
            value = visitExpr(node.expr_9())
        )
        is CellmataParser.NegativeExprContext -> NegativeExpr(
            ctx = node,
            value = visitExpr(node.expr_9())
        )
        is CellmataParser.InverseExprContext -> InverseExpr(
            ctx = node,
            value = visitExpr(node.expr_9())
        )
        is CellmataParser.PostIncExprContext -> PostIncExpr(
            ctx = node,
            value = visitExpr(node.expr_10())
        )
        is CellmataParser.PostDecExprContext -> PostDecExpr(
            ctx = node,
            value = visitExpr(node.expr_10())
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
            ident = node.var_ident().text
        )
        is CellmataParser.FuncExprContext -> when (node.func()) {
            // CountFunc
            // RandFunc
            // AbsFunc
            else -> throw NotImplementedError()
        }

        is CellmataParser.ExprContext -> visitExpr(node.expr_1())
        is CellmataParser.Expr2ContContext -> visitExpr(node.expr_2())
        is CellmataParser.Expr3ContContext -> visitExpr(node.expr_3())
        is CellmataParser.Expr4ContContext -> visitExpr(node.expr_4())
        is CellmataParser.Expr5ContContext -> visitExpr(node.expr_5())
        is CellmataParser.Expr6ContContext -> visitExpr(node.expr_6())
        is CellmataParser.Expr7ContContext -> visitExpr(node.expr_7())
        is CellmataParser.Expr8ContContext -> visitExpr(node.expr_8())
        is CellmataParser.Expr9ContContext -> visitExpr(node.expr_9())
        is CellmataParser.Expr10ContContext -> visitExpr(node.expr_10())
        is CellmataParser.Expr11ContContext -> visitExpr(node.expr_11())

        else -> throw AssertionError()
    }
}

private fun visitStmt(node: ParseTree): Stmt {
    return when(node) {
        is CellmataParser.Assign_stmtContext -> AssignStmt(ctx = node, expr = visitExpr(node.expr()))
        is CellmataParser.If_stmtContext -> throw NotImplementedError() //ToDo
        is CellmataParser.Become_stmtContext -> BecomeStmt(ctx = node)
        is CellmataParser.PreIncStmtContext -> PreIncStmt(ctx = node)
        is CellmataParser.PostIncStmtContext -> PostIncStmt(ctx = node)
        is CellmataParser.PreDecStmtContext -> PreDecStmt(ctx = node)
        is CellmataParser.PostDecStmtContext -> PostDecStmt(ctx = node)
        else -> throw AssertionError("Unexpected tree node")
    }
}

private fun visitDecl(node: ParseTree): Decl {
    return when (node) {
        is CellmataParser.Const_declContext -> ConstDecl(ctx = node, expr = visitExpr(node.expr()))
        is CellmataParser.State_declContext -> StateDecl(
            ctx = node,
            body = (node.children // Get the body/code block
                .stream()
                .filter { it is CellmataParser.Code_blockContext }
                .findFirst().orElseThrow() as CellmataParser.Code_blockContext).children
                .stream() // Iterate through content of body/code block
                .filter { it !is TerminalNode } // Remove terminals
                .filter { it !is CellmataParser.State_rgbContext } // Remove color declaration
                .filter { it !is CellmataParser.State_identContext } // Remove name node
                .map { visitStmt(it) }
                .toList()
        )
        is CellmataParser.Neighbourhood_declContext -> throw NotImplementedError()
        else -> throw AssertionError()
    }
}

fun visit(node: ParseTree): AST {
    return when (node) {
        is CellmataParser.StartContext -> RootNode(
            world = WorldNode(ctx = node.world_dcl()),
            body = node.body().children.stream().map {
                when (it) {
                    is CellmataParser.Const_declContext -> visitDecl(it)
                    is CellmataParser.State_declContext -> visitDecl(it)
                    is CellmataParser.Neighbourhood_declContext -> visitDecl(it)
                    else -> throw AssertionError("")
                }
            }.toList()
        )
        is CellmataParser.Const_declContext -> visitDecl(node)
        is CellmataParser.State_declContext -> visitDecl(node)
        is CellmataParser.Neighbourhood_declContext -> visitDecl(node)
        else -> throw AssertionError("Unexpected tree node. Have the grammar been changed without updating the AST mapper?")
    }
}