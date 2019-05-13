package dk.aau.cs.d409f19.cellumata.interpreter

import dk.aau.cs.d409f19.cellumata.ast.*
import dk.aau.cs.d409f19.cellumata.visitors.ASTVisitor
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.lang.RuntimeException
import java.util.*
import javax.swing.JFrame
import javax.swing.JPanel

open class InterpretRuntimeException(msg: String) : RuntimeException(msg)
class InterpreterVisitException(node: Any) : InterpretRuntimeException("${node.javaClass} should not be visited.")

class StateValue(val decl: StateDecl, val index: Int)
object BreakValue
object ContinueValue

class Interpreter(val rootNode: RootNode) : ASTVisitor<Any> {

    private val stack = MemoryStack()
    private var defaultStateValue: StateValue? = null

    fun start() {
        visit(rootNode)
    }

    private fun prefillMemory(root: RootNode) {
        stack.pushStack()
        // TODO Built-in functions
        // Add components except neighbourhoods. Constants are added last as they need computation
        root.body
            .filter { it !is ConstDecl && it !is NeighbourhoodDecl}
            .forEach {
                when (it) {
                    is StateDecl -> stack.declareGlobal(it.ident, StateValue(it, 0))
                    is FuncDecl -> stack.declareGlobal(it.ident, it)
                    else -> AssertionError()
                }
            }
        root.body.filterIsInstance<ConstDecl>().forEach { stack.declareGlobal(it.ident, visit(it.expr)) }
    }

