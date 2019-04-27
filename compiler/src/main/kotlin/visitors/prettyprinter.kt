package dk.aau.cs.d409f19.cellumata.visitors

import dk.aau.cs.d409f19.cellumata.ast.WorldNode
import dk.aau.cs.d409f19.cellumata.ast.WorldType.EDGE
import dk.aau.cs.d409f19.cellumata.ast.WorldType.WRAPPING

class PrettyPrinter : BaseASTVisitor() {

    // String builder for accumulating the pretty-printed program source
    var stringBuilder = StringBuilder()

    /**
     * Print accumulated program from string builder
     */
    fun print() {
        println(stringBuilder.toString())
    }

    override fun visit(node: WorldNode) {
        // Begin world block boilerplate
        stringBuilder.appendln("world {")

        // Begin size-declaration boilerplate
        stringBuilder.append("\tsize = ")

        // For each dimension, print size and type
        for (n in node.dimensions.indices) {
            val isLast: Boolean = node.dimensions.lastIndex == n

            // Set separator to ", " if not last, else empty
            val separator = if (!isLast) ", " else ""

            // Print size and depending on type, print both type and identifier within brackets appended by separator
            stringBuilder.append(
                "${node.dimensions[n].size} ${if (node.dimensions[n].type == WRAPPING) {
                    "[wrap]$separator"
                } else if (node.dimensions[n].type == EDGE) {
                    "[edge=${node.dimensions[n].edge}]$separator"
                } else {
                    "[${node.dimensions[n].type}]$separator"
                }
                }"
            )
        }

        // When done with printing dimension, add linebreak
        stringBuilder.appendln()

        // Print tickrate and cellsize if not null, this could possibly be handled nicer with Elvis operator
        if (node.tickrate != null) {
            stringBuilder.appendln("\ttickrate = ${node.tickrate}")
        }

        if (node.cellSize != null) {
            stringBuilder.appendln("\tcellsize = ${node.cellSize}")
        }

        // When done with all world declaration printing, print closing curly bracket
        stringBuilder.appendln("}")

    }
}