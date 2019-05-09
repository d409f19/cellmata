package dk.aau.cs.d409f18.cellumata.codegen.kotlin.runtime

/**
 * Implements a cellular automator.
 */
interface IProgram {
    /**
     * Given a world view return the new state of the center cell
     */
    fun updateCell(worldView: IWorldView): Int
}

/**
 * Provides local view of the world.
 * Any lookup is relative to the current cell.
 */
interface IWorldView {
    fun getCell(vararg relPos: Int): Int
}

enum class DimensionType {
    WRAPPING,
    EDGE
}

infix fun Int.wrap(divisor: Int) = (this % divisor).let { if (it < 0) it + divisor else it }

class MultiWorldView(private val world: World, private val dims: List<Int>, private val pos: List<Int>, private val edge: Int, private val dimTypes: List<DimensionType>): IWorldView {
    override fun getCell(vararg relPos: Int): Int {
        // Check if any dimension is a edge type, and if it is check if the coordinate is over the edge
        // If the coordinate is over the edge on any edge type dimension, it will default to the edge type.
        val overEdge = relPos.mapIndexed { index, p ->
            if (dimTypes[index] != DimensionType.EDGE) {
                (pos[index] + p) >= dims[index] || (pos[index] + p) < 0
            } else {
                false
            }
        }.contains(true)
        return if (overEdge) {
            edge
        } else {
            world.getCell(*relPos.mapIndexed { index, p -> p wrap dims[index] }.toIntArray())
        }
    }
}

/**
 * Defines the interface for world edge logic, like edge states, and wrapping
 */
interface IWorldType {
    fun getWorldView(world: World, dims: List<Int>, pos: List<Int>): IWorldView
}

/**
 * Implements a world view capable of having edge states and wrapping dimensions
 */
class MultiWorldType(private val edge: Int, private val dimTypes: List<DimensionType>): IWorldType {
    override fun getWorldView(world: World, dims: List<Int>, pos: List<Int>): IWorldView {
        return MultiWorldView(world, dims, pos, edge, dimTypes)
    }
}

/**
 * Runs a cellular automaton by computing the next world configuration using the cell logic from the generated program.
 */
open class Driver(private val worldConfig: WorldConfiguration, private val program: IProgram, private val worldType: IWorldType) {
    var worldCurrent = World(worldConfig.dims)
    var worldNext = World(worldConfig.dims)

    open fun run() {
        while(true) {
            update()
        }
    }

    open fun update() {
        fun iterate(subDims: List<Int>, pos: List<Int> = listOf()) {
            if (subDims.size == 1) {
                for (x in 0 until subDims[0]) {
                    val currentPos = pos + x
                    worldNext.setCell(
                        *currentPos.toIntArray(),
                        state = program.updateCell(
                            worldView = worldType.getWorldView(worldCurrent, worldConfig.dims, currentPos)
                        )
                    )
                }
            } else {
                for (x in 0 until subDims[0]) {
                    iterate(subDims.subList(1, subDims.size), pos + x)
                }
            }
        }

        // Run iteration
        iterate(worldConfig.dims)

        // Flip worlds
        val temp = worldCurrent
        worldCurrent = worldNext
        worldNext = temp
    }
}

class World(private val dims: List<Int>) {
    /**
     * World is implemented as an N dimensional MutableList.
     * Because the N isn't know when the code is written, an exact type can't be specified.
     * Therefore Any is used to allow for both MutableList and Int.
     */
    var world = generateWorld(dims)

    private fun generateWorld(dims: List<Int>): MutableList<Any> {
        if (dims.size == 1) {
            return Array(dims[0]) { 0 }.toMutableList()
        } else {
            val subDims = dims.subList(1, dims.size)
            return Array(dims[0]) { generateWorld(subDims) }.toMutableList()
        }
    }

    fun getCell(vararg pos: Int): Int {
        assert(pos.size == dims.size)

        fun lookup(pos: List<Int>, world: MutableList<Any>): Int {
            if (pos.size == 1) {
                return (world[pos[0]] as Int)
            } else {
                @Suppress("UNCHECKED_CAST")
                return lookup(pos.subList(1, pos.size), world[pos[0]] as MutableList<Any>)
            }
        }

        return lookup(pos.toList(), world)
    }

    fun setCell(vararg pos: Int, state: Int) {
        assert(pos.size == dims.size)

        fun lookup(pos: List<Int>, world: MutableList<Any>) {
            if (pos.size == 1) {
                world[pos[0]] = state
            } else {
                @Suppress("UNCHECKED_CAST")
                lookup(pos.subList(1, pos.size), world[pos[0]] as MutableList<Any>)
            }
        }

        lookup(pos.toList(), world)
    }
}