    override fun visit(node: RootNode): Any {

        prefillMemory(node)

        val xDim = node.world.dimensions[0]
        val width = xDim.size
        val yDim = node.world.dimensions[0]
        val height = yDim.size

        val listOfStates = node.body.filterIsInstance<StateDecl>()
        defaultStateValue = StateValue(listOfStates[0], 0)
        val grid = Array(width) { Array(height) { StateValue(listOfStates.random(), 0) } }

        // Rendering
        val cellSize = node.world.cellSize
        val tickrate = node.world.tickrate
        val frame = JFrame("Cellmata")
        val panel = frame.add(JPanel())
        panel.preferredSize = Dimension(width * cellSize, height * cellSize)
        frame.isResizable = false
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
                        stack.openScope()
                        stack.declare("#", state.index)
                        val newState = (visit(state.decl.body).takeIf { it is StateValue } ?: state) as StateValue
                        stack.closeScope()
                        grid[x][y] = newState

                        // Draw
                        g.color = Color(newState.decl.red, newState.decl.green, newState.decl.blue)
                        g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize)
                    }
                }
            }
        }, 0, (1000f / tickrate).toLong())

        return Unit
    }

    override fun visit(node: AST): Any {
        return when (node) {
            is Expr -> visit(node)
            is Stmt -> visit(node)
            is ConditionalBlock -> visit(node)
            is CodeBlock -> visit(node)
            is ErrorAST -> AssertionError("ErrorAST (${node.ctx.lineNumber}, ${node.ctx.charPositionInLine}) made it to the interpreter.")
            else -> throw InterpreterVisitException(node)
        }
    }

    override fun visit(node: Decl): Any {
        throw InterpreterVisitException(node)
    }

    override fun visit(node: ConstDecl): Any {
        throw InterpreterVisitException(node)
    }

    override fun visit(node: StateDecl): Any {
        throw InterpreterVisitException(node)
    }

    override fun visit(node: NeighbourhoodDecl): Any {
        throw InterpreterVisitException(node)
    }

    override fun visit(node: Coordinate): Any {
        throw InterpreterVisitException(node)
    }

    override fun visit(node: FuncDecl): Any {
        throw InterpreterVisitException(node)
    }

    override fun visit(node: Expr): Any {
        return when (node) {
            is BinaryExpr -> visit(node)
            is NegationExpr -> visit(node)
            is NotExpr -> visit(node)
            is LookupExpr -> visit(node)
            is Identifier -> visit(node)
            is FuncCallExpr -> visit(node)
            is StateIndexExpr -> visit(node)
            is SizedArrayExpr -> visit(node)
            is ArrayLiteralExpr -> visit(node)
            is IntLiteral -> visit(node)
            is FloatLiteral -> visit(node)
            is BoolLiteral -> visit(node)
            is IntToFloatConversion -> visit(node)
            is StateArrayToLocalNeighbourhoodConversion -> visit(node)
            is ErrorExpr -> AssertionError("ErrorExpr (${node.ctx.lineNumber}, ${node.ctx.charPositionInLine}) made it to the interpreter.")
        }
    }

    override fun visit(node: BinaryExpr): Any {
        return when (node) {
            is EqualityComparisonExpr -> visit(node)
            is BinaryArithmeticExpr -> visit(node)
            is BinaryBooleanExpr -> visit(node)
            is NumericComparisonExpr -> visit(node)
        }
    }

    override fun visit(node: EqualityComparisonExpr): Any {
        return when (node) {
            is InequalityExpr -> visit(node)
            is EqualityExpr -> visit(node)
        }
    }

    override fun visit(node: BinaryArithmeticExpr): Any {
        return when (node) {
            is AdditionExpr -> visit(node)
            is SubtractionExpr -> visit(node)
            is MultiplicationExpr -> visit(node)
            is DivisionExpr -> visit(node)
            is ModuloExpr -> visit(node)
        }
    }

    override fun visit(node: BinaryBooleanExpr): Any {
        return when (node) {
            is OrExpr -> visit(node)
            is AndExpr -> visit(node)
        }
    }

    override fun visit(node: NumericComparisonExpr): Any {
        return when (node) {
            is GreaterThanExpr -> visit(node)
            is GreaterOrEqExpr -> visit(node)
            is LessThanExpr -> visit(node)
            is LessOrEqExpr -> visit(node)
        }
    }

    override fun visit(node: OrExpr): Any {
        return (visit(node.left) as Boolean) || (visit(node.right) as Boolean)
    }

    override fun visit(node: AndExpr): Any {
        return (visit(node.left) as Boolean) && (visit(node.right) as Boolean)
    }

    override fun visit(node: InequalityExpr): Any {
        return visit(node.left) != visit(node.right)
    }

    override fun visit(node: EqualityExpr): Any {
        return visit(node.left) == visit(node.right)
    }

    override fun visit(node: GreaterThanExpr): Any {
        return with (node) {
            when {
                left.getType() == FloatType && right.getType() == FloatType -> {
                    (visit(left) as Float) > (visit(right) as Float)
                }
                left.getType() == FloatType && right.getType() == IntegerType -> {
                    (visit(left) as Float) > (visit(right) as Int)
                }
                left.getType() == IntegerType && right.getType() == FloatType -> {
                    (visit(left) as Int) > (visit(right) as Float)
                }
                left.getType() == IntegerType && right.getType() == IntegerType -> {
                    (visit(left) as Int) > (visit(right) as Int)
                }
                else -> throw AssertionError("GreaterThanExpr type error.")
            }
        }
    }

    override fun visit(node: GreaterOrEqExpr): Any {
        return with (node) {
            when {
                left.getType() == FloatType && right.getType() == FloatType -> {
                    (visit(left) as Float) >= (visit(right) as Float)
                }
                left.getType() == FloatType && right.getType() == IntegerType -> {
                    (visit(left) as Float) >= (visit(right) as Int)
                }
                left.getType() == IntegerType && right.getType() == FloatType -> {
                    (visit(left) as Int) >= (visit(right) as Float)
                }
                left.getType() == IntegerType && right.getType() == IntegerType -> {
                    (visit(left) as Int) >= (visit(right) as Int)
                }
                else -> throw AssertionError("GreaterOrEqExpr type error.")
            }
        }
    }

    override fun visit(node: LessThanExpr): Any {
        return with (node) {
            when {
                left.getType() == FloatType && right.getType() == FloatType -> {
                    (visit(left) as Float) < (visit(right) as Float)
                }
                left.getType() == FloatType && right.getType() == IntegerType -> {
                    (visit(left) as Float) < (visit(right) as Int)
                }
                left.getType() == IntegerType && right.getType() == FloatType -> {
                    (visit(left) as Int) < (visit(right) as Float)
                }
                left.getType() == IntegerType && right.getType() == IntegerType -> {
                    (visit(left) as Int) < (visit(right) as Int)
                }
                else -> throw AssertionError("LessThanExpr type error.")
            }
        }
    }

    override fun visit(node: LessOrEqExpr): Any {
        return with (node) {
            when {
                left.getType() == FloatType && right.getType() == FloatType -> {
                    (visit(left) as Float) <= (visit(right) as Float)
                }
                left.getType() == FloatType && right.getType() == IntegerType -> {
                    (visit(left) as Float) <= (visit(right) as Int)
                }
                left.getType() == IntegerType && right.getType() == FloatType -> {
                    (visit(left) as Int) <= (visit(right) as Float)
                }
                left.getType() == IntegerType && right.getType() == IntegerType -> {
                    (visit(left) as Int) <= (visit(right) as Int)
                }
                else -> throw AssertionError("LessOrEqExpr type error.")
            }
        }
    }

    override fun visit(node: AdditionExpr): Any {
        return with (node) {
            when {
                left.getType() == FloatType && right.getType() == FloatType -> {
                    (visit(left) as Float) + (visit(right) as Float)
                }
                left.getType() == FloatType && right.getType() == IntegerType -> {
                    (visit(left) as Float) + (visit(right) as Int)
                }
                left.getType() == IntegerType && right.getType() == FloatType -> {
                    (visit(left) as Int) + (visit(right) as Float)
                }
                left.getType() == IntegerType && right.getType() == IntegerType -> {
                    (visit(left) as Int) + (visit(right) as Int)
                }
                else -> throw AssertionError("AdditionExpr type error.")
            }
        }
    }

    override fun visit(node: SubtractionExpr): Any {
        return with (node) {
            when {
                left.getType() == FloatType && right.getType() == FloatType -> {
                    (visit(left) as Float) - (visit(right) as Float)
                }
                left.getType() == FloatType && right.getType() == IntegerType -> {
                    (visit(left) as Float) - (visit(right) as Int)
                }
                left.getType() == IntegerType && right.getType() == FloatType -> {
                    (visit(left) as Int) - (visit(right) as Float)
                }
                left.getType() == IntegerType && right.getType() == IntegerType -> {
                    (visit(left) as Int) - (visit(right) as Int)
                }
                else -> throw AssertionError("SubtractionExpr type error.")
            }
        }
    }

    override fun visit(node: MultiplicationExpr): Any {
        return with (node) {
            when {
                left.getType() == FloatType && right.getType() == FloatType -> {
                    (visit(left) as Float) * (visit(right) as Float)
                }
                left.getType() == FloatType && right.getType() == IntegerType -> {
                    (visit(left) as Float) * (visit(right) as Int)
                }
                left.getType() == IntegerType && right.getType() == FloatType -> {
                    (visit(left) as Int) * (visit(right) as Float)
                }
                left.getType() == IntegerType && right.getType() == IntegerType -> {
                    (visit(left) as Int) * (visit(right) as Int)
                }
                else -> throw AssertionError("MultiplicationExpr type error.")
            }
        }
    }

    override fun visit(node: DivisionExpr): Any {
        return with (node) {
            when {
                left.getType() == FloatType && right.getType() == FloatType -> {
                    (visit(left) as Float) / (visit(right) as Float)
                }
                left.getType() == FloatType && right.getType() == IntegerType -> {
                    (visit(left) as Float) / (visit(right) as Int)
                }
                left.getType() == IntegerType && right.getType() == FloatType -> {
                    (visit(left) as Int) / (visit(right) as Float)
                }
                left.getType() == IntegerType && right.getType() == IntegerType -> {
                    (visit(left) as Int) / (visit(right) as Int)
                }
                else -> throw AssertionError("DivisionExpr type error.")
            }
        }
    }

    override fun visit(node: ModuloExpr): Any {
        return with (node) {
            when {
                left.getType() == FloatType && right.getType() == FloatType -> {
                    (visit(left) as Float) % (visit(right) as Float)
                }
                left.getType() == FloatType && right.getType() == IntegerType -> {
                    (visit(left) as Float) % (visit(right) as Int)
                }
                left.getType() == IntegerType && right.getType() == FloatType -> {
                    (visit(left) as Int) % (visit(right) as Float)
                }
                left.getType() == IntegerType && right.getType() == IntegerType -> {
                    (visit(left) as Int) % (visit(right) as Int)
                }
                else -> throw AssertionError("ModuloExpr type error.")
            }
        }
    }

    override fun visit(node: NegationExpr): Any {
        return with (node) {
            when {
                value.getType() == FloatType -> -(visit(value) as Float)
                value.getType() == IntegerType -> -(visit(value) as Int)
                else -> throw AssertionError("NegationExpr type error.")
            }
        }
    }

    override fun visit(node: NotExpr): Any {
        return !(visit(node.value) as Boolean)
    }

    override fun visit(node: LookupExpr): Any {
        return when (node.lookupType) {
            // Multi-state
            LookupExprType.MULTI_STATE -> {
                val state = visit(node.target) as StateValue
                val index = visit(node.index) as Int
                StateValue(state.decl, index)
            }

            // Neighbourhood
            LookupExprType.NEIGHBOURHOOD -> {
                val list = visit(node.target) as List<*>
                val index = visit(node.index) as Int
                list[index]!!
            }

            // Array
            LookupExprType.ARRAY -> {
                val list = visit(node.target) as MutableList<*>
                val index = visit(node.index) as Int
                list[index]!!
            }

            // Error
            LookupExprType.UNKNOWN -> throw InterpretRuntimeException("Could not determined lookup method.")
        }
    }

    override fun visit(node: SizedArrayExpr): Any {
        if (node.body != null) {
            return visit(node.body)
        } else {
            val type = node.getType()
            if (type is ArrayType && type.subtype !is ArrayType) {
                return Array(node.declaredSize[0]!!) { getDefaultArrayValue(type.subtype) }
            } else {
                TODO("not implemented") // We need to construct default values that are arrays
            }
        }
    }

    override fun visit(node: ArrayLiteralExpr): Any {
        return Array(node.size!!) { i ->
            when {
                i < node.values.size -> visit(node.values[i])
                node.getType() !is ArrayType -> getDefaultArrayValue(node.getType())
                else -> TODO("not implemented") // Default values that are arrays. They need a size from somewhere.
            }
        }
    }

    fun getDefaultArrayValue(type: Type): Any {
        return when (type) {
            IntegerType -> 0
            FloatType -> 0.0
            BooleanType -> false
            StateType -> defaultStateValue!!
            LocalNeighbourhoodType -> emptyList<StateValue>()
            else -> throw AssertionError("'$type' has no default array value.")
        }
    }

    override fun visit(node: Identifier): Any {
        return stack[node.spelling]
    }

    override fun visit(node: FuncCallExpr): Any {
        val funcDecl = stack[node.ident] as FuncDecl
        val actualArguments = node.args.map { visit(it) }
        stack.pushStack()
        stack.openScope()
        for ((i, formalArg) in funcDecl.args.withIndex()) {
            stack[formalArg.ident] = actualArguments[i]
        }
        val result = visit(funcDecl.body)
        stack.closeScope()
        stack.closeScope()
        return result
    }

    override fun visit(node: StateIndexExpr): Any {
        return stack["#"]
    }

    override fun visit(node: IntLiteral): Any {
        return node.value
    }

    override fun visit(node: BoolLiteral): Any {
        return node.value
    }

    override fun visit(node: FloatLiteral): Any {
        return node.value
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
        if (node.isDeclaration) {
            stack.declare(node.ident, visit(node.expr))
        } else {
            stack[node.ident] = visit(node.expr)
        }
        return Unit
    }

    override fun visit(node: IfStmt): Any {
        for (conditionalBlock in node.conditionals) {
            if (visit(conditionalBlock.expr) as Boolean) {
                val value = visit(conditionalBlock.block)
                if (value != Unit) {
                    return value
                }
                return Unit
            }
        }
        if (node.elseBlock != null) {
            return visit(node.elseBlock)
        }
        return Unit
    }

    override fun visit(node: BecomeStmt): Any {
        return visit(node.state)
    }

    override fun visit(node: ReturnStmt): Any {
        return visit(node.expr)
    }

    override fun visit(node: ConditionalBlock): Any {
        throw InterpreterVisitException(node)
    }

    override fun visit(node: FunctionArgument): Any {
        throw InterpreterVisitException(node)
    }

    override fun visit(node: WorldNode): Any {
        throw InterpreterVisitException(node)
    }

    override fun visit(node: WorldDimension): Any {
        throw InterpreterVisitException(node)
    }

    override fun visit(node: ForLoopStmt): Any {
        stack.openScope()
        var result: Any = Unit

        if (node.initPart != null) visit(node.initPart)
        while (visit(node.condition) as Boolean) {
            val value = visit(node.body)
            if (value == BreakValue) break
            if (value == ContinueValue) continue
            if (value != Unit) {
                result = value
                break
            }
            if (node.postIterationPart != null) visit(node.postIterationPart)
        }

        stack.closeScope()
        return result
    }

    override fun visit(node: BreakStmt): Any {
        return BreakValue
    }

    override fun visit(node: ContinueStmt): Any {
        return ContinueValue
    }

    override fun visit(node: CodeBlock): Any {
        stack.openScope()
        // Execute each statement in order, until one returns something else than Unit. Return that value.
        var result: Any = Unit
        for (statement in node.body) {
            val value = visit(statement)
            if (value != Unit) {
                result = value
                break
            }
        }
        stack.closeScope()
        return result
    }

    override fun visit(node: IntToFloatConversion): Any {
        return (visit(node.expr) as Int).toFloat()
    }

    override fun visit(node: StateArrayToLocalNeighbourhoodConversion): Any {
        return (visit(node.expr) as List<*>).toMutableList()
    }
}
