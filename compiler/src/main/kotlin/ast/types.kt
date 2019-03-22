package dk.aau.cs.d409f19.cellumata.ast

import dk.aau.cs.d409f19.antlr.CellmataParser
import org.antlr.v4.runtime.tree.ParseTree

/**
 * Base class for all type classes. This class should be viewed as effectively being an enum.
 */
sealed class Type

object IntegerType : Type()
object FloatType : Type()
object BooleanType : Type()
object StateType : Type()
object ActualNeighbourhoodType : Type() // An evaluated neighbourhood
data class ArrayType(val subtype: Type) : Type()

/**
 * Default value for types before they're checked by the type checker
 */
object UncheckedType : Type()

/**
 * Convert a parse tree node to the type it represents
 */
fun typeFromCtx(ctx: ParseTree): Type {
    return when(ctx) {
        is CellmataParser.TypeArrayContext -> ArrayType(subtype = typeFromCtx(ctx.array_decl().type_ident()))
        is CellmataParser.TypeBooleanContext -> BooleanType
        is CellmataParser.TypeIntegerContext -> IntegerType
        is CellmataParser.TypeFloatContext -> FloatType
        is CellmataParser.TypeNeighbourContext -> ActualNeighbourhoodType
        is CellmataParser.TypeStateContext -> StateType
        else -> error("Unknown type")
    }
}

fun wrapInImplicitConversion(expr: Expr, targetType: Type) : Expr {
    // There are two types of implicit type conversions:
    // int -> float
    // array<state> -> actual neighbourhood

    val type = expr.getType()
    return when {
        type is IntegerType && targetType is FloatType -> IntToFloatConversion(expr)
        type is ArrayType && type.subtype is StateType && targetType is ActualNeighbourhoodType -> StateArrayToActualNeighbourhoodConversion(expr)
        else -> error("Cannot implicitly convert $type to $targetType.") // TODO Replace with smarter error reporting
    }
}