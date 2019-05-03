package dk.aau.cs.d409f19.cellumata.visitors

import dk.aau.cs.d409f19.cellumata.CompileError
import dk.aau.cs.d409f19.cellumata.ErrorLogger
import dk.aau.cs.d409f19.cellumata.ast.*
import kotlin.AssertionError
import kotlin.math.min

/**
 * Error for violation of the type rules
 */
class TypeError(ctx: SourceContext, description: String) : CompileError(ctx, description)

/**
 * Synthesizes types by moving them up the abstract syntax tree according to the type rules, and check that there is no violation of the type rules
 */
class TypeChecker(symbolTable: Table) : ScopedASTVisitor(symbolTable = symbolTable) {
    private var expectedReturn: Type? = null

    override fun visit(node: OrExpr) {
        super.visit(node)

        if (node.left.getType() != BooleanType) {
            ErrorLogger += TypeError(node.left.ctx, "Expected boolean expression in left hand side of or-expression.")
        }
        if (node.right.getType() != BooleanType) {
            ErrorLogger += TypeError(node.right.ctx, "Expected boolean expression in right hand side of or-expression.")
        }

        node.setType(BooleanType)
    }

    override fun visit(node: AndExpr) {
        super.visit(node)

        if (node.left.getType() != BooleanType) {
            ErrorLogger += TypeError(node.left.ctx, "Expected boolean expression in left hand side of and-expression.")
        }
        if (node.right.getType() != BooleanType) {
            ErrorLogger += TypeError(
                node.right.ctx,
                "Expected boolean expression in right hand side of and-expression."
            )
        }

        node.setType(BooleanType)
    }

    override fun visit(node: InequalityExpr) {
        super.visit(node)

        node.setType(
            when {
                node.left.getType() == BooleanType && node.right.getType() == BooleanType -> BooleanType
                node.left.getType() == IntegerType && node.right.getType() == IntegerType -> BooleanType
                node.left.getType() == FloatType && node.right.getType() == FloatType -> BooleanType
                node.left.getType() == IntegerType && node.right.getType() == FloatType -> BooleanType
                node.left.getType() == FloatType && node.right.getType() == IntegerType -> BooleanType
                node.left.getType() == StateType && node.right.getType() == StateType -> BooleanType
                node.left.getType() == LocalNeighbourhoodType && node.right.getType() == LocalNeighbourhoodType -> BooleanType
                node.left.getType() == LocalNeighbourhoodType && node.right.getType() is ArrayType && (node.right.getType() as ArrayType).subtype == StateType -> BooleanType
                node.left.getType() is ArrayType && (node.left.getType() as ArrayType).subtype == StateType && node.right.getType() == LocalNeighbourhoodType -> BooleanType
                node.left.getType() is ArrayType && node.right.getType() is ArrayType -> BooleanType
                else -> {
                    ErrorLogger += TypeError(
                        node.ctx,
                        "Could not compare the types of right and left hand side of inequality-expression."
                    )
                    null
                }
            }
        )
    }

    override fun visit(node: EqualityExpr) {
        super.visit(node)

        node.setType(
            when {
                node.left.getType() == BooleanType && node.right.getType() == BooleanType -> BooleanType
                node.left.getType() == IntegerType && node.right.getType() == IntegerType -> BooleanType
                node.left.getType() == FloatType && node.right.getType() == FloatType -> BooleanType
                node.left.getType() == IntegerType && node.right.getType() == FloatType -> BooleanType
                node.left.getType() == FloatType && node.right.getType() == IntegerType -> BooleanType
                node.left.getType() == StateType && node.right.getType() == StateType -> BooleanType
                node.left.getType() == LocalNeighbourhoodType && node.right.getType() == LocalNeighbourhoodType -> BooleanType
                node.left.getType() == LocalNeighbourhoodType && node.right.getType() is ArrayType && (node.right.getType() as ArrayType).subtype == StateType -> BooleanType
                node.left.getType() is ArrayType && (node.left.getType() as ArrayType).subtype == StateType && node.right.getType() == LocalNeighbourhoodType -> BooleanType
                node.left.getType() is ArrayType && node.right.getType() is ArrayType -> BooleanType
                else -> {
                    ErrorLogger += TypeError(
                        node.ctx,
                        "Could not compare the types of right and left hand side of equality-expression."
                    )
                    null
                }
            }
        )
    }

