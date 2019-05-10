package dk.aau.cs.d409f19.cellumata.visitors

import dk.aau.cs.d409f19.cellumata.CompileError
import dk.aau.cs.d409f19.cellumata.ErrorLogger
import dk.aau.cs.d409f19.cellumata.ast.*
import java.lang.AssertionError

/**
 * Error for violation of the type rules
 */
class TypeError(ctx: SourceContext, description: String) : CompileError(ctx, description)

/**
 * Synthesizes types by moving them up the abstract syntax tree according to the type rules, and check that there is no violation of the type rules
 */
class TypeChecker(symbolTable: Table) : ScopedASTVisitor(symbolTable = symbolTable) {
    private var expectedReturn: Type? = null
    private var isOuterArray = true

    override fun visit(node: WorldNode) {
        node.dimensions.forEach { visit(it) }
        if (node.edge != null) {
            visit(node.edge)
            if (node.edge.getType() !is StateType) {
                ErrorLogger += TypeError(
                    node.ctx,
                    "Expected edge's expressions to be of state-type. Found: ${node.edge.getType()}"
                )
            }
        }
    }

    override fun visit(node: NegationExpr) {
        super.visit(node)

        node.setType(
            when (node.value.getType()) {
                IntegerType -> IntegerType
                FloatType -> FloatType
                else -> {
                    ErrorLogger += TypeError(node.ctx, "Only float or integer can be negative.")
                    UndeterminedType
                }
            }
        )
    }

    override fun visit(node: NotExpr) {
        super.visit(node)

        if (node.value.getType() != BooleanType) {
            ErrorLogger += TypeError(node.ctx, "The NOT-operator only works on boolean expressions.")
        }

        node.setType(BooleanType)
    }

    override fun visit(node: ArrayLookupExpr) {
        super.visit(node)

        val arrayType = node.arr.getType()

        if (arrayType is ArrayType) {
            node.setType(arrayType.subtype)
        } else {
            ErrorLogger += TypeError(node.ctx, "Cannot lookup in expression of type $arrayType")
            node.setType(UndeterminedType)
        }
    }

    /**
     * Finds the type of an array based on its content. No types are changed or converted, but errors are logged if the
     * array's type (or if a sub-array's type) can't be determined.
     * @see pushDownArrayType
     */
    private fun searchArrayType(expr: Expr): Type {
        when {
            expr !is ArrayLiteralExpr -> return expr.getType()
            expr.values.isEmpty() -> return ArrayType(NoSubtypeType)
            else -> {
                // Determine subtype by comparing the types of each child expression
                var subtype = searchArrayType(expr.values[0])
                for (i in 1 until expr.values.size) {
                    val type = searchArrayType(expr.values[i])
                    when {
                        // Previous expressions contained uncertainty, use this type instead
                        subtype.baseType == NoSubtypeType || subtype.baseType == UndeterminedType -> {
                            subtype = type
                        }

                        // Ignore this expression
                        type == UndeterminedType -> {
                        }

                        // Ok
                        isCompatibleType(type, subtype) -> {
                        }

                        // The subtype should be this type instead, since we can't use conversion
                        isCompatibleType(subtype, type) -> {
                            subtype = type
                        }

                        // Conversion is not possible. Error!
                        else -> {
                            ErrorLogger += TypeError(expr.ctx, "Could not determined subtype of array.")
                            return ArrayType(UndeterminedType)
                        }
                    }
                }

                return ArrayType(subtype)
            }
        }
    }

    /**
     * Sets the type of an array and its sub-arrays. Also adds implicit conversion around child expressions if needed.
     * @see searchArrayType
     */
    private fun pushDownArrayType(expr: Expr, type: Type) {
        if (type is ArrayType && expr is ArrayLiteralExpr) {

            expr.setType(type)

            when {
                // Insert implicit int-to-float conversion if needed
                type.subtype == FloatType ->
                    for (i in expr.values.indices) {
                        if (expr.values[i].getType() == IntegerType) {
                            expr.values[i] = IntToFloatConversion(expr.values[i])
                        }
                    }

                // Insert implicit array<state>-to-neighbourhood conversion if needed
                type.subtype == LocalNeighbourhoodType ->
                    for (i in expr.values.indices) {
                        if (expr.values[i] is ArrayLiteralExpr) {
                            expr.values[i].setType(ArrayType(StateType))
                            expr.values[i] = StateArrayToLocalNeighbourhoodConversion(expr.values[i])
                        }
                    }

                // push down subtype
                else ->
                    expr.values.forEach { pushDownArrayType(it, type.subtype) }
            }

        } else if (type == UndeterminedType && expr is ArrayLiteralExpr) {
            // There's a type error in the array or one of its siblings
            expr.setType(UndeterminedType)

        } else if (type != expr.getType()) {
            ErrorLogger += TypeError(
                expr.ctx,
                "Should never happen: Could not push down array type '$type' over expr '$expr'"
            )
        }
    }

