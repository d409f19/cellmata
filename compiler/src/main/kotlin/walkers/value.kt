package dk.aau.cs.d409f19.cellumata.walkers

import dk.aau.cs.d409f19.cellumata.ast.*

open class ParsingException(msg: String = ""): Exception(msg)

class IntegerParsingException(val value: String, val ctx: AST): ParsingException(msg = "\"$value\" is not a valid integer")

class BoolParsingException(val value: String, val ctx: BoolLiteral): ParsingException(msg = "\"$value\"\"$value\" is not a valid boolean")

class FloatParsingException(val value: String, val ctx: FloatLiteral): ParsingException(msg = "\"$value\" is not a valid float")

class ColorParsingException(val value: String, val ctx: StateDecl): ParsingException(msg = "\"$value\" is not a valid color")

class CoordinateParsingException: ParsingException("Inconsistent amount axes in neighbourhood")


class LiteralExtractorVisitor : BaseASTVisitor() {
    // World

    override fun visit(node: NamedExpr) {
        node.ident = node.ctx.text
    }

    override fun visit(node: FuncExpr) {
        super.visit(node)
        node.ident = node.ctx.text
    }

    override fun visit(node: IntLiteral) {
        try {
            node.value = node.ctx.text.toInt()
        } catch (e: NumberFormatException) {
            throw IntegerParsingException(node.ctx.value.text, node)
        }
    }

    override fun visit(node: BoolLiteral) {
        node.value = when(node.ctx.text) {
            "true" -> true
            "false" -> false
            else -> throw BoolParsingException(node.ctx.text, node)
        }
    }

    override fun visit(node: FloatLiteral) {
        try {
            node.value = node.ctx.value.text.toFloat()
        } catch (e: java.lang.NumberFormatException) {
            throw FloatParsingException(node.ctx.value.text, node)
        }
    }

    override fun visit(node: ConstDecl) {
        super.visit(node)
        node.ident = node.ctx.const_ident().text
    }

    override fun visit(node: NeighbourhoodDecl) {
        super.visit(node)

        node.ident = node.ctx.neighbourhood_ident().text

        val coords =  node.ctx.neighbourhood_code().coords_decl().map {
            val axes = it.integer_literal().map { intCtx ->
                try {
                    intCtx.text.toInt()
                } catch (e: NumberFormatException) {
                    throw IntegerParsingException(intCtx.text, node)
                }
            }
            Coordinate(axes = axes, ctx = it)
        }

        if (coords.map { it.axes.size }.distinct().count() > 1) {
            // In case not all coordinates has the same amount of axes throw an error
            throw CoordinateParsingException()
        }

        node.coords = coords
    }

    override fun visit(node: FuncDecl) {
        super.visit(node)

        node.args = node.ctx.func_decl_arg().map { FunctionArgs(it, it.IDENT().text, it.type_ident().text) }.toList()

        node.ident = node.ctx.func_ident().text
        node.returnType = node.ctx.type_ident().text
    }

    override fun visit(node: StateDecl) {
        super.visit(node)

        node.ident = node.ctx.state_ident().text

        try {
            val value = node.ctx.state_rgb().red.text.toShort()
            if (value < 0 || value > 255) {
                throw ColorParsingException(node.ctx.state_rgb().red.text, node)
            }
            node.red = value
        } catch (e: NumberFormatException) {
            throw ColorParsingException(node.ctx.state_rgb().red.text, node)
        }

        try {
            val value = node.ctx.state_rgb().green.text.toShort()
            if (value < 0 || value > 255) {
                throw ColorParsingException(node.ctx.state_rgb().green.text, node)
            }
            node.green = value
        } catch (e: NumberFormatException) {
            throw ColorParsingException(node.ctx.state_rgb().green.text, node)
        }

        try {
            val value = node.ctx.state_rgb().blue.text.toShort()
            if (value < 0 || value > 255) {
                throw ColorParsingException(node.ctx.state_rgb().blue.text, node)
            }
            node.blue = value
        } catch (e: NumberFormatException) {
            throw ColorParsingException(node.ctx.state_rgb().blue.text, node)
        }
    }

    override fun visit(node: AssignStmt) {
        super.visit(node)
        node.ident = node.ctx.var_ident().text
    }
}