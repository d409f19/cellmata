package dk.aau.cs.d409f19.cellumata.visitors

import dk.aau.cs.d409f19.cellumata.CompileError
import dk.aau.cs.d409f19.cellumata.ErrorLogger
import dk.aau.cs.d409f19.cellumata.ast.*
import kotlin.AssertionError

/**
 * Error for violation of the type rules
 */
class TypeError(ctx: SourceContext, description: String) : CompileError(ctx, description)

/**
 * Synthesizes types by moving them up the abstract syntax tree according to the type rules, and check that there is no violation of the type rules
 */
class TypeChecker(symbolTable: Table) : ScopedASTVisitor(symbolTable = symbolTable) {
    private var expectedReturn: Type? = null

    override fun visit(node: NegationExpr) {
        super.visit(node)

        node.setType(when(node.value.getType()) {
            IntegerType -> IntegerType
            FloatType -> FloatType
            else -> {
                ErrorLogger += TypeError(node.ctx, "Only float or integer can be negative.")
                UndeterminedType
            }
        })
    }

    override fun visit(node: NotExpr) {
        super.visit(node)

        if (node.value.getType() != BooleanType) {
            ErrorLogger += TypeError(node.ctx, "Can only NOT boolean expressions.")
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
     * Finds the size of the largest subarray in each dimension.
     * Note: if there are empty array literals in the tree, this function may return fewer dimensions than expected.
     */
    private fun searchSize(node: ArrayLiteralExpr, sizes: MutableList<Int> = mutableListOf(), depth: Int = 1): MutableList<Int> {
        if (sizes.size < depth) {
            sizes.add(node.values.size)
        } else  if (sizes[depth-1] < node.values.size) {
            sizes[depth-1] = node.values.size
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
         * SizedArrayExpr always have a declared type, so these scenarios has to be checked:
         *
         * Has no values -> Used declared type
         * Has values -> Check value consistency, and check declared type matches values type
         */

        if (node.body == null || node.body.values.isEmpty()) {
            // No values specified, nothing to check against
            return

        } else {
            // Check consistency of types
            val types = node.body.values.map { it.getType() }.toList()
            if (types.distinct().count() > 1) {
                // ToDo int to float conversion
                ErrorLogger += TypeError(node.ctx, "Cannot determine type of array because it is initialised with multiple types.")
                node.setType(UndeterminedType)
                return
            }
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
        node.setType(if (node.body.values.isNotEmpty()) {
                if (node.declaredType is ArrayType && !compareType(node.declaredType.subtype, node.body.getType())) {
                    ErrorLogger += TypeError(node.ctx, "Cannot determine type of array since initialised values does not match the declared type.")
                    node.setType(UndeterminedType)
                    return
                }
                node.declaredType
            } else {
                node.declaredType
            })

        val literalSizes = searchSize(node.body)

        // compare sizes
        if (literalSizes.size > node.declaredSize.size) {
            throw AssertionError("Type consistency check has failed, it should have already caught this")
        }
        val finalSizes = node.declaredSize.mapIndexed { i, declaredSize ->
            if (declaredSize == null && i >= literalSizes.size) {
                null
            } else if (declaredSize == null || (i < literalSizes.size-1 && literalSizes[i] > declaredSize)) {
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
            finalSizes.map {it!!}.toList()
        )
    }

    override fun visit(node: ArrayLiteralExpr) {
        super.visit(node)

        node.setType(ArrayType(if (node.values.isEmpty()) UndeterminedType else node.values[0].getType()))

        val foundSizes = searchSize(node)
        pushDownSize(node, foundSizes)
    }

    override fun visit(node: Identifier) {
        // Get type of name
        node.setType(symbolTableSession.getSymbolType(node.spelling)!!)
    }

    override fun visit(node: BinaryArithmeticExpr) {
        super.visit(node)

        val lt = node.left.getType()
        val rt = node.right.getType()

        node.setType(when {
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
        })
    }

    override fun visit(node: BinaryBooleanExpr) {
        super.visit(node)

        val lt = node.left.getType()
        val rt = node.right.getType()

        if (lt != UndeterminedType && rt != UndeterminedType
                && lt != BooleanType && rt != BooleanType) {

            ErrorLogger += TypeError(node.ctx, "Right and left hand side of must be boolean.")
        }

        node.setType(BooleanType)
    }

    override fun visit(node: NumericComparisonExpr) {
        super.visit(node)

        val lt = node.left.getType()
        val rt = node.right.getType()

        if (lt == UndeterminedType || rt == UndeterminedType) {
            // Could not determine type of a child node, so we can't conclude more type errors
        }
        else if ((lt == IntegerType || lt == FloatType) && lt == rt) {
            // Both are ints, or both are floats. Perfect!
        }
        else if (lt == IntegerType && rt == FloatType) {
            // int -> float conversion for left child
            node.left = IntToFloatConversion(node.left)
        }
        else if (lt == FloatType && rt == IntegerType) {
            // int -> float conversion for right child
            node.right = IntToFloatConversion(node.right)
        }
        else {
            // Something is wrong, raise an error
            ErrorLogger += TypeError(node.ctx, "Right and left hand side of must be either float or integer.")
        }

        node.setType(BooleanType)
    }

    override fun visit(node: ReturnStmt) {
        super.visit(node)

        expectedReturn!!

        val exprType = node.expr.getType()
        if (expectedReturn != exprType) {
            // Maybe we can do implicit conversion?
            if (expectedReturn == FloatType && exprType == IntegerType) {
                node.expr = IntToFloatConversion(node.expr)
            } else if ((expectedReturn == ArrayType(StateType) && exprType == LocalNeighbourhoodType)) {
                node.expr = StateArrayToLocalNeighbourhoodConversion(node.expr)
            } else {
                ErrorLogger += TypeError(node.ctx, "Wrong return type (${node.expr.getType()}). Expected $expectedReturn")
            }
        }
    }

    override fun visit(node: FuncDecl) {
        expectedReturn = node.returnType
        super.visit(node)
        expectedReturn = null
    }

    override fun visit(node: FuncCallExpr) {
        super.visit(node)

        // Get return type of function
        node.setType(symbolTableSession.getSymbolType(node.ident)!!)
    }

    override fun visit(node: ConstDecl) {
        super.visit(node)
        node.type = node.expr.getType()
    }

    override fun visit(node: AssignStmt) {
        super.visit(node)
        node.setType(node.expr.getType())
    }
}
