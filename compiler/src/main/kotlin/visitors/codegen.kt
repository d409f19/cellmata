package dk.aau.cs.d409f19.cellumata.visitors

import dk.aau.cs.d409f19.cellumata.ast.*
import java.util.*

sealed class KotlinCodegenError : Error()

class KotlinCodegen_ErrorDecl : KotlinCodegenError()

class KotlinCodegen_ErrorExpr : KotlinCodegenError()

class KotlinCodegen_ErrorStmt : KotlinCodegenError()

class KotlinCodegen_InvalidIdentLookup : KotlinCodegenError()

class KotlinCodegen_FoundUncheckedType : KotlinCodegenError()

class KotlinCodegen_FoundUndefinedWorldType : KotlinCodegenError()

class KotlinCodegenError_FoundUnknownLookupType : KotlinCodegenError()

class KotlinCodegenError_FoundUndeterminedType : KotlinCodegenError()

class KotlinCodegenError_FoundNoSubtypeType : KotlinCodegenError()

class KotlinCodegenError_Unreachable: KotlinCodegenError()


class KotlinCodegen : ASTVisitor<String> {
    /**
     * String used for indentation in emitted code
     */
    private val INDENT = "    "
    /**
     * Maps states names to an unique integer identifier
     */
    private var stateIDs: MutableMap<String, Int> = mutableMapOf()
    /**
     * Used to keep track of the next available label
     */
    private var labelCounter = 0

    /**
     * Acts like symbol table in that it keeps track of mapping labels to states, and tracking scopes.
     */
    private var mapping: Stack<Map<String, String>> = Stack()

    /**
     * Number of dimensions
     */
    private var dimCount = 0

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

    /**
     * Emit state dispatcher.
     * The generated function calls the state function with the logic for the current cell's state.
     */
    private fun emitDispatcher(): String {
        val builder = StringBuilder()
        builder.appendln(
            """
            override fun updateCell(worldView: IWorldView): Int {
            ${INDENT}return when(worldView.getCell(0, 0)) {
        """.trimIndent()
        )

        stateIDs.forEach { (_, id) ->
            builder.appendln("$INDENT$INDENT$id -> state_$id(worldView)")
        }

        builder.append(
            """
            $INDENT${INDENT}else -> throw Error()
            $INDENT}
            }
        """.trimIndent()
        )

        return builder.toString()
    }

    /**
     * Emit the main function starting the generated program
     */
    private fun emitMain(world: WorldNode, states: List<StateDecl>): String {
        val builder = StringBuilder()

        builder.append(
            """
            fun main() {
                GraphicalDriver(WorldConfiguration(listOf(
        """.trimIndent()
        )

        // Emit list containing the size of each dimension
        world.dimensions.forEachIndexed { i, it ->
            if (i > 0) {
                builder.append(", ")
            }
            builder.append(it.size)
        }

        builder.append("), listOf(")
        // Emit list of colors of each state
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
        // Emit cell size
        builder.append(world.cellSize)
        builder.append(", ")
        // Emit tickrate
        builder.append(world.tickrate)
        // Emit instantiation of generated cell program
        builder.append("), ProgramImpl(), ")
        // Emit instatiation of the WorldView factory
        builder.append("MultiWorldType(")

        // Emit the edge state
        builder.append(world.edge?.let { stateIDs[it.spelling] } ?: 0)
        // Emit the type (EDGE, WRAPPING) for each dimension
        builder.append(", listOf(")

        world.dimensions.forEachIndexed { i, it ->
            if (i > 0) {
                builder.append(", ")
            }
            builder.append(
                when (it.type) {
                    WorldType.WRAPPING -> "DimensionType.WRAPPING"
                    WorldType.EDGE -> "DimensionType.EDGE"
                    WorldType.UNDEFINED -> throw KotlinCodegen_FoundUndefinedWorldType()
                }
            )
        }

        builder.append(
            """))).run()
            }
        """.trimIndent()
        )

        return builder.toString()
    }