    override fun visit(node: GreaterThanExpr) {
        super.visit(node)

        node.setType(
            when {
                node.left.getType() == IntegerType && node.right.getType() == IntegerType -> BooleanType
                node.left.getType() == FloatType && node.right.getType() == FloatType -> BooleanType
                node.left.getType() == IntegerType && node.right.getType() == FloatType -> BooleanType
                node.left.getType() == FloatType && node.right.getType() == IntegerType -> BooleanType
                else -> {
                    ErrorLogger += TypeError(node.ctx, "Right and left hand side of must be either float or integer.")
                    null
                }
            }
        )
    }

    override fun visit(node: GreaterOrEqExpr) {
        super.visit(node)

        node.setType(
            when {
                node.left.getType() == IntegerType && node.right.getType() == IntegerType -> BooleanType
                node.left.getType() == FloatType && node.right.getType() == FloatType -> BooleanType
                node.left.getType() == IntegerType && node.right.getType() == FloatType -> BooleanType
                node.left.getType() == FloatType && node.right.getType() == IntegerType -> BooleanType
                else -> {
                    ErrorLogger += TypeError(node.ctx, "Right and left hand side of must be either float or integer.")
                    null
                }
            }
        )
    }

    override fun visit(node: LessThanExpr) {
        super.visit(node)

        node.setType(
            when {
                node.left.getType() == IntegerType && node.right.getType() == IntegerType -> BooleanType
                node.left.getType() == FloatType && node.right.getType() == FloatType -> BooleanType
                node.left.getType() == IntegerType && node.right.getType() == FloatType -> BooleanType
                node.left.getType() == FloatType && node.right.getType() == IntegerType -> BooleanType
                else -> {
                    ErrorLogger += TypeError(node.ctx, "Right and left hand side of must be either float or integer.")
                    null
                }
            }
        )
    }

    override fun visit(node: LessOrEqExpr) {
        super.visit(node)

        node.setType(
            when {
                node.left.getType() == IntegerType && node.right.getType() == IntegerType -> BooleanType
                node.left.getType() == FloatType && node.right.getType() == FloatType -> BooleanType
                node.left.getType() == IntegerType && node.right.getType() == FloatType -> BooleanType
                node.left.getType() == FloatType && node.right.getType() == IntegerType -> BooleanType
                else -> {
                    ErrorLogger += TypeError(node.ctx, "Right and left hand side of must be either float or integer.")
                    null
                }
            }
        )
    }

    override fun visit(node: AdditionExpr) {
        super.visit(node)

        node.setType(
            when {
                node.left.getType() == IntegerType && node.right.getType() == IntegerType -> IntegerType
                node.left.getType() == FloatType && node.right.getType() == FloatType -> FloatType
                node.left.getType() == IntegerType && node.right.getType() == FloatType -> FloatType
                node.left.getType() == FloatType && node.right.getType() == IntegerType -> FloatType
                else -> {
                    ErrorLogger += TypeError(node.ctx, "Right and left hand side of must be either float or integer.")
                    null
                }
            }
        )
    }

    override fun visit(node: SubtractionExpr) {
        super.visit(node)

        node.setType(
            when {
                node.left.getType() == IntegerType && node.right.getType() == IntegerType -> IntegerType
                node.left.getType() == FloatType && node.right.getType() == FloatType -> FloatType
                node.left.getType() == IntegerType && node.right.getType() == FloatType -> FloatType
                node.left.getType() == FloatType && node.right.getType() == IntegerType -> FloatType
                else -> {
                    ErrorLogger += TypeError(node.ctx, "Right and left hand side of must be either float or integer.")
                    null
                }
            }
        )
    }

    override fun visit(node: MultiplicationExpr) {
        super.visit(node)

        node.setType(
            when {
                node.left.getType() == IntegerType && node.right.getType() == IntegerType -> IntegerType
                node.left.getType() == FloatType && node.right.getType() == FloatType -> FloatType
                node.left.getType() == IntegerType && node.right.getType() == FloatType -> FloatType
                node.left.getType() == FloatType && node.right.getType() == IntegerType -> FloatType
                else -> {
                    ErrorLogger += TypeError(node.ctx, "Right and left hand side of must be either float or integer.")
                    null
                }
            }
        )
    }

