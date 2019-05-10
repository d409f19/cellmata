package dk.aau.cs.d409f19.cellumata.visitors

import dk.aau.cs.d409f19.cellumata.ast.*
import java.io.OutputStream
import java.io.PrintStream

class ASTGrapher(sink: OutputStream, private val output: PrintStream = PrintStream(sink)) : BaseASTVisitor() {
    private var depth = 0

    private fun printNode(from: Any, to: Any) {
        output.println("\t" + from.hashCode() + " -> " + to.hashCode())
    }

    private fun printLabel(node: Any, label: String) {
        output.println("\t" + node.hashCode() + " [label=\"" + label + "\"]")
    }

    override fun visit(node: WorldNode) {
        var builder = StringBuilder()
            .append("WorldNode\\nsize = ")
            .append(node.dimensions.map {
                when(it.type) {
                    WorldType.EDGE -> it.size.toString() + "[edge]"
                    WorldType.UNDEFINED -> it.size.toString() + "[undefined]"
                    WorldType.WRAPPING -> it.size.toString() + "[wrapping]"
                }
            }.joinToString(", "))

        if (node.edge != null) builder.append("\\nedge=").append(node.edge.spelling)
        builder.append("\\ncellsize=").append(node.cellSize)
        builder.append("\\ntickrate=").append(node.tickrate)

        printLabel(node, builder.toString())

        node.dimensions.forEach { printNode(node, it) }

        super.visit(node)
    }

    override fun visit(node: WorldDimension) {
        val builder: StringBuilder = StringBuilder("WorldDimension\\ntype=${node.type}\\nsize=${node.size}")
        printLabel(node, builder.toString())
        super.visit(node)
    }

    override fun visit(node: RootNode) {
        output.println("digraph program {")
        output.println(
            "\tgraph [pad=\"0.5\", nodesep=\"1\", ranksep=\"1\"];" +
            "\tgraph [splines=line];" +
            "\tedge [dir=none];" +
            "\tnode[shape = square];"
        )
        printLabel(node, "Start")
        printNode(node, node.world)
        node.body.forEach { printNode(node, it) }
        super.visit(node)
        output.println("}")
    }

    override fun visit(node: ConstDecl) {
        printLabel(node, "ConstDecl\\n" + node.ident)
        printNode(node, node.expr)
        super.visit(node)
    }

    override fun visit(node: StateDecl) {
        printLabel(node, "State\\n" + node.ident + "\\nred=" + node.red + "\\ngreen=" + node.green + "\\nblue=" + node.blue)
        printNode(node, node.body)
        depth++
        super.visit(node)
        depth--
    }

    override fun visit(node: NeighbourhoodDecl) {
        printLabel(node, "Neighbourhood\\n" + node.ident + "\\ncoords=" + node.coords.size)
        node.coords.forEach { printNode(node, it) }
        super.visit(node)
    }

    override fun visit(node: FuncDecl) {
        printLabel(node, "Function\\n" + node.ident + "\\nargs=" + node.args.size)
        printNode(node, node.body)
        depth++
        node.args.forEach { printNode(node, it) }
        super.visit(node)
        depth--
    }

    override fun visit(node: FunctionArgument) {
        printLabel(node, "FunctionArgs\\n" + node.ident)
        super.visit(node)
    }

    override fun visit(node: OrExpr) {
        printLabel(node, "OrExpr")
        printNode(node, node.left)
        printNode(node, node.right)
        super.visit(node)
    }

    override fun visit(node: AndExpr) {
        printLabel(node, "AndExpr")
        printNode(node, node.left)
        printNode(node, node.right)
        super.visit(node)
    }

    override fun visit(node: InequalityExpr) {
        printLabel(node, "InequalityExpr")
        printNode(node, node.left)
        printNode(node, node.right)
        super.visit(node)
    }

    override fun visit(node: EqualityExpr) {
        printLabel(node, "EqualityExpr")
        printNode(node, node.left)
        printNode(node, node.right)
        super.visit(node)
    }

    override fun visit(node: GreaterThanExpr) {
        printLabel(node, "GreaterThanExpr")
        printNode(node, node.left)
        printNode(node, node.right)
        super.visit(node)
    }

    override fun visit(node: GreaterOrEqExpr) {
        printLabel(node, "GreaterOrEqExpr")
        printNode(node, node.left)
        printNode(node, node.right)
        super.visit(node)
    }

    override fun visit(node: LessThanExpr) {
        printLabel(node, "LessThanExpr")
        printNode(node, node.left)
        printNode(node, node.right)
        super.visit(node)
    }

    override fun visit(node: LessOrEqExpr) {
        printLabel(node, "LessEqExpr")
        printNode(node, node.left)
        printNode(node, node.right)
        super.visit(node)
    }