    /**
     * Emit a function that given a state, return the id of base state for the mutltistate
     */
    private fun emitBaseStateLookup(states: List<StateDecl>): String {
        val builder = StringBuilder()

        builder.append("""
            fun baseStateLookup(state: Int): Int {
            ${INDENT}when(state) {
        """.trimIndent())

        states.forEach {
            val baseId = stateIDs[it.ident]!!
            for (stateId in baseId until baseId + it.multiStateCount) {
                builder.append("$INDENT$INDENT$stateId -> $baseId")
            }
        }

        builder.append("""
            $INDENT}
            }
        """.trimIndent())

        return builder.toString()
    }

    /**
     * Emit a function that given a state, return its index in its multistate
     */
    private fun emitMultiStateIndexLookup(states: List<StateDecl>): String {
        val builder = StringBuilder()

        builder.append("""
            fun baseMultiStateIndexLookup(state: Int): Int {
            ${INDENT}when(state) {
        """.trimIndent())

        states.forEach {
            val baseId = stateIDs[it.ident]!!
            for (x in 0 until it.multiStateCount) {
                builder.append("$INDENT$INDENT${baseId + x} -> $x")
            }
        }

        builder.append("""
            $INDENT}
            }
        """.trimIndent())

        return builder.toString()
    }

    /**
     * Emit a function that given a multistate and a index in the in the multistate, returns the id of the concrete state from the given multistate
     */
    private fun emitMultiStateLookup(states: List<StateDecl>): String {
        val builder = StringBuilder()

        builder.append("""
            fun baseMultiStateLookup(state: Int, index: Int): Int {

            if (index < 0) {
            ${INDENT}throw IndexOutOfBoundsException()
            }

            val baseId = baseStateLookup(state)
            ${INDENT}return when(baseId) {
        """.trimIndent())

        states.forEach {
            val baseId = stateIDs[it.ident]!!
            builder.append("""
                $INDENT$INDENT$baseId -> {
                $INDENT$INDENT${INDENT}if(index >= ${it.multiStateCount}) {
                $INDENT$INDENT$INDENT${INDENT}throw IndexOutOfBoundsException()
                $INDENT$INDENT}
                $INDENT${INDENT}$baseId + index
                $INDENT}
                """.trimIndent())
        }

        builder.append("""
            $INDENT${INDENT}else -> throw Error()
            $INDENT}
            }
        """.trimIndent())

        return builder.toString()
    }

    /**
     * Emit Kotlin equivalent type identifiers
     */
    private fun toKotlinType(type: Type): String {
        return when (type) {
            IntegerType -> "Int"
            FloatType -> "Float"
            BooleanType -> "Bool"
            StateType -> "Int"
            LocalNeighbourhoodType -> "List<Int>"
            is ArrayType -> "MutableList<${toKotlinType(type.subtype)}>"
            UncheckedType -> throw KotlinCodegen_FoundUncheckedType()
            UndeterminedType -> throw KotlinCodegenError_FoundUndeterminedType()
            NoSubtypeType -> throw KotlinCodegenError_FoundNoSubtypeType()
        }
    }

    override fun visit(node: RootNode): String {
        addMapping("count", "`builtin count`")
        addMapping("randi", "`builtin randi`")
        addMapping("randf", "`builtin randf`")
        addMapping("absi", "`builtin absi`")
        addMapping("absf", "`builtin absf`")
        addMapping("floor", "`builtin floor`")
        addMapping("ceil", "`builtin ceil`")
        addMapping("root", "`builtin root`")
        addMapping("pow", "`builtin pow`")

        dimCount = node.world.dimensions.size

        // For each state create an associated integer identifier, and label mapping that will be used in the final program
        var stateCounter = 0
        node.body.filterIsInstance<StateDecl>().forEach {
            stateIDs[it.ident] = stateCounter
            addMapping(it.ident, stateCounter.toString())
            stateCounter += it.multiStateCount
        }


        openScope()

        // For each const, neighbourhood, and function create a mapping to a label
        node.body.filter { it !is StateDecl }.forEach {
            when (it) {
                is ConstDecl -> addMapping(it.ident, nextLabel())
                is NeighbourhoodDecl -> addMapping(it.ident, nextLabel())
                is FuncDecl -> addMapping(it.ident, nextLabel())
                else -> throw KotlinCodegen_ErrorDecl()
            }
        }

        val builder = StringBuilder()
        builder.appendln(
            """
            package dk.aau.cs.d409f18.cellumata.codegen.kotlin.impl

            import dk.aau.cs.d409f18.cellumata.codegen.kotlin.runtime.*

            class ProgramImpl: IProgram {
            """.trimIndent()
        )

        // Emit state dispatcher
        builder.appendln(emitDispatcher().prependIndent(INDENT))

        // Multi-state
        builder.appendln(emitBaseStateLookup(node.body.filterIsInstance<StateDecl>()).prependIndent(INDENT))
        builder.appendln(emitMultiStateLookup(node.body.filterIsInstance<StateDecl>()).prependIndent(INDENT))

        // Emit the cell program
        node.body.forEach { builder.appendln(visit(it).prependIndent(INDENT) + ";") }

        builder.append(
            """
            }
        """.trimIndent()
        )

        closeScope()

        builder.append("\n\n")

        // Emit the main function such the program has a starting point
        builder.appendln(
            emitMain(
                node.world,
                node.body.filterIsInstance<StateDecl>().toList()
            )
        )

        return builder.toString()
    }

