package dk.aau.cs.d409f19.cellumata.visitors

import dk.aau.cs.d409f19.antlr.CellmataParser
import dk.aau.cs.d409f19.cellumata.ast.*

open class ParsingException(msg: String = ""): Exception(msg)

/**
 * Thrown if there where an error while parsing a integer
 */
class IntegerParsingException(val value: String, val ctx: AST): ParsingException(msg = "\"$value\" is not a valid integer")

/**
 * Thrown if there where an error while parsing a bool
 */
class BoolParsingException(val value: String, val ctx: BoolLiteral): ParsingException(msg = "\"$value\"\"$value\" is not a valid boolean")

/**
 * Thrown if there where an error while parsing a float
 */
class FloatParsingException(val value: String, val ctx: FloatLiteral): ParsingException(msg = "\"$value\" is not a valid float")

/**
 * Thrown if there where an error while parsing a color/byte
 */
class ColorParsingException(val value: String, val ctx: StateDecl): ParsingException(msg = "\"$value\" is not a valid color")

/**
 * Thrown if there where an error while parsing a single coordinate of a neighbourhood
 */
class CoordinateParsingException: ParsingException("Inconsistent amount axes in neighbourhood")

/**
 * Visits the abstract syntax tree parsing
 */
class LiteralExtractorVisitor : BaseASTVisitor() {
    // World

    override fun visit(node: NamedExpr) {
        node.ident = node.ctx.text
    }

    override fun visit(node: FuncExpr) {
        super.visit(node)
        node.ident = node.ctx.value.ident.text
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

        // For each declared coordinate produce a Coordinate through the map operation
        val coords = node.ctx.neighbourhood_code().coords_decl().map {
            // For each axes in the the coordinate convert it from a string to an integer
            val axes = it.integer_literal().map { intCtx ->
                try {
                    intCtx.text.toInt()
                } catch (e: NumberFormatException) {
                    throw IntegerParsingException(intCtx.text, node)
                }
            }
            Coordinate(axes = axes, ctx = it)
        }

        // In case not all coordinates has the same amount of axes throw an error
        if (coords.map { it.axes.size }.distinct().count() > 1) {
            throw CoordinateParsingException()
        }

        node.coords = coords
    }

    override fun visit(node: FuncDecl) {
        super.visit(node)

        // Interpret each argument and produce a FunctionArgs for it
        node.args = node.ctx.func_decl_arg().map { FunctionArgs(it, it.IDENT().text, typeFromCtx(it.type_ident())) }.toList()

        node.ident = node.ctx.func_ident().text
        node.returnType = typeFromCtx(node.ctx.type_ident())
    }

    override fun visit(node: StateDecl) {
        super.visit(node)

        node.ident = node.ctx.state_ident().text

        // Red
        try {
            val value = node.ctx.state_rgb().red.text.toShort()
            if (value < 0 || value > 255) {
                throw ColorParsingException(node.ctx.state_rgb().red.text, node)
            }
            node.red = value
        } catch (e: NumberFormatException) {
            throw ColorParsingException(node.ctx.state_rgb().red.text, node)
        }

        // Green
        try {
            val value = node.ctx.state_rgb().green.text.toShort()
            if (value < 0 || value > 255) {
                throw ColorParsingException(node.ctx.state_rgb().green.text, node)
            }
            node.green = value
        } catch (e: NumberFormatException) {
            throw ColorParsingException(node.ctx.state_rgb().green.text, node)
        }

        // Blue
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
        node.isDeclaration = (node.ctx.STMT_LET() != null)
    }

    /**
     * Parse a string into a integer, or throw IntegerParsingException the string isn't a valid integer.
     *
     * @throws IntegerParsingException
     */
    private fun parseInt(text: String, node: AST): Int {
        try {
            return text.toInt()
        } catch (e: NumberFormatException) {
            throw IntegerParsingException(text, node)
        }
    }

    override fun visit(node: WorldNode) {
        /**
         * Parses a single dimension from the world size declaration list
         */
        fun parseDimension(dim: CellmataParser.World_size_dimContext): WorldDimension {
            val type = dim.type
            return WorldDimension(
                size = parseInt(dim.size.text, node),
                type = when(type) {
                    is CellmataParser.DimFiniteEdgeContext -> WorldType.EDGE
                    is CellmataParser.DimFiniteWrappingContext -> WorldType.WRAPPING
                    else -> throw AssertionError()
                },
                edge = if(type is CellmataParser.DimFiniteEdgeContext) {
                    type.state.text
                } else {
                    null
                }
            )
        }

        val width = parseDimension(node.ctx.size.width)

        // If height is non-null it is two dimensional, otherwise it is one dimensional
        node.dimensions = if (node.ctx.size.height != null) {
            val height = parseDimension(node.ctx.size.height)
            listOf(width, height)
        } else {
            listOf(width)
        }

        node.cellSize = parseInt(node.ctx.cellsize.value.text, node)
        node.tickrate = parseInt(node.ctx.tickrate.value.text, node)

        super.visit(node)
    }
}
