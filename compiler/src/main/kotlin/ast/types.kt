package dk.aau.cs.d409f19.cellumata.ast

sealed class Type

object IntegerType : Type()
object FloatType : Type()
object BooleanType : Type()
object StateType : Type()
object ActualNeighbourhoodType : Type() // An evaluated neighbourhood
data class ArrayType(val subtype: Type) : Type()

object UncheckedType : Type() // Type of expressions until type checking is done

fun typeFromString(str: String) {
    when (str) {
        "int" -> IntegerType
        "float" -> FloatType
        "bool" -> BooleanType
        "state" -> StateType
        else -> error("Type not recognised.") // TODO Replace with smarter error reporting
    }
}

fun wrapInImplicitConversion(expr: Expr, targetType: Type) : Expr {
    // There are two types of implicit type conversions:
    // int -> float
    // array<state> -> actual neighbourhood

    val type = expr.type
    return when {
        type is IntegerType && targetType is FloatType -> IntToFloatConversion(expr)
        type is ArrayType && type.subtype is StateType && targetType is ActualNeighbourhoodType -> StateArrayToActualNeighbourhoodConversion(expr)
        else -> error("Cannot implicitly convert $type to $targetType.") // TODO Replace with smarter error reporting
    }
}