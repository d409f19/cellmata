package dk.aau.cs.d409f19.cellumata.visitors

import dk.aau.cs.d409f19.cellumata.ast.*
import java.util.*

sealed class KotlinCodegenError: Error()

class KotlinCodegen_ErrorDecl: KotlinCodegenError()

class KotlinCodegen_ErrorExpr: KotlinCodegenError()

class KotlinCodegen_ErrorStmt: KotlinCodegenError()

class KotlinCodegen_InvalidIdentLookup: KotlinCodegenError()

class KotlinCodegen_FoundUncheckedType: KotlinCodegenError()

class KotlinCodegen_FoundUndefinedWorldType: KotlinCodegenError()

class KotlinCodegen: ASTVisitor<String> {
    private val INDENT = "    "
    private var stateIDs: Map<String, Int> = mutableMapOf()
    private var labelCounter = 0

    private var mapping: Stack<Map<String, String>> = Stack()

    init {
        mapping.push(mutableMapOf())
    }

    private fun nextLabel(): String {
        return "label_${labelCounter++}"
    }

    private fun addMapping(from: String, to: String) {
        val oldMapping = mapping.pop()
        val newMapping = oldMapping.plus(Pair(from, to))
        mapping.push(newMapping)
    }

    private fun getMappedLabel(ident: String): String {
        for (scope in mapping.reversed()) {
            if (scope.containsKey(ident)) {
                return scope.getValue(ident)
            }
        }

        throw KotlinCodegen_InvalidIdentLookup()
    }

    private fun openScope() {
        mapping.push(mapOf())
    }

    private fun closeScope() {
        mapping.pop()
    }

    private fun emitDispatcher(): String {
        val builder = StringBuilder()
        builder.appendln("""
            override fun updateCell(worldView: IWorldView): Int {
            ${INDENT}return when(worldView.getCell(0, 0)) {
        """.trimIndent())

        stateIDs.forEach { (_, id) ->
            builder.appendln("$INDENT$INDENT$id -> state_$id(worldView)")
        }

        builder.append("""
            $INDENT${INDENT}else -> throw Error()
            $INDENT}
            }
        """.trimIndent())

        return builder.toString()
    }

    private fun emitMain(world: WorldNode, states: List<StateDecl>): String {
        val builder = StringBuilder()

        builder.append("""
            fun main() {
                GraphicalDriver(WorldConfiguration(listOf(
        """.trimIndent())

        world.dimensions.forEachIndexed { i, it ->
            if (i > 0) {
                builder.append(", ")
            }
            builder.append(it.size)
        }

        builder.append("), listOf(")
        states.forEachIndexed { i, it ->
            if (i > 0) {
                builder.append(", ")
            }
            builder.append("StateColor(")
            builder.append(it.red)
            builder.append(", ")
            builder.append(it.green)
            builder.append(", ")
            builder.append(it.blue)
            builder.append(")")
        }
        builder.append("), ")
        builder.append(world.cellSize)
        builder.append(", ")
        builder.append(world.tickrate)
        builder.append("), ProgramImpl(), ")

        builder.append("MultiWorldType(")

        val edge = world.edge
        builder.append(edge?.let { stateIDs[it.spelling] } ?: 0)
        builder.append(", listOf(")

        world.dimensions.forEachIndexed { i, it ->
            if(i > 0) {
                builder.append(", ")
            }
            builder.append(when(it.type) {
                WorldType.WRAPPING -> "DimensionType.WRAPPING"
                WorldType.EDGE -> "DimensionType.EDGE"
                WorldType.UNDEFINED -> throw KotlinCodegen_FoundUndefinedWorldType()
            })
        }

        builder.append("""))).run()
            }
        """.trimIndent())

        return builder.toString()
    }

    private fun toKotlinType(type: Type): String {
        return when(type) {
            IntegerType -> "Int"
            FloatType -> "Float"
            BooleanType -> "Bool"
            StateType -> "Int"
            LocalNeighbourhoodType -> "List<Int>"
            is ArrayType -> "MutableList<${toKotlinType(type.subtype)}>"
            UncheckedType -> throw KotlinCodegen_FoundUncheckedType()
            UndeterminedType -> TODO()
            NoSubtypeType -> TODO()
        }
    }

