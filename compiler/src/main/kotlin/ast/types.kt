package dk.aau.cs.d409f19.cellumata.ast

sealed class Type

object IntegerType : Type()
object FloatType : Type()
object BooleanType : Type()
object StateType : Type()
object ActualNeighbourhoodType : Type() // An evaluated neighbourhood
data class ArrayType(val subtype: Type)
