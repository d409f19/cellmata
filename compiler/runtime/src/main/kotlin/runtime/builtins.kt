package dk.aau.cs.d409f18.cellumata.codegen.kotlin.runtime

import kotlin.random.Random

fun `builtin count`(worldView: IWorldView, state: Int, nei: List<Int>): Int {
    return nei.count { it == state }
}

fun `builtin randi`(worldView: IWorldView, min: Int, max: Int): Int {
    return Random.nextInt(min, max)
}

fun `builtin randf`(worldView: IWorldView, min: Float, max: Float): Float {
    return Random.nextFloat() * (max - min) + min
}

fun `builtin absi`(worldView: IWorldView, value: Int): Int {
    return Math.abs(value)
}

fun `builtin absf`(worldView: IWorldView, value: Float): Float {
    return Math.abs(value)
}

fun `builtin floor`(worldView: IWorldView, value: Float): Int {
    return Math.floor(value.toDouble()).toInt()
}

fun `builtin ceil`(worldView: IWorldView, value: Float): Int {
    return Math.ceil(value.toDouble()).toInt()
}

fun `builtin root`(worldView: IWorldView, value: Float, root: Float): Float {
    return Math.pow(value.toDouble(), 1.0 / root).toFloat()
}

fun `builtin pow`(worldView: IWorldView, value: Float, exp: Float): Float {
    return Math.pow(value.toDouble(), exp.toDouble()).toFloat()
}