    override fun visit(node: RootNode): String {
        // ToDo write implementation of builtins
        addMapping("count", nextLabel())
        addMapping("randi", nextLabel())
        addMapping("randf", nextLabel())
        addMapping("absi", nextLabel())
        addMapping("absf", nextLabel())
        addMapping("floor", nextLabel())
        addMapping("ceil", nextLabel())
        addMapping("root", nextLabel())
        addMapping("pow", nextLabel())

        var stateCounter = 0
        node.body.filterIsInstance<StateDecl>().forEach {
            // ToDo multi-states
            stateIDs = stateIDs.plus(pair = Pair(it.ident, stateCounter))
            addMapping(it.ident, stateCounter.toString())
            stateCounter += 1
        }


        openScope()

        node.body.filter { it !is StateDecl }.forEach {
            when(it) {
                is ConstDecl -> addMapping(it.ident, nextLabel())
                is NeighbourhoodDecl -> addMapping(it.ident, nextLabel())
                is FuncDecl -> addMapping(it.ident, nextLabel())
                is ErrorDecl -> throw KotlinCodegen_ErrorDecl()
            }
        }

        val builder = StringBuilder()
        builder.appendln("""
            package dk.aau.cs.d409f18.cellumata.codegen.kotlin.impl

            import dk.aau.cs.d409f18.cellumata.codegen.kotlin.runtime.*

            class ProgramImpl: IProgram {
            """.trimIndent())

        builder.appendln(emitDispatcher().prependIndent(INDENT))
        node.body.forEach { builder.appendln(visit(it).prependIndent(INDENT) + ";") }

        builder.append("""
            }
        """.trimIndent())

        closeScope()

        builder.append("\n\n")
        builder.appendln(emitMain(
            node.world,
            node.body.filterIsInstance<StateDecl>().toList()
        ))

        return builder.toString()
    }

    override fun visit(node: Decl): String {
        return when(node) {
            is ConstDecl -> visit(node)
            is StateDecl -> visit(node)
            is NeighbourhoodDecl -> visit(node)
            is FuncDecl -> visit(node)
            is ErrorDecl -> throw KotlinCodegen_ErrorDecl()
        }
    }

    override fun visit(node: ConstDecl): String {
        return "val ${getMappedLabel(node.ident)} = ${visit(node.expr)};"
    }

    override fun visit(node: StateDecl): String {
        // ToDo multi-state

        val builder = StringBuilder()

        builder.appendln("""
            fun state_${stateIDs[node.ident]}(worldView: IWorldView): Int {
        """.trimIndent())

        builder.append(visit(node.body).prependIndent(INDENT))

        builder.append("}")

        return builder.toString()
    }

    override fun visit(node: NeighbourhoodDecl): String {
        val builder = StringBuilder()

        // Neighbourhood lookup function
        builder.appendln("""
            fun ${getMappedLabel(node.ident)}(worldView: IWorldView): List<Int> {
                return listOf(
            """.trimIndent())

        node.coords.forEachIndexed { i, it ->
            builder.append("${INDENT}worldView.getCell(")
            it.axes.forEachIndexed { o, that ->
                if (o > 0) {
                    builder.append(",")
                }
                builder.append(that)
            }
            builder.append(")")
            if (i < node.coords.size-1) {
                builder.append(",")
            } else {
                builder.appendln()
            }
        }

        builder.append("""
            $INDENT)
            }
        """.trimIndent())

        return builder.toString()
    }

    override fun visit(node: Coordinate): String {
        // Should be unreachable
        TODO("not implemented")
    }

    override fun visit(node: FuncDecl): String {
        val builder = StringBuilder()

        openScope()
        node.args.forEach { addMapping(it.ident, nextLabel()) }

        openScope()
        builder.append("fun ${getMappedLabel(node.ident)}(")
        node.args.forEachIndexed { i, it ->
            if(i > 0) {
                builder.append(", ")
            }
            builder.append("${getMappedLabel(it.ident)}: ${toKotlinType(it.getType()!!)}")
        }
        builder.appendln("): ${toKotlinType(node.returnType)} {")
        builder.append(visit(node.body).prependIndent(INDENT))

        closeScope() // Body
        closeScope() // Function arguments

        builder.append("}")

        return builder.toString()
    }

    override fun visit(node: Expr): String {
        return when(node) {
            is BinaryExpr -> visit(node)
            is NegationExpr -> visit(node)
            is NotExpr -> visit(node)
            is ArrayLookupExpr -> visit(node)
            is SizedArrayExpr -> visit(node)
            is ArrayLiteralExpr -> visit(node)
            is Identifier -> visit(node)
            is FuncCallExpr -> visit(node)
            is StateIndexExpr -> visit(node)
            is IntLiteral -> visit(node)
            is BoolLiteral -> visit(node)
            is FloatLiteral -> visit(node)
            is IntToFloatConversion -> visit(node)
            is StateArrayToLocalNeighbourhoodConversion -> visit(node)
            is ErrorExpr -> throw KotlinCodegen_ErrorExpr()
        }
    }