    override fun visit(node: AdditionExpr) {
        printLabel(node, "AdditionExpr")
        printNode(node, node.left)
        printNode(node, node.right)
        super.visit(node)
    }

    override fun visit(node: SubtractionExpr) {
        printLabel(node, "SubtractionExpr")
        printNode(node, node.left)
        printNode(node, node.right)
        super.visit(node)
    }

    override fun visit(node: MultiplicationExpr) {
        printLabel(node, "MultiplicationExpr")
        printNode(node, node.left)
        printNode(node, node.right)
        super.visit(node)
    }

    override fun visit(node: DivisionExpr) {
        printLabel(node, "DivisionExpr")
        printNode(node, node.left)
        printNode(node, node.right)
        super.visit(node)
    }

    override fun visit(node: NegationExpr) {
        printLabel(node, "NegationExpr")
        printNode(node, node.value)
        super.visit(node)
    }

    override fun visit(node: NotExpr) {
        printLabel(node, "NotExpr")
        printNode(node, node.value)
        super.visit(node)
    }

    override fun visit(node: LookupExpr) {
        printLabel(node, "LookupExpr")
        printNode(node, node.index)
        super.visit(node)
    }

    override fun visit(node: SizedArrayExpr) {
        printLabel(node, "SizedArrayExpr\\ntype=${node.declaredType}\\nsize=${node.declaredSize.map { it.toString() }.joinToString(", ")}")
        if (node.body != null) {
            printNode(node, node.body)
            visit(node.body)
        }
        super.visit(node)
    }

    override fun visit(node: ArrayLiteralExpr) {
        printLabel(node, "ArrayLiteralExpr\\nsize=${node.size}")
        node.values.forEach { printNode(node, it); visit(it) }
        super.visit(node)
    }

    override fun visit(node: ModuloExpr) {
        printLabel(node, "ModuloExpr")
        printNode(node, node.left)
        printNode(node, node.right)
        super.visit(node)
    }

    override fun visit(node: FuncCallExpr) {
        printLabel(node, "FuncCallExpr\\n" + node.ident)
        node.args.forEach { printNode(node, it) }
        super.visit(node)
    }

    override fun visit(node: AssignStmt) {
        printLabel(node, "AssignStmt\\n" + node.ident)
        printNode(node, node.expr)
        super.visit(node)
    }

    override fun visit(node: IfStmt) {
        printLabel(node, "IfStmt")
        node.conditionals.forEach { printNode(node, it) }
        if (node.elseBlock != null) {
            // Insert a node to represent the else block, otherwise the else block is unreadable
            printLabel(node.elseBlock, "Else")
            printNode(node, node.elseBlock)
        }
        super.visit(node)
    }

    override fun visit(node: ConditionalBlock) {
        printLabel(node, "ConditionalBlock")
        printNode(node, node.expr)
        depth++
        super.visit(node)
        depth--
    }

    override fun visit(node: BecomeStmt) {
        printLabel(node, "BecomeStmt")
        printNode(node, node.state)
        super.visit(node)
    }

    override fun visit(node: ReturnStmt) {
        printLabel(node, "ReturnStmt")
        printNode(node, node.expr)
        super.visit(node)
    }

    override fun visit(node: Identifier) {
        printLabel(node, "Identifier\\n" + node.spelling)
        super.visit(node)
    }

    override fun visit(node: StateIndexExpr) {
        printLabel(node, "StateIndexExpr")
        super.visit(node)
    }

    override fun visit(node: IntLiteral) {
        printLabel(node, "IntLiteral\\n" + node.value)
        super.visit(node)
    }

    override fun visit(node: BoolLiteral) {
        printLabel(node, "BoolLiteral\\n" + node.value)
        super.visit(node)
    }

    override fun visit(node: FloatLiteral) {
        printLabel(node, "FloatLiteral\\n" + node.value)
        super.visit(node)
    }

    override fun visit(node: Coordinate) {
        printLabel(node, "Coordinate\\n(" + node.axes.joinToString(", ") + ")")
        super.visit(node)
    }

    override fun visit(node: ForLoopStmt) {
        node.initPart?.let { printNode(node, it) }
        printNode(node, node.condition)
        node.postIterationPart?.let { printNode(node, it) }
        printNode(node, node.body)
        printLabel(node.body, "body")
        super.visit(node)
    }

    override fun visit(node: BreakStmt) {
        printLabel(node, "BreakStmt")
        super.visit(node)
    }

    override fun visit(node: ContinueStmt) {
        printLabel(node, "ContinueStmt")
        super.visit(node)
    }

    override fun visit(node: CodeBlock) {
        printLabel(node, "CodeBlock")
        node.body.forEach { printNode(node, it) }
        super.visit(node)
    }
}