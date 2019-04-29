package dk.aau.cs.d409f19.cellumata.interpreter

import dk.aau.cs.d409f19.cellumata.ast.*
import dk.aau.cs.d409f19.cellumata.visitors.ASTVisitor
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.util.*
import javax.swing.JFrame
import javax.swing.JPanel

class Interpreter(val rootNode: RootNode, symbolTable: Table) : ASTVisitor<Any> {

    private val symbolTableSession = ViewingSymbolTableSession(symbolTable)

    fun start() {
        visit(rootNode)
    }

    override fun visit(node: RootNode): Any {

        val xDim = node.world.dimensions[0]
        val width = xDim.size
        val yDim = node.world.dimensions[0]
        val height = yDim.size

        val listOfStates = node.body.filterIsInstance<StateDecl>()
        val grid = Array(width) { Array(height) { listOfStates.random() } }

        // Rendering
        val cellSize = node.world.cellSize!!
        val tickrate = node.world.tickrate!!
        val frame = JFrame("Cellmata")
        val panel = frame.add(JPanel())
        panel.preferredSize = Dimension(width * cellSize, height * cellSize)
        frame.isResizable = true
        frame.setLocationRelativeTo(null)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.pack()
        frame.isVisible = true
        val g = panel.graphics as Graphics2D

        // Driver
        Timer().scheduleAtFixedRate(object: TimerTask() {
            override fun run() {

                // Iterate over all cells, find their new state, and draw the new state
                for ((x, row) in grid.withIndex()) {
                    for ((y, state) in row.withIndex()) {

                        // Find new state by executing the state's logic. If the logic does not return a StateDecl
                        // then use the old state
                        val newState = (visit(state.body).takeIf { it is StateDecl } ?: state) as StateDecl
                        grid[x][y] = newState

                        // Draw
                        g.color = Color(newState.red, newState.green, newState.blue)
                        g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize)
                    }
                }
            }
        }, 0, tickrate.toLong())

        return Unit
    }

    override fun visit(node: Decl): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: ConstDecl): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: StateDecl): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: NeighbourhoodDecl): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: Coordinate): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: FuncDecl): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: Expr): Any {
        return when (node) {
            is BinaryExpr -> visit(node)
            is NegationExpr -> visit(node)
            is NotExpr -> visit(node)
            is ArrayLookupExpr -> visit(node)
            is ArrayBodyExpr -> visit(node)
            is Identifier -> visit(node)
            is FuncCallExpr -> visit(node)
            is StateIndexExpr -> visit(node)
            is IntLiteral -> visit(node)
            is FloatLiteral -> visit(node)
            is BoolLiteral -> visit(node)
            is IntToFloatConversion -> visit(node)
            is StateArrayToLocalNeighbourhoodConversion -> visit(node)
            is ErrorExpr -> AssertionError("ErrorExpr (${node.ctx.lineNumber}, ${node.ctx.charPositionInLine}) made it to the interpreter.")
        }
    }

    override fun visit(node: BinaryExpr): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: BinaryArithmeticExpr): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: BinaryBooleanExpr): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: NumericComparisonExpr): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: OrExpr): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: AndExpr): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: InequalityExpr): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: EqualityExpr): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: GreaterThanExpr): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: GreaterOrEqExpr): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: LessThanExpr): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: LessOrEqExpr): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: AdditionExpr): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: SubtractionExpr): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: MultiplicationExpr): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: DivisionExpr): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: NegationExpr): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: NotExpr): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: ArrayLookupExpr): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: ArrayBodyExpr): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: Identifier): Any {
        return symbolTableSession.getSymbol(node.spelling) ?: AssertionError("\"${node.spelling}\" at ${node.ctx} is not declared but made it to interpreter.")
    }

    override fun visit(node: ModuloExpr): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: FuncCallExpr): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: StateIndexExpr): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: IntLiteral): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: BoolLiteral): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: Stmt): Any {
        return when(node) {
            is AssignStmt -> visit(node)
            is IfStmt -> visit(node)
            is BecomeStmt -> visit(node)
            is ReturnStmt -> visit(node)
            is ForLoopStmt -> visit(node)
            is BreakStmt -> visit(node)
            is ContinueStmt -> visit(node)
            is ErrorStmt -> AssertionError("ErrorStmt from ${node.ctx} made it to the interpreter.")
        }
    }

    override fun visit(node: AssignStmt): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: IfStmt): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: BecomeStmt): Any {
        return visit(node.state)
    }

    override fun visit(node: ReturnStmt): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: AST): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: FloatLiteral): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: ConditionalBlock): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: FunctionArgument): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: WorldNode): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: WorldDimension): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: ForLoopStmt): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: BreakStmt): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: ContinueStmt): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: CodeBlock): Any {
        // Execute each statement in order, until one returns something else than Unit. Return that value.
        for (statement in node.body) {
            val value = visit(statement)
            if (value != Unit) {
                return value
            }
        }

        // No return/become value found, so this block returns Unit
        return Unit
    }
}