    override fun visit(node: BinaryExpr): String {
        return when(node) {
            is BinaryArithmeticExpr -> visit(node)
            is NumericComparisonExpr -> visit(node)
            is BinaryBooleanExpr -> visit(node)
        }
    }

    override fun visit(node: BinaryArithmeticExpr): String {
        return when(node) {
            is AdditionExpr -> visit(node)
            is SubtractionExpr -> visit(node)
            is MultiplicationExpr -> visit(node)
            is DivisionExpr -> visit(node)
            is ModuloExpr -> visit(node)
        }
    }

    override fun visit(node: BinaryBooleanExpr): String {
        return when(node) {
            is OrExpr -> visit(node)
            is AndExpr -> visit(node)
        }
    }

    override fun visit(node: NumericComparisonExpr): String {
        return when(node) {
            is InequalityExpr -> visit(node)
            is EqualityExpr -> visit(node)
            is GreaterThanExpr -> visit(node)
            is GreaterOrEqExpr -> visit(node)
            is LessThanExpr -> visit(node)
            is LessOrEqExpr -> visit(node)
        }
    }

    override fun visit(node: OrExpr): String {
        return "(${visit(node.left)} || ${visit(node.right)})"
    }

    override fun visit(node: AndExpr): String {
        return "(${visit(node.left)} && ${visit(node.right)})"
    }

    override fun visit(node: InequalityExpr): String {
        return "(${visit(node.left)} != ${visit(node.right)})"
    }

    override fun visit(node: EqualityExpr): String {
        return "(${visit(node.left)} == ${visit(node.right)})"
    }

    override fun visit(node: GreaterThanExpr): String {
        return "(${visit(node.left)} > ${visit(node.right)})"
    }

    override fun visit(node: GreaterOrEqExpr): String {
        return "(${visit(node.left)} >= ${visit(node.right)})"
    }

    override fun visit(node: LessThanExpr): String {
        return "(${visit(node.left)} < ${visit(node.right)})"
    }

    override fun visit(node: LessOrEqExpr): String {
        return "(${visit(node.left)} <= ${visit(node.right)})"
    }

    override fun visit(node: AdditionExpr): String {
        return "(${visit(node.left)} + ${visit(node.right)})"
    }

    override fun visit(node: SubtractionExpr): String {
        return "(${visit(node.left)} - ${visit(node.right)})"
    }

    override fun visit(node: MultiplicationExpr): String {
        return "(${visit(node.left)} * ${visit(node.right)})"
    }

    override fun visit(node: DivisionExpr): String {
        return "(${visit(node.left)} / ${visit(node.right)})"
    }

    override fun visit(node: NegationExpr): String {
        return "(-${visit(node.value)})"
    }

    override fun visit(node: NotExpr): String {
        return "(!${visit(node.value)})"
    }

    private fun emitArray(sizes: List<Int>?, type: ArrayType, values: ArrayLiteralExpr?): String {
        // Note: if both sizes and values are null, then we don't know the size of the array
        assert(sizes != null || values != null)
        // Note: sizes is empty, then we have recursed to deeply, and should instead have visited a another type of value
        assert(sizes == null || sizes.isNotEmpty())

        val builder = StringBuilder()
        var indexCounter = 0
        val limit: Int = if (sizes == null) {
            values!!.size!!
        } else {
            sizes[0]
        }

        // Note: Array initialization specifies elements from the beginning of the array,
        // and any remaining elements will have the default value for the type

        while (indexCounter < limit) {
            builder.append(if (values != null && indexCounter < values.values.size) {
                // Note: A explicit value has been specified for this element in the array
                // Emit values from source code
                val value = values.values[indexCounter]

                when(value.getType()) {
                    IntegerType -> visit(value)
                    FloatType -> visit(value)
                    BooleanType -> visit(value)
                    StateType -> visit(value)
                    LocalNeighbourhoodType -> visit(value)
                    is ArrayType -> {
                        val arrayLiteral = value as ArrayLiteralExpr
                        emitArray(
                            sizes?.subList(1, sizes.size),
                            arrayLiteral.getType()!! as ArrayType,
                            arrayLiteral
                        )
                    }
                    UncheckedType -> throw KotlinCodegen_FoundUncheckedType()
                    UndeterminedType -> TODO()
                    NoSubtypeType -> TODO()
                }
            } else {
                // Note: values is either null, or we have emitted all specified values and the remainder of values is the default value for the type
                // Emit default value
                when (type.subtype) {
                    BooleanType -> "(false)"
                    IntegerType -> "(0)"
                    FloatType -> "(0.0F)"
                    StateType -> "(0)"
                    LocalNeighbourhoodType -> "(listOf())"
                    is ArrayType -> emitArray(
                        sizes?.subList(1, sizes.size),
                        type.subtype,
                        null
                    )
                    UncheckedType -> throw KotlinCodegen_FoundUncheckedType()
                    UndeterminedType -> TODO()
                    NoSubtypeType -> TODO()
                }
            })

            indexCounter++
        }

        return builder.toString()
    }