    /**
     * Finds the size of the largest subarray in each dimension.
     * Note: if there are empty array literals in the tree, this function may return fewer dimensions than expected.
     */
    private fun searchSize(
        node: ArrayLiteralExpr,
        sizes: MutableList<Int> = mutableListOf(),
        depth: Int = 0
    ): MutableList<Int> {

        if (sizes.size <= depth) {
            sizes.add(node.values.size)
        } else if (sizes[depth] < node.values.size) {
            sizes[depth] = node.values.size
        }

        node.values.forEach {
            if (it is ArrayLiteralExpr) {
                searchSize(it, sizes, depth + 1)
            }
        }

        return sizes
    }

    private fun pushDownSize(n: Expr, size: List<Int>) {
        if (n is ArrayLiteralExpr) {
            n.size = size[0]
            n.values.forEach {
                pushDownSize(it, size.subList(1, size.size))
            }
        }
    }

    /**
     * Compares two types and returns whether type1 is compatible with type2, i.e. they are equivalent types or
     * type1 can be converted to type2. The function accounts for NoSubtypeType and UndeterminedType and
     * handles nested checking for arrays.
     * @return Returns true if the types are equivalent
     */
    private fun isCompatibleType(type1: Type, type2: Type): Boolean {
        if ((type1 == UndeterminedType || type1 == NoSubtypeType)
            || (type2 == UndeterminedType || type2 == NoSubtypeType)
        ) {
            return true
        }

        if (type1 is ArrayType && type2 is ArrayType) {
            return isCompatibleType(type1.subtype, type2.subtype)
        }

        if (type1 == IntegerType && type2 == FloatType) {
            return true
        }

        if (type1 is ArrayType && type1.subtype == StateType && type2 == LocalNeighbourhoodType) {
            return true
        }

        return type1 == type2
    }

    override fun visit(node: SizedArrayExpr) {
        if (!isOuterArray) {
            ErrorLogger += TypeError(node.ctx, "Sized arrays cannot be declared inside other arrays.")
            return
        }

        isOuterArray = false
        super.visit(node)
        isOuterArray = true

        /* We need to do two things:
        1. Find the type of the array and push it down to any child arrays
        2. Determine the sizes of each dimension and push it down to any child arrays */

        // type check
        if (node.body == null || node.body.values.isEmpty()) {
            // No values specified, nothing to check against
            return

        } else {
            // Check if declared type matches the type of the body
            val type = searchArrayType(node.body)
            if (isCompatibleType(type, node.declaredType)) {
                pushDownArrayType(node.body, node.declaredType)
                node.setType(node.declaredType)

            } else {
                ErrorLogger += TypeError(node.ctx, "The type of the array's body does not adhere to the declared type.")
            }
        }

        // Sizes
        val literalSizes = searchSize(node.body)
        // Check if declared size and actual size are consistent. If the base type is LocalNeighbourhoodType we allow
        // the literal size to be one greater due to array<state>-to-neighbourhood conversion, hence the -1
        if (if (node.declaredType.baseType == LocalNeighbourhoodType)
                literalSizes.size - 1 > node.declaredSize.size
            else literalSizes.size > node.declaredSize.size
        ) {
            ErrorLogger += CompileError(node.ctx, "The array body has more dimensions than the amount declared.")
            // We add some null dimensions to prevent more errors from happening
            node.declaredSize = literalSizes.mapIndexed { i, _ ->
                if (i < node.declaredSize.size) node.declaredSize[i] else null
            }
        }
        val finalSizes = node.declaredSize.mapIndexed { i, declaredSize ->
            if (declaredSize == null && i >= literalSizes.size) {
                ErrorLogger += CompileError(node.ctx, "Could not determine size of dimension $i of array.")
                -1
            } else if (declaredSize == null) {
                literalSizes[i]
            } else if (declaredSize < literalSizes[i]) {
                ErrorLogger += CompileError(
                    node.ctx,
                    "Dimension $i of array's body is greater than the declared size of dimension $i."
                )
                literalSizes[i]
            } else {
                declaredSize
            }
        }.toList()

        // Update declared sizes so there are no unknowns
        node.declaredSize = finalSizes

        // Push down size
        pushDownSize(node.body, finalSizes)
    }

