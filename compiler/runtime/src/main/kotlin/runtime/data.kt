package dk.aau.cs.d409f18.cellumata.codegen.kotlin.runtime

data class StateColor(
    val red: Int,
    val blue: Int,
    val green: Int
)

data class WorldConfiguration(
    val dims: List<Int>,
    val colors: List<StateColor>,
    val cellSize: Int,
    val tickrate: Long
)