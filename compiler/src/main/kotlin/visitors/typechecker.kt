package dk.aau.cs.d409f19.cellumata.visitors

import dk.aau.cs.d409f19.cellumata.ErrorFromContext
import dk.aau.cs.d409f19.cellumata.ErrorLogger
import dk.aau.cs.d409f19.cellumata.ast.*
import org.antlr.v4.runtime.ParserRuleContext
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

    override fun visit(node: OrExpr) {
        super.visit(node)

        if (node.left.getType() != BooleanType) {
            ErrorLogger.registerError(TypeError(node.left.ctx, "Expected boolean expression in left hand side of or-expression."))
        }
        if (node.right.getType() != BooleanType) {
            ErrorLogger.registerError(TypeError(node.right.ctx, "Expected boolean expression in right hand side of or-expression."))
        }

        node.setType(BooleanType)
    }

    override fun visit(node: AndExpr) {
        super.visit(node)

        if (node.left.getType() != BooleanType) {
            ErrorLogger.registerError(TypeError(node.left.ctx, "Expected boolean expression in left hand side of and-expression."))
        }
        if (node.right.getType() != BooleanType) {
            ErrorLogger.registerError(TypeError(node.right.ctx, "Expected boolean expression in right hand side of and-expression."))
        }

        node.setType(BooleanType)
    }

    override fun visit(node: InequalityExpr) {
        super.visit(node)

        node.setType(when {
            node.left.getType() == BooleanType && node.right.getType() == BooleanType -> BooleanType
            node.left.getType() == IntegerType && node.right.getType() == IntegerType -> BooleanType
            node.left.getType() == FloatType && node.right.getType() == FloatType -> BooleanType
            node.left.getType() == IntegerType && node.right.getType() == FloatType -> BooleanType
            node.left.getType() == FloatType && node.right.getType() == IntegerType -> BooleanType
            node.left.getType() == StateType && node.right.getType() == StateType -> BooleanType
            node.left.getType() == ActualNeighbourhoodType && node.right.getType() == ActualNeighbourhoodType -> BooleanType
            node.left.getType() == ActualNeighbourhoodType && node.right.getType() is ArrayType && (node.right.getType() as ArrayType).subtype == StateType -> BooleanType
            node.left.getType() is ArrayType && (node.left.getType() as ArrayType).subtype == StateType && node.right.getType() == ActualNeighbourhoodType -> BooleanType
            node.left.getType() is ArrayType && node.right.getType() is ArrayType -> BooleanType
            else -> {
                ErrorLogger.registerError(TypeError(node.ctx, "Could not compare the types of right and left hand side of inequality-expression."))
                null
            }
        })
    }

    override fun visit(node: EqualityExpr) {
        super.visit(node)

        node.setType(when {
            node.left.getType() == BooleanType && node.right.getType() == BooleanType -> BooleanType
            node.left.getType() == IntegerType && node.right.getType() == IntegerType -> BooleanType
            node.left.getType() == FloatType && node.right.getType() == FloatType -> BooleanType
            node.left.getType() == IntegerType && node.right.getType() == FloatType -> BooleanType
            node.left.getType() == FloatType && node.right.getType() == IntegerType -> BooleanType
            node.left.getType() == StateType && node.right.getType() == StateType -> BooleanType
            node.left.getType() == ActualNeighbourhoodType && node.right.getType() == ActualNeighbourhoodType -> BooleanType
            node.left.getType() == ActualNeighbourhoodType && node.right.getType() is ArrayType && (node.right.getType() as ArrayType).subtype == StateType -> BooleanType
            node.left.getType() is ArrayType && (node.left.getType() as ArrayType).subtype == StateType && node.right.getType() == ActualNeighbourhoodType -> BooleanType
            node.left.getType() is ArrayType && node.right.getType() is ArrayType -> BooleanType
            else -> {
                ErrorLogger.registerError(TypeError(node.ctx, "Could not compare the types of right and left hand side of equality-expression."))
                null
            }
        })
    }

    override fun visit(node: GreaterThanExpr) {
        super.visit(node)

        node.setType(when {
            node.left.getType() == IntegerType && node.right.getType() == IntegerType -> BooleanType
            node.left.getType() == FloatType && node.right.getType() == FloatType -> BooleanType
            node.left.getType() == IntegerType && node.right.getType() == FloatType -> BooleanType
            node.left.getType() == FloatType && node.right.getType() == IntegerType -> BooleanType
            else -> {
                ErrorLogger.registerError(TypeError(node.ctx, "Right and left hand side of must be either float or integer."))
                null
            }
        })
    }

    override fun visit(node: GreaterOrEqExpr) {
        super.visit(node)

        node.setType(when {
            node.left.getType() == IntegerType && node.right.getType() == IntegerType -> BooleanType
            node.left.getType() == FloatType && node.right.getType() == FloatType -> BooleanType
            node.left.getType() == IntegerType && node.right.getType() == FloatType -> BooleanType
            node.left.getType() == FloatType && node.right.getType() == IntegerType -> BooleanType
            else -> {
                ErrorLogger.registerError(TypeError(node.ctx, "Right and left hand side of must be either float or integer."))
                null
            }
        })
    }

    override fun visit(node: LessThanExpr) {
        super.visit(node)

        node.setType(when {
            node.left.getType() == IntegerType && node.right.getType() == IntegerType -> BooleanType
            node.left.getType() == FloatType && node.right.getType() == FloatType -> BooleanType
            node.left.getType() == IntegerType && node.right.getType() == FloatType -> BooleanType
            node.left.getType() == FloatType && node.right.getType() == IntegerType -> BooleanType
            else -> {
                ErrorLogger.registerError(TypeError(node.ctx, "Right and left hand side of must be either float or integer."))
                null
            }
        })
    }

    override fun visit(node: LessOrEqExpr) {
        super.visit(node)

        node.setType(when {
            node.left.getType() == IntegerType && node.right.getType() == IntegerType -> BooleanType
            node.left.getType() == FloatType && node.right.getType() == FloatType -> BooleanType
            node.left.getType() == IntegerType && node.right.getType() == FloatType -> BooleanType
            node.left.getType() == FloatType && node.right.getType() == IntegerType -> BooleanType
            else -> {
                ErrorLogger.registerError(TypeError(node.ctx, "Right and left hand side of must be either float or integer."))
                null
            }
        })
    }

    override fun visit(node: AdditionExpr) {
        super.visit(node)

        node.setType(when {
            node.left.getType() == IntegerType && node.right.getType() == IntegerType -> IntegerType
            node.left.getType() == FloatType && node.right.getType() == FloatType -> FloatType
            node.left.getType() == IntegerType && node.right.getType() == FloatType -> FloatType
            node.left.getType() == FloatType && node.right.getType() == IntegerType -> FloatType
            else -> {
                ErrorLogger.registerError(TypeError(node.ctx, "Right and left hand side of must be either float or integer."))
                null
            }
        })
    }

    override fun visit(node: SubtractionExpr) {
        super.visit(node)

        node.setType(when {
            node.left.getType() == IntegerType && node.right.getType() == IntegerType -> IntegerType
            node.left.getType() == FloatType && node.right.getType() == FloatType -> FloatType
            node.left.getType() == IntegerType && node.right.getType() == FloatType -> FloatType
            node.left.getType() == FloatType && node.right.getType() == IntegerType -> FloatType
            else -> {
                ErrorLogger.registerError(TypeError(node.ctx, "Right and left hand side of must be either float or integer."))
                null
            }
        })
    }

    override fun visit(node: MultiplicationExpr) {
        super.visit(node)

        node.setType(when {
            node.left.getType() == IntegerType && node.right.getType() == IntegerType -> IntegerType
            node.left.getType() == FloatType && node.right.getType() == FloatType -> FloatType
            node.left.getType() == IntegerType && node.right.getType() == FloatType -> FloatType
            node.left.getType() == FloatType && node.right.getType() == IntegerType -> FloatType
            else -> {
                ErrorLogger.registerError(TypeError(node.ctx, "Right and left hand side of must be either float or integer."))
                null
            }
        })
    }

    override fun visit(node: DivisionExpr) {
        super.visit(node)

        node.setType(when {
            node.left.getType() == IntegerType && node.right.getType() == IntegerType -> IntegerType
            node.left.getType() == FloatType && node.right.getType() == FloatType -> FloatType
            node.left.getType() == IntegerType && node.right.getType() == FloatType -> FloatType
            node.left.getType() == FloatType && node.right.getType() == IntegerType -> FloatType
            else -> {
                ErrorLogger.registerError(TypeError(node.ctx, "Right and left hand side of must be either float or integer."))
                null
            }
        })
    }

    override fun visit(node: NegationExpr) {
        super.visit(node)

        node.setType(when(node.value.getType()) {
            IntegerType -> IntegerType
            FloatType -> FloatType
            else -> {
                ErrorLogger.registerError(TypeError(node.ctx, "Only float or integer can be negative."))
                null
            }
        })
    }

    override fun visit(node: NotExpr) {
        super.visit(node)

        if (node.value.getType() != BooleanType) {
            ErrorLogger.registerError(TypeError(node.ctx, "Can only invert boolean expressions."))
        }

        node.setType(BooleanType)
    }

    override fun visit(node: ArrayLookupExpr) {
        super.visit(node)

        node.setType(node.arr.getType())
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

        val valuesType: Type?
        if (node.values.isEmpty()) {
            // No values specified, nothing to check against
            valuesType = null
        } else {
            // Check consistency of types
            val types = node.values.map { it.getType() }.toList()
            if (types.distinct().count() > 1) {
                // ToDo int to float conversion
                ErrorLogger.registerError(TypeError(node.ctx, "Cannot determine type of array because it is initialised with multiple types."))
                node.setType(null)
                return
            }
            valuesType = types.first() // Pick any one because we have already checked they are identical
        }

        // ToDo implicit conversion, declaredType == float, and valuesType == integer -> type = float
        node.setType(when {
            node.declaredType != null && node.values.isNotEmpty() -> {
                if (node.declaredType != valuesType) {
                    ErrorLogger.registerError(TypeError(node.ctx, "Cannot determine type of array since initialised values does not match the declared type."))
                    node.setType(null)
                    return
                }
                node.declaredType
            }
            node.declaredType != null && node.values.isEmpty() -> node.declaredType
            node.declaredType == null && node.values.isNotEmpty() -> valuesType
            node.declaredType == null && node.values.isEmpty() -> {
                ErrorLogger.registerError(TypeError(node.ctx, "Cannot determine type of array."))
                node.setType(null)
                return
            }
            else -> throw AssertionError("Cannot determine type of array. This case should never be hit!")
        })
    }

    override fun visit(node: Identifier) {
        // Get type of name
        node.setType(symbolTableSession.getSymbolType(node.spelling))
    }

    override fun visit(node: ModuloExpr) {
        super.visit(node)

        node.setType(when {
            node.left.getType() == IntegerType && node.right.getType() == IntegerType -> IntegerType
            node.left.getType() == FloatType && node.right.getType() == FloatType -> FloatType
            node.left.getType() == IntegerType && node.right.getType() == FloatType -> FloatType
            node.left.getType() == FloatType && node.right.getType() == IntegerType -> FloatType
            else -> {
                ErrorLogger.registerError(TypeError(node.ctx, "Right and left hand side of must be either float or integer."))
                null
            }
        })
    }

    override fun visit(node: ReturnStmt) {
        super.visit(node)

        if (expectedReturn != node.value.getType()) {
            node.value.setType(when {
                node.value.getType() == IntegerType -> FloatType
                else -> {
                    ErrorLogger.registerError(TypeError(node.ctx, "Wrong return type (${node.value.getType()}). Expected $expectedReturn"))
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
