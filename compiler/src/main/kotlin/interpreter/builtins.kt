package dk.aau.cs.d409f19.cellumata.interpreter

import dk.aau.cs.d409f19.cellumata.ast.*
import kotlin.random.Random

fun callBuiltinFunction(funcDecl: BuiltinFunc, arguments: List<Any>): Any {
    return when (funcDecl) {
        BuiltinFuncCount -> builtInCount(arguments[0] as StateValue, arguments[1] as List<StateValue>)
        BuiltinFuncRandi -> builtInRandi(arguments[0] as Int, arguments[1] as Int)
        BuiltinFuncRandf -> builtInRandf(arguments[0] as Float, arguments[1] as Float)
        BuiltinFuncAbsi -> builtInAbsi(arguments[0] as Int)
        BuiltinFuncAbsf -> builtInAbsf(arguments[0] as Float)
        BuiltinFuncFloor -> builtInFloor(arguments[0] as Float)
        BuiltinFuncCeil -> builtInCeil(arguments[0] as Float)
        BuiltinFuncRoot -> builtInRoot(arguments[0] as Float, arguments[1] as Float)
        BuiltinFuncPow -> builtInPow(arguments[0] as Float, arguments[1] as Float)
        else -> throw InterpretRuntimeException("No built-in implementation of '${funcDecl.javaClass}'.")
    }
}

fun builtInCount(state: StateValue, nei: List<StateValue>): Int {
    return nei.count { it == state }
}

fun builtInRandi(min: Int, max: Int): Int {
    return Random.nextInt(min, max)
}

fun builtInRandf(min: Float, max: Float): Float {
    return Random.nextFloat() * (max - min) + min
}

fun builtInAbsi(value: Int): Int {
    return Math.abs(value)
}

fun builtInAbsf(value: Float): Float {
    return Math.abs(value)
}

fun builtInFloor(value: Float): Int {
    return Math.floor(value.toDouble()).toInt()
}

fun builtInCeil(value: Float): Int {
    return Math.ceil(value.toDouble()).toInt()
}

fun builtInRoot(value: Float, root: Float): Float {
    return Math.pow(value.toDouble(), 1.0 / root).toFloat()
}

fun builtInPow(value: Float, exp: Float): Float {
    return Math.pow(value.toDouble(), exp.toDouble()).toFloat()
}