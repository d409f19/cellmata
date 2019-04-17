package dk.aau.cs.d409f19.cellumata.visitors

import dk.aau.cs.d409f19.cellumata.ErrorFromContext
import dk.aau.cs.d409f19.cellumata.ErrorLogger
import dk.aau.cs.d409f19.cellumata.ast.*
import kotlin.AssertionError

/**
 * Error for violation of the type rules
 */
class TypeError(ctx: SourceContext, description: String) : ErrorFromContext(ctx, description)

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
                ErrorLogger.registerError(TypeError(node.ctx, "Only float or integer expressions can be negative."))
                UndeterminedType
            }
        })
    }

    override fun visit(node: NotExpr) {
        super.visit(node)

        if (node.value.getType() != BooleanType) {
            ErrorLogger.registerError(TypeError(node.ctx, "Can only NOT boolean expressions."))
        }

        node.setType(BooleanType)
    }

    override fun visit(node: ArrayLookupExpr) {
        super.visit(node)

        val arrayType = node.arr.getType()

        if (arrayType is ArrayType) {
            node.setType(arrayType.subtype)
        } else {
            node.setType(UndeterminedType)
        }
    }

    override fun visit(node: ArrayBodyExpr) {
        super.visit(node)

        /*
        * Scenarios that has to be checked:
        *
        * Has declared type, and has values -> Check value consistency, and check declared type matches values type
        * Has declared type, but no values  -> Used declared type
        * No declared type, and has values  -> Check value consistency
        * No declared type, and no values   -> Impossible
        * */

        val valuesType: Type
        if (node.values.isEmpty()) {
            // No values specified, nothing to check against
            valuesType = UndeterminedType
        } else {
            // Check consistency of types
            val types = node.values.map { it.getType() }.toList()
            if (types.distinct().count() > 1) {
                // ToDo int to float conversion
                ErrorLogger.registerError(TypeError(node.ctx, "Cannot determine type of array because it is initialised with multiple types."))
                node.setType(UndeterminedType)
                return
            }
            valuesType = types.first() // Pick any one because we have already checked they are identical
        }

        // ToDo implicit conversion, declaredType == float, and valuesType == integer -> type = float
        node.setType(when {
            node.values.isNotEmpty() -> {
                if (node.declaredType != valuesType || valuesType != UndeterminedType) {
                    ErrorLogger.registerError(TypeError(node.ctx, "Cannot determine type of array since initialised values does not match the declared type."))
                    node.setType(UndeterminedType)
                    return
                }
                node.declaredType
            }
            node.values.isEmpty() -> node.declaredType
            node.values.isNotEmpty() -> ArrayType(valuesType)
            node.values.isEmpty() -> {
                ErrorLogger.registerError(TypeError(node.ctx, "Cannot determine type of array."))
                UndeterminedType
            }
            else -> throw AssertionError("Cannot determine type of array. This case should never be hit!")
        })
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
                ErrorLogger.registerError(TypeError(node.ctx, "Right and left hand side of must be either float or integer."))
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

            ErrorLogger.registerError(TypeError(node.ctx, "Right and left hand side of must be boolean."))
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
            ErrorLogger.registerError(TypeError(node.ctx, "Right and left hand side of must be either float or integer."))
        }

        node.setType(BooleanType)
    }

    override fun visit(node: ReturnStmt) {
        super.visit(node)

        expectedReturn!!

        val exprType = node.expr.getType()
        if (expectedReturn != node.expr.getType()) {
            // Maybe we can do implicit conversion?
            if (expectedReturn == FloatType && exprType == IntegerType) {
                node.expr = IntToFloatConversion(node.expr)
            } else if ((expectedReturn == ArrayType(StateType) && exprType == LocalNeighbourhoodType)) {
                node.expr = StateArrayToLocalNeighbourhoodConversion(node.expr)
            } else {
                ErrorLogger.registerError(TypeError(node.ctx, "Wrong return type (${node.expr.getType()}). Expected $expectedReturn"))
            }
        }
    }

    override fun visit(node: FuncDecl) {
        expectedReturn = node.returnType
        super.visit(node)
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