    override fun visit(node: DivisionExpr) {
        super.visit(node)

        node.setType(
            when {
                node.left.getType() == IntegerType && node.right.getType() == IntegerType -> IntegerType
                node.left.getType() == FloatType && node.right.getType() == FloatType -> FloatType
                node.left.getType() == IntegerType && node.right.getType() == FloatType -> FloatType
                node.left.getType() == FloatType && node.right.getType() == IntegerType -> FloatType
                else -> {
                    ErrorLogger += TypeError(node.ctx, "Right and left hand side of must be either float or integer.")
                    null
                }
            }
        )
    }

    override fun visit(node: NegationExpr) {
        super.visit(node)

        node.setType(
            when (node.value.getType()) {
                IntegerType -> IntegerType
                FloatType -> FloatType
                else -> {
                    ErrorLogger += TypeError(node.ctx, "Only float or integer can be negative.")
                    null
                }
            }
        )
    }

    override fun visit(node: NotExpr) {
        super.visit(node)

        if (node.value.getType() != BooleanType) {
            ErrorLogger += TypeError(node.ctx, "Can only invert boolean expressions.")
        }

        node.setType(BooleanType)
    }

    override fun visit(node: ArrayLookupExpr) {
        super.visit(node)

        node.setType(node.arr.getType())
    }

    /**
     * Finds the size of the largest subarray in each dimension.
     * Note: if there are empty array literals in the tree, this function may return fewer dimensions than expected.
     */
    private fun searchSize(
        node: ArrayLiteralExpr,
        sizes: MutableList<Int> = mutableListOf(),
        depth: Int = 1
    ): MutableList<Int> {
        if (sizes.size < depth) {
            sizes.add(node.values.size)
        } else if (sizes[depth - 1] < node.values.size) {
            sizes[depth - 1] = node.values.size
        }

        @Suppress("NAME_SHADOWING") var sizes = sizes
        node.values.forEach {
            if (it is ArrayLiteralExpr) {
                sizes = searchSize(it, sizes, depth + 1)
            }
        }

        return sizes
    }

    private fun pushDownSize(n: Expr, size: List<Int>) {
        if (n is ArrayLiteralExpr) {
            n.size = size[0]

            if (n.values.size > 1) {
                n.values.forEach {
                    pushDownSize(it, size.subList(1, size.size))
                }
            }
        }
    }

    override fun visit(node: SizedArrayExpr) {
        super.visit(node)

        /*
         * Scenarios that has to be checked:
         *
         * Has declared type, and has values -> Check value consistency, and check declared type matches values type
         * Has declared type, but no values  -> Used declared type
         */

        if (node.body == null) {
            return
        }

        val valuesType: Type?
        if (node.body.values.isEmpty()) {
            // No values specified, nothing to check against
            valuesType = null
        } else {
            // Check consistency of types
            val types = node.body.values.map { it.getType() }.toList()
            if (types.distinct().count() > 1) {
                // ToDo int to float conversion
                ErrorLogger += TypeError(
                    node.ctx,
                    "Cannot determine type of array because it is initialised with multiple types."
                )
                node.setType(null)
                return
            }
            valuesType = types.first() // Pick any one because we have already checked they are identical
        }

        /**
         * Compares two types, handles nested checking for arrays.
         * Null is equal to any other type.
         *
         * @return Returns true if the type are the same, accounting for wildcard matching
         */
        fun compareType(type1: Type?, type2: Type?): Boolean {
            if (type1 == null || type2 == null) {
                return true
            }

            if (type1 is ArrayType && type2 is ArrayType) {
                return compareType(type1.subtype, type2.subtype)
            }

            return type1 == type2
        }

        // ToDo implicit conversion, declaredType == float, and valuesType == integer -> type = float
        node.setType(
            if (node.body.values.isNotEmpty()) {
                if (node.declaredType is ArrayType && !compareType(node.declaredType.subtype, valuesType)) {
                    ErrorLogger += TypeError(
                        node.ctx,
                        "Cannot determine type of array since initialised values does not match the declared type."
                    )
                    node.setType(null)
                    return
                }
                node.declaredType
            } else {
                node.declaredType
            }
        )

        val literalSizes = searchSize(node.body)

        // compare sizes
        if (literalSizes.size > node.declaredSize.size) {
            throw AssertionError("Type consistency check has failed, it should have already caught this")
        }
        val finalSizes = node.declaredSize.mapIndexed { i, declaredSize ->
            if (declaredSize == null && i >= literalSizes.size) {
                null
            } else if (declaredSize == null || (i < literalSizes.size - 1 && literalSizes[i] > declaredSize)) {
                literalSizes[i]
            } else {
                declaredSize
            }
        }.toList()

        // check that all dimensions has a size
        finalSizes.forEach {
            if (it == null) {
                throw AssertionError("A dimension has undetermined size")
            }
        }

        // push down size
        pushDownSize(
            node.body,
            finalSizes.map { it!! }.toList()
        )
    }

