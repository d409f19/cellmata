package dk.aau.cs.d409f18.cellumata.codegen.kotlin.runtime

interface IProgram {
    fun updateCell(worldView: IWorldView): Int
}

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

interface IWorldType {
    fun getWorldView(world: World, dims: List<Int>, pos: List<Int>): IWorldView
}

class MultiWorldType(private val edge: Int, private val dimTypes: List<DimensionType>): IWorldType {
    override fun getWorldView(world: World, dims: List<Int>, pos: List<Int>): IWorldView {
        return MultiWorldView(world, dims, pos, edge, dimTypes)
    }
}

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