    override fun visit(node: ArrayLookupExpr): String {
        return "(${visit(node.arr)}[${visit(node.index)}])"
    }

    override fun visit(node: SizedArrayExpr): String {
        return emitArray(node.declaredSize.map { it!! }, node.declaredType, node.body)
    }

    override fun visit(node: Identifier): String {
        return "(${getMappedLabel(node.spelling)})"
    }

    override fun visit(node: ModuloExpr): String {
        return "(${visit(node.left)} % ${visit(node.right)})"
    }

    override fun visit(node: FuncCallExpr): String {
        return "(${getMappedLabel(node.ident)}())"
    }

    override fun visit(node: ArrayLiteralExpr): String {
        return emitArray(null, node.getType()!! as ArrayType, node)
    }

    override fun visit(node: StateIndexExpr): String {
        /*
         * Idea: Emit a when statement that know based on the state id
         * which multi-state the state belongs to and the multi-state index
         */
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(node: IntLiteral): String {
        return "(${node.value})"
    }

    override fun visit(node: BoolLiteral): String {
        return "(${node.value})"
    }

    override fun visit(node: Stmt): String {
        return when(node) {
            is AssignStmt -> visit(node)
            is IfStmt -> visit(node)
            is ForLoopStmt -> visit(node)
            is BreakStmt -> visit(node)
            is ContinueStmt -> visit(node)
            is BecomeStmt -> visit(node)
            is ReturnStmt -> visit(node)
            is ErrorStmt -> throw KotlinCodegen_ErrorStmt()
        }
    }

    override fun visit(node: AssignStmt): String {
        val label = nextLabel()
        if (node.isDeclaration) {
            addMapping(node.ident, label)
        }
        return "var $label = (${visit(node.expr)})"
    }

    override fun visit(node: IfStmt): String {
        val builder = StringBuilder()

        node.conditionals.forEachIndexed { i, it ->
            if(i > 0) {
                builder.append(" else ")
            }
            builder.append(visit(it))
        }

        if (node.elseBlock != null) {
            builder.appendln(" else {")
            builder.append(visit(node.elseBlock).prependIndent(INDENT))
            builder.append('}')
        }

        return builder.toString()
    }

    override fun visit(node: BecomeStmt): String {
        return "return ${visit(node.state)}"
    }

    override fun visit(node: ReturnStmt): String {
        return "return ${visit(node.expr)}"
    }

    override fun visit(node: AST): String {
        return when (node) {
            is RootNode -> visit(node)
            is WorldNode -> visit(node)
            is Expr -> visit(node)
            is Stmt -> visit(node)
            is Decl -> visit(node)
            is Coordinate -> visit(node)
            is FunctionArgument -> visit(node)
            is ConditionalBlock -> visit(node)
            is CodeBlock -> visit(node)
            is ErrorAST -> visit(node)
        }
    }

    override fun visit(node: FloatLiteral): String {
        // String representation may not be exact, so we encode the float as bits to get an exact representation
        return "(Float.fromBits(${node.value.toBits()}))"
    }

    override fun visit(node: ConditionalBlock): String {
        val builder = StringBuilder()

        builder.append("if (${visit(node.expr)}) {\n${visit(node.block).prependIndent(INDENT)}\n}")

        return builder.toString()
    }

    override fun visit(node: FunctionArgument): String {
        // Should be unreachable
        TODO("not implemented")
    }

    override fun visit(node: WorldNode): String {
        // Should be unreachable
        TODO("not implemented")
    }

    override fun visit(node: WorldDimension): String {
        // Should be unreachable
        TODO("not implemented")
    }

    override fun visit(node: ForLoopStmt): String {
        val builder = StringBuilder()

        if(node.initPart != null) {
            builder.appendln(visit(node.initPart))
        }

        builder.append("while (")
        builder.append(visit(node.condition))
        builder.append(") {")

        builder.appendln(visit(node.body))
        if (node.postIterationPart != null) {
            builder.appendln(visit(node.postIterationPart))
        }

        builder.append("}")

        return builder.toString()
    }

    override fun visit(node: BreakStmt): String {
        return "break"
    }

    override fun visit(node: ContinueStmt): String {
        return "continue"
    }

    override fun visit(node: CodeBlock): String {
        val builder = StringBuilder()

        node.body.forEach { builder.append(visit(it)); builder.appendln(";") }

        return builder.toString()
    }
}