    override fun visit(node: Decl): String {
        return when (node) {
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
        val builder = StringBuilder()

        builder.appendln("fun state_${stateIDs[node.ident]}(worldView: IWorldView): Int {")

        builder.append(visit(node.body).prependIndent(INDENT))

        builder.append("}")

        return builder.toString()
    }

    override fun visit(node: NeighbourhoodDecl): String {
        val builder = StringBuilder()

        // Neighbourhood lookup function
        builder.appendln(
            """
            fun ${getMappedLabel(node.ident)}(worldView: IWorldView): List<Int> {
                return listOf(
            """.trimIndent()
        )

        node.coords.forEachIndexed { i, it ->
            builder.append("${INDENT}worldView.getCell(")
            it.axes.forEachIndexed { o, that ->
                if (o > 0) {
                    builder.append(",")
                }
                builder.append(that)
            }
            builder.append(")")
            if (i < node.coords.size - 1) {
                builder.append(",")
            } else {
                builder.appendln()
            }
        }

        builder.append(
            """
            $INDENT)
            }
        """.trimIndent()
        )

        return builder.toString()
    }

    override fun visit(node: Coordinate): String {
        // Should be unreachable
        throw KotlinCodegenError_Unreachable()
    }

    override fun visit(node: FuncDecl): String {
        val builder = StringBuilder()

        openScope()
        node.args.forEach { addMapping(it.ident, nextLabel()) }

        openScope()
        builder.append("fun ${getMappedLabel(node.ident)}(worldView: WorldView")
        node.args.forEachIndexed { i, it ->
            builder.append(", ")
            builder.append("${getMappedLabel(it.ident)}: ${toKotlinType(it.type)}")
        }
        builder.appendln("): ${toKotlinType(node.returnType)} {")
        builder.append(visit(node.body).prependIndent(INDENT))

        closeScope() // Body
        closeScope() // Function arguments

        builder.append("}")

        return builder.toString()
    }

    override fun visit(node: Expr): String {
        return when (node) {
            is BinaryExpr -> visit(node)
            is NegationExpr -> visit(node)
            is NotExpr -> visit(node)
            is LookupExpr -> visit(node)
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

    override fun visit(node: IntToFloatConversion): String {
        return "((${visit(node.expr)}).toFloat())"
    }

    override fun visit(node: StateArrayToLocalNeighbourhoodConversion): String {
        return "((${visit(node.expr)}).toList())"
    }

    override fun visit(node: BinaryExpr): String {
        return when (node) {
            is EqualityComparisonExpr -> visit(node)
            is BinaryArithmeticExpr -> visit(node)
            is NumericComparisonExpr -> visit(node)
            is BinaryBooleanExpr -> visit(node)
        }
    }

    override fun visit(node: EqualityComparisonExpr): String {
        return when (node) {
            is InequalityExpr -> visit(node)
            is EqualityExpr -> visit(node)
        }
    }

    override fun visit(node: BinaryArithmeticExpr): String {
        return when (node) {
            is AdditionExpr -> visit(node)
            is SubtractionExpr -> visit(node)
            is MultiplicationExpr -> visit(node)
            is DivisionExpr -> visit(node)
            is ModuloExpr -> visit(node)
        }
    }

    override fun visit(node: BinaryBooleanExpr): String {
        return when (node) {
            is OrExpr -> visit(node)
            is AndExpr -> visit(node)
        }
    }

    override fun visit(node: NumericComparisonExpr): String {
        return when (node) {
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
        val limit: Int = if (sizes == null) {
            values!!.size!!
        } else {
            sizes[0]
        }

        // Note: Array initialization specifies elements from the beginning of the array,
        // and any remaining elements will have the default value for the type

        for (indexCounter in 0 until limit) {
            builder.append(
                if (values != null && indexCounter < values.values.size) {
                    // Note: A explicit value has been specified for this element in the array
                    // Emit values from source code
                    val value = values.values[indexCounter]

                    when (value.getType()) {
                        IntegerType -> visit(value)
                        FloatType -> visit(value)
                        BooleanType -> visit(value)
                        StateType -> visit(value)
                        LocalNeighbourhoodType -> visit(value)
                        is ArrayType -> {
                            val arrayLiteral = value as ArrayLiteralExpr
                            emitArray(
                                sizes?.subList(1, sizes.size),
                                arrayLiteral.getType() as ArrayType,
                                arrayLiteral
                            )
                        }
                        UncheckedType -> throw KotlinCodegen_FoundUncheckedType()
                        UndeterminedType -> throw KotlinCodegenError_FoundUndeterminedType()
                        NoSubtypeType -> throw KotlinCodegenError_FoundNoSubtypeType()
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
                        UndeterminedType -> throw KotlinCodegenError_FoundUndeterminedType()
                        NoSubtypeType -> throw KotlinCodegenError_FoundNoSubtypeType()
                    }
                }
            )
        }

        return builder.toString()
    }

    override fun visit(node: LookupExpr): String {
        return when (node.lookupType) {
            LookupExprType.ARRAY, LookupExprType.NEIGHBOURHOOD -> "(${visit(node.target)}[${visit(node.index)}])"
            LookupExprType.MULTI_STATE -> {
                "(multiStateLookup((${visit(node.target)}), (${visit(node.index)})))"
            }
            LookupExprType.UNKNOWN -> throw KotlinCodegenError_FoundUnknownLookupType()
        }
    }

    override fun visit(node: SizedArrayExpr): String {
        return emitArray(node.declaredSize.map { it!! }, node.declaredType, node.body)
    }

    override fun visit(node: Identifier): String {
        return if (node.getType() == LocalNeighbourhoodType) {
            "(${getMappedLabel(node.spelling)}(worldView))"
        } else {
            "(${getMappedLabel(node.spelling)})"
        }
    }

    override fun visit(node: ModuloExpr): String {
        return "(${visit(node.left)} % ${visit(node.right)})"
    }

    override fun visit(node: FuncCallExpr): String {
        val builder = StringBuilder()
        builder.append("(")
        builder.append(getMappedLabel(node.ident))
        builder.append("(worldView")
        node.args.forEachIndexed { index, expr ->
            builder.append(", ")
            builder.append(visit(expr))
        }
        builder.append("))")

        return builder.toString()
    }

    override fun visit(node: ArrayLiteralExpr): String {
        return emitArray(null, node.getType() as ArrayType, node)
    }

    override fun visit(node: StateIndexExpr): String {
        val builder = StringBuilder()
        builder.append("(multiStateIndexLookup(worldView.getCell(")

        for (i in 0 until dimCount) {
            if (i > 0) {
                builder.append(", ")
            }
            builder.append("0")
        }

        builder.append(")))")

        return builder.toString()
    }

    override fun visit(node: IntLiteral): String {
        return "(${node.value})"
    }

    override fun visit(node: BoolLiteral): String {
        return "(${node.value})"
    }

    override fun visit(node: Stmt): String {
        return when (node) {
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
            if (i > 0) {
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
        throw KotlinCodegenError_Unreachable()
    }

    override fun visit(node: WorldNode): String {
        // Should be unreachable
        throw KotlinCodegenError_Unreachable()
    }

    override fun visit(node: WorldDimension): String {
        // Should be unreachable
        throw KotlinCodegenError_Unreachable()
    }

    override fun visit(node: ForLoopStmt): String {
        val builder = StringBuilder()

        if (node.initPart != null) {
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