    override fun visit(node: ArrayLiteralExpr) {
        super.visit(node)

        node.setType(ArrayType(if (node.values.isEmpty()) null else node.values[0].getType()))

        val foundSizes = searchSize(node)
        pushDownSize(node, foundSizes)
    }

    override fun visit(node: Identifier) {
        // Get type of name
        node.setType(symbolTableSession.getSymbolType(node.spelling))
    }

    override fun visit(node: ModuloExpr) {
        super.visit(node)

        node.setType(
            when {
                node.left.getType() == IntegerType && node.right.getType() == IntegerType -> IntegerType
                node.left.getType() == FloatType && node.right.getType() == FloatType -> FloatType
                node.left.getType() == IntegerType && node.right.getType() == FloatType -> FloatType
                node.left.getType() == FloatType && node.right.getType() == IntegerType -> FloatType
                else -> {
                    ErrorLogger += TypeError(node.ctx, "Right and left hand side of must be either float or integer.")
                    null
                }
            }
        )
    }

    override fun visit(node: ReturnStmt) {
        super.visit(node)

        if (expectedReturn != node.value.getType()) {
            node.value.setType(
                when {
                    node.value.getType() == IntegerType -> FloatType
                    else -> {
                        ErrorLogger += TypeError(
                            node.ctx,
                            "Wrong return type (${node.value.getType()}). Expected $expectedReturn"
                        )
                        null
                    }
                }
            )
        }
    }

    override fun visit(node: FuncDecl) {
        expectedReturn = node.returnType
        super.visit(node)
    }

    override fun visit(node: FuncCallExpr) {
        super.visit(node)

        // Get function declaration for type-checking
        val funcDecl = (symbolTableSession.getSymbol(node.ident) as FuncDecl)

        // If actual and formal arguments are of equal size, type-check them
        if (node.args.size == funcDecl.args.size) {

            for (i in node.args.indices) {
                // If each argument are not typewise congruent, register error
                if (node.args[i].getType() != funcDecl.args[i].getType()) {

                    val nodeArg = node.args[i]
                    val funcDeclArg = funcDecl.args[i]

                    ErrorLogger += TypeError(
                        node.ctx,
                        "Actual argument \"${nodeArg.ctx.text}\", of type ${nodeArg.getType()}, was not equal to " +
                                "formal argument \"${funcDeclArg.ident}\", of type ${funcDeclArg.getType()}"
                    )
                }
            }
        } else { // If formal and actual arguments differ in size, register error
            ErrorLogger += CompileError(
                node.ctx,
                "Size of actual arguments: ${node.args.size} to function-call \"${node.ident}\" differ from " +
                        "formal arguments' size ${funcDecl.args.size}"
            )
        }

        // Get return type of function
        node.setType(symbolTableSession.getSymbolType(node.ident))
    }

    override fun visit(node: ConstDecl) {
        super.visit(node)

        node.type = node.expr.getType()
    }

    // FuncDecl is handled in ParseTreeValueWalker

    override fun visit(node: AssignStmt) {
        super.visit(node)

        node.setType(node.expr.getType())
    }
}