    override fun visit(node: ArrayLiteralExpr) {

        if (isOuterArray) {
            // This array literal is an outermost array and should determine the size and type of itself and
            // its children based on the values of all the children

            // Visit children
            if (node.values.isNotEmpty()) {
                isOuterArray = false
                super.visit(node)
                isOuterArray = true
            } else {
                node.size = 0
                ErrorLogger += TypeError(node.ctx, "Cannot determine the type an empty array literal.")
                return
            }

            /* We need to do two things:
            1. Find the type of the array and push it down to any child arrays
            2. Determine the sizes of each dimension and push it down to any child arrays */

            // Type
            val type = searchArrayType(node)
            pushDownArrayType(node, type)

            // Sizes
            val sizes = searchSize(node)
            pushDownSize(node, sizes)

        } else {
            super.visit(node)
        }
    }

    override fun visit(node: Identifier) {
        // Get type of name
        val decl = symbolTableSession.getSymbol(node.spelling)!!
        if (decl is FuncDecl) {
            node.setType(UndeterminedType)
            ErrorLogger += TypeError(
                node.ctx,
                "Found function name '${node.spelling}', but the function is not called."
            )
        } else {
            node.setType(
                when (decl) {
                    is StateDecl -> StateType
                    is NeighbourhoodDecl -> LocalNeighbourhoodType
                    is AssignStmt -> decl.expr.getType()
                    is ConstDecl -> decl.expr.getType()
                    is FunctionArgument -> decl.type
                    else -> throw AssertionError("Identifier '${node.spelling}' is declared as a $'${decl.javaClass}' and can't determine its type.")
                }
            )
        }
    }

    override fun visit(node: EqualityComparisonExpr) {
        super.visit(node)

        val lt = node.left.getType()
        val rt = node.right.getType()

        when {
            // Could not determine type of a child node, so we can't raise any errors
            lt == UndeterminedType || rt == UndeterminedType -> {
            }

            // Both are the same, awesome
            lt == rt -> {
            }

            // int-to-float conversion for left child
            rt == FloatType && lt == IntegerType -> {
                node.left = IntToFloatConversion(node.left)
            }

            // int-to-float conversion for right child
            lt == FloatType && rt == IntegerType -> {
                node.right = IntToFloatConversion(node.right)
            }

            // state-array-to-neighbourhood conversion for left child
            rt == LocalNeighbourhoodType && lt is ArrayType && lt.subtype == StateType -> {
                node.left = StateArrayToLocalNeighbourhoodConversion(node.left)
            }

            // state-array-to-neighbourhood conversion for right child
            lt == LocalNeighbourhoodType && rt is ArrayType && rt.subtype == StateType -> {
                node.right = StateArrayToLocalNeighbourhoodConversion(node.right)
            }

            // Something is wrong, raise an error
            else -> {
                ErrorLogger += TypeError(node.ctx, "Right and left hand side of must be the same type.")
            }
        }

        node.setType(BooleanType)
    }

    override fun visit(node: BinaryArithmeticExpr) {
        super.visit(node)

        val lt = node.left.getType()
        val rt = node.right.getType()

        node.setType(
            when {
                // Could not determine type of a child node, so we can't determine the type of this node
                lt == UndeterminedType || rt == UndeterminedType -> UndeterminedType

                // Both are ints, or both are floats
                (lt == IntegerType || lt == FloatType) && lt == rt -> lt

                // int -> float conversion for left child
                lt == IntegerType && rt == FloatType -> {
                    node.left = IntToFloatConversion(node.left)
                    FloatType
                }

                // int -> float conversion for right child
                lt == FloatType && rt == IntegerType -> {
                    node.right = IntToFloatConversion(node.right)
                    FloatType
                }

                // Something is wrong, raise an error
                else -> {
                    ErrorLogger += TypeError(node.ctx, "Right and left hand side of must be either float or integer.")
                    UndeterminedType
                }
            }
        )
    }

    override fun visit(node: BinaryBooleanExpr) {
        super.visit(node)

        val lt = node.left.getType()
        val rt = node.right.getType()

        if (lt != UndeterminedType && rt != UndeterminedType
            && lt != BooleanType && rt != BooleanType
        ) {

            ErrorLogger += TypeError(node.ctx, "Right and left hand side of must be boolean.")
        }

        node.setType(BooleanType)
    }

