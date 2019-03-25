package dk.aau.cs.d409f19.cellumata.walkers

import dk.aau.cs.d409f19.cellumata.ast.*
import kotlin.AssertionError

/**
 * Thrown when a violation of the type rules is found
 */
class TypeError : Exception()

/**
 * Move types up the abstract syntax tree according to the type rules, and check that there is no violation of the type rules
 */
class TypeChecker(symbolTable: Table) : ScopedASTVisitor(symbolTable = symbolTable) {
    override fun visit(node: OrExpr) {
        super.visit(node)

        if (node.left.getType() != BooleanType) {
            throw TypeError()
        }
        if (node.right.getType() != BooleanType) {
            throw TypeError()
        }

        node.setType(BooleanType)
    }

    override fun visit(node: AndExpr) {
        super.visit(node)

        if (node.left.getType() != BooleanType) {
            throw TypeError()
        }
        if (node.right.getType() != BooleanType) {
            throw TypeError()
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
            else -> throw TypeError()
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
            else -> throw TypeError()
        })
    }

    override fun visit(node: MoreThanExpr) {
        super.visit(node)

        node.setType(when {
            node.left.getType() == IntegerType && node.right.getType() == IntegerType -> BooleanType
            node.left.getType() == FloatType && node.right.getType() == FloatType -> BooleanType
            node.left.getType() == IntegerType && node.right.getType() == FloatType -> BooleanType
            node.left.getType() == FloatType && node.right.getType() == IntegerType -> BooleanType
            else -> throw TypeError()
        })
    }

    override fun visit(node: MoreEqExpr) {
        super.visit(node)

        node.setType(when {
            node.left.getType() == IntegerType && node.right.getType() == IntegerType -> BooleanType
            node.left.getType() == FloatType && node.right.getType() == FloatType -> BooleanType
            node.left.getType() == IntegerType && node.right.getType() == FloatType -> BooleanType
            node.left.getType() == FloatType && node.right.getType() == IntegerType -> BooleanType
            else -> throw TypeError()
        })
    }

    override fun visit(node: LessThanExpr) {
        super.visit(node)

        node.setType(when {
            node.left.getType() == IntegerType && node.right.getType() == IntegerType -> BooleanType
            node.left.getType() == FloatType && node.right.getType() == FloatType -> BooleanType
            node.left.getType() == IntegerType && node.right.getType() == FloatType -> BooleanType
            node.left.getType() == FloatType && node.right.getType() == IntegerType -> BooleanType
            else -> throw TypeError()
        })
    }

    override fun visit(node: LessEqExpr) {
        super.visit(node)

        node.setType(when {
            node.left.getType() == IntegerType && node.right.getType() == IntegerType -> BooleanType
            node.left.getType() == FloatType && node.right.getType() == FloatType -> BooleanType
            node.left.getType() == IntegerType && node.right.getType() == FloatType -> BooleanType
            node.left.getType() == FloatType && node.right.getType() == IntegerType -> BooleanType
            else -> throw TypeError()
        })
    }

    override fun visit(node: AdditionExpr) {
        super.visit(node)

        node.setType(when {
            node.left.getType() == IntegerType && node.right.getType() == IntegerType -> IntegerType
            node.left.getType() == FloatType && node.right.getType() == FloatType -> FloatType
            node.left.getType() == IntegerType && node.right.getType() == FloatType -> FloatType
            node.left.getType() == FloatType && node.right.getType() == IntegerType -> FloatType
            else -> throw TypeError()
        })
    }

    override fun visit(node: SubtractionExpr) {
        super.visit(node)

        node.setType(when {
            node.left.getType() == IntegerType && node.right.getType() == IntegerType -> IntegerType
            node.left.getType() == FloatType && node.right.getType() == FloatType -> FloatType
            node.left.getType() == IntegerType && node.right.getType() == FloatType -> FloatType
            node.left.getType() == FloatType && node.right.getType() == IntegerType -> FloatType
            else -> throw TypeError()
        })
    }

    override fun visit(node: MultiplicationExpr) {
        super.visit(node)

        node.setType(when {
            node.left.getType() == IntegerType && node.right.getType() == IntegerType -> IntegerType
            node.left.getType() == FloatType && node.right.getType() == FloatType -> FloatType
            node.left.getType() == IntegerType && node.right.getType() == FloatType -> FloatType
            node.left.getType() == FloatType && node.right.getType() == IntegerType -> FloatType
            else -> throw TypeError()
        })
    }

    override fun visit(node: DivisionExpr) {
        super.visit(node)

        node.setType(when {
            node.left.getType() == IntegerType && node.right.getType() == IntegerType -> IntegerType
            node.left.getType() == FloatType && node.right.getType() == FloatType -> FloatType
            node.left.getType() == IntegerType && node.right.getType() == FloatType -> FloatType
            node.left.getType() == FloatType && node.right.getType() == IntegerType -> FloatType
            else -> throw TypeError()
        })
    }

    override fun visit(node: PreIncExpr) {
        super.visit(node)

        node.setType(when(node.value.getType()) {
            IntegerType -> IntegerType
            FloatType -> FloatType
            else -> throw TypeError()
        })
    }

    override fun visit(node: PreDecExpr) {
        super.visit(node)

        node.setType(when(node.value.getType()) {
            IntegerType -> IntegerType
            FloatType -> FloatType
            else -> throw TypeError()
        })
    }

    override fun visit(node: PostIncExpr) {
        super.visit(node)

        node.setType(when(node.value.getType()) {
            IntegerType -> IntegerType
            FloatType -> FloatType
            else -> throw TypeError()
        })
    }

    override fun visit(node: PostDecExpr) {
        super.visit(node)

        node.setType(when(node.value.getType()) {
            IntegerType -> IntegerType
            FloatType -> FloatType
            else -> throw TypeError()
        })
    }

    override fun visit(node: PositiveExpr) {
        super.visit(node)

        node.setType(when(node.value.getType()) {
            IntegerType -> IntegerType
            FloatType -> FloatType
            else -> throw TypeError()
        })
    }

    override fun visit(node: NegativeExpr) {
        super.visit(node)

        node.setType(when(node.value.getType()) {
            IntegerType -> IntegerType
            FloatType -> FloatType
            else -> throw TypeError()
        })
    }

    override fun visit(node: InverseExpr) {
        super.visit(node)

        if (node.value.getType() != BooleanType) {
            throw TypeError()
        }

        node.setType(BooleanType)
    }

    override fun visit(node: ArrayLookupExpr) {
        super.visit(node)

        node.setType(symbolTableSession.getSymbolType(node.ident))
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
                throw TypeError()
            }
            valuesType = types.first() // Pick any one because we have already checked they are identical
        }

        // ToDo implicit conversion, declaredType == float, and valuesType == integer -> type = float
        node.setType(when {
            node.declaredType != null && node.values.isNotEmpty() -> {
                if (node.declaredType != valuesType) {
                    throw TypeError()
                }
                node.declaredType
            }
            node.declaredType != null && node.values.isEmpty() -> node.declaredType
            node.declaredType == null && node.values.isNotEmpty() -> valuesType
            node.declaredType == null && node.values.isEmpty() -> throw TypeError()
            else -> throw AssertionError("This case should never be hit")
        })
    }

    override fun visit(node: ParenExpr) {
        super.visit(node)

        node.setType(node.expr.getType())
    }

    override fun visit(node: NamedExpr) {
        // Get type of name
        node.setType(symbolTableSession.getSymbolType(node.ident))
    }

    override fun visit(node: ModuloExpr) {
        super.visit(node)

        node.setType(when {
            node.left.getType() == IntegerType && node.right.getType() == IntegerType -> IntegerType
            node.left.getType() == FloatType && node.right.getType() == FloatType -> FloatType
            node.left.getType() == IntegerType && node.right.getType() == FloatType -> FloatType
            node.left.getType() == FloatType && node.right.getType() == IntegerType -> FloatType
            else -> throw TypeError()
        })
    }

    override fun visit(node: FuncExpr) {
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