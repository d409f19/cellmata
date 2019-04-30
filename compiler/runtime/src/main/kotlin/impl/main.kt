package dk.aau.cs.d409f18.cellumata.codegen.kotlin.impl

import dk.aau.cs.d409f18.cellumata.codegen.kotlin.runtime.*

class ProgramImpl: IProgram {
    override fun updateCell(worldView: IWorldView): Int {
        return when(worldView.getCell(0, 0)) {
            0 -> state_0(worldView)
            1 -> state_1(worldView)
            2 -> state_2(worldView)
            else -> throw Error()
        }
    }
    fun state_0(worldView: IWorldView): Int {
        return (1);
        };
    fun state_1(worldView: IWorldView): Int {
        return (2);
        };
    fun state_2(worldView: IWorldView): Int {
        return (0);
        };
}

fun main() {
    GraphicalDriver(WorldConfiguration(listOf(5, 5), listOf(StateColor(255, 0, 0), StateColor(0, 255, 0), StateColor(0, 0, 255)), 20, 500), ProgramImpl(), MultiWorldType(0, listOf(DimensionType.EDGE, DimensionType.WRAPPING))).run()
            }