    override fun visit(node: NumericComparisonExpr) {
        super.visit(node)

        val lt = node.left.getType()
        val rt = node.right.getType()

        when {
            // Could not determine type of a child node, so we can't conclude more type errors
            lt == UndeterminedType || rt == UndeterminedType -> {
            }

            // Both are ints, or both are floats. Perfect!
            (lt == IntegerType || lt == FloatType) && lt == rt -> {
            }

            // int -> float conversion for left child
            lt == IntegerType && rt == FloatType -> node.left = IntToFloatConversion(node.left)

            // int -> float conversion for right child
            lt == FloatType && rt == IntegerType -> node.right = IntToFloatConversion(node.right)

            // Something is wrong, raise an error
            else ->
                ErrorLogger += TypeError(node.ctx, "Right and left hand side of must be either float or integer.")
        }

        node.setType(BooleanType)
    }

    override fun visit(node: ReturnStmt) {
        super.visit(node)

        // Return statements are only allowed in functions
        // if expectedReturn is null, sanity-checker failed
        expectedReturn!!

        val exprType = node.expr.getType()
        if (expectedReturn != exprType) {
            // Maybe we can do implicit conversion?
            if (expectedReturn == FloatType && exprType == IntegerType) {
                node.expr = IntToFloatConversion(node.expr)
            } else if ((expectedReturn == LocalNeighbourhoodType && canConvertToNeighbourhood(exprType))) {
                // If the expression is an empty array, we can now conclude, that it is an array of states
                if (exprType is ArrayType && exprType.subtype == NoSubtypeType) {
                    node.expr.setType(ArrayType(StateType))
                }
                node.expr = StateArrayToLocalNeighbourhoodConversion(node.expr)
            } else {
                ErrorLogger += TypeError(
                    node.ctx,
                    "Wrong return type (${node.expr.getType()}). Expected $expectedReturn"
                )
            }
        }
    }

    override fun visit(node: BecomeStmt) {
        super.visit(node)

        // If state which the become statement is to become is not of state type, throw error
        if (node.state.getType() !is StateType) {
            ErrorLogger += TypeError(
                node.ctx,
                "Expected statement's expressions to be of state-type. Found: ${node.state.getType()}"
            )
        }
    }

    override fun visit(node: FuncDecl) {
        expectedReturn = node.returnType
        super.visit(node)
        expectedReturn = null
    }

    override fun visit(node: FuncCallExpr) {
        super.visit(node)

        val funcDecl = symbolTableSession.getSymbol(node.ident)

        // If node is not a function, register error and continue type-checking
        if (funcDecl !is FuncDecl) {
            node.setType(UndeterminedType)
            ErrorLogger += TypeError(node.ctx, "\"${node.ident}\" cannot be called, as it is not a function")

        } else { // If node is a function, continue type-checking return type and arguments

            // Set type of this node to the return type of the function
            node.setType(funcDecl.returnType)

            // If actual and formal arguments are of equal size, type-check them
            if (node.args.size == funcDecl.args.size) {

                for (i in node.args.indices) {

                    val callArg = node.args[i]
                    val funcDeclArg = funcDecl.args[i]

                    val callArgType = callArg.getType()
                    val funcDeclArgType = funcDeclArg.type

                    // If each argument are not typewise congruent or can be implicitly converted, register error
                    when {
                        // Ok
                        callArgType == funcDeclArgType -> {
                        }

                        // Int-to-float conversion
                        callArgType == IntegerType && funcDeclArgType == FloatType -> {
                            node.args[i] = IntToFloatConversion(node.args[i])
                        }

                        // State-array-to-neighbourhood conversion
                        canConvertToNeighbourhood(callArgType) && funcDeclArgType == LocalNeighbourhoodType -> {
                            node.args[i] = StateArrayToLocalNeighbourhoodConversion(node.args[i])
                        }

                        // No conversion is possible. Error!
                        else -> {
                            ErrorLogger += TypeError(
                                node.ctx,
                                "Actual argument \"${callArg.ctx.text}\", of type ${callArg.getType()}, was not equal to " +
                                        "formal argument \"${funcDeclArg.ident}\", of type ${funcDeclArg.type}"
                            )
                        }
                    }
                }

            } else { // If formal and actual arguments differ in size, register error
                ErrorLogger += CompileError(
                    node.ctx,
                    "Size of actual arguments: ${node.args.size} to function-call \"${node.ident}\" differ from " +
                            "formal arguments' size ${funcDecl.args.size}"
                )
            }
        }
    }

    override fun visit(node: ConstDecl) {
        super.visit(node)
        node.type = node.expr.getType()
    }

    override fun visit(node: AssignStmt) {
        super.visit(node)
    }

    /**
     * Returns true of the given type can be converted to LocalNeighbourhoodType
     */
    private fun canConvertToNeighbourhood(type: Type) = type == ArrayType(StateType) || type == ArrayType(NoSubtypeType)
}
