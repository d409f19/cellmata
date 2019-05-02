package dk.aau.cs.d409f18.cellumata.codegen.kotlin.runtime

interface IProgram {
    fun updateCell(worldView: IWorldView): Int
}

interface IWorldView {
    fun getCell(vararg relPos: Int): Int
}

@Deprecated("Use MultiWorldView")
class WrappingWorldView(private val world: World, private val dims: List<Int>, private val pos: List<Int>): IWorldView {
    override fun getCell(vararg relPos: Int): Int {
        return world.getCell(*relPos.mapIndexed { index, p ->
            val x = (pos[index] + p).rem(dims[index])
            // We need a bit of logic to convert remainder operator to modulus operator,
            // otherwise we are gonna have problems with negative numbers
            if (x < 0) {
                x + dims[index]
            } else {
                x
            }
        }.toIntArray())
    }
}

@Deprecated("Use MultiWorldView")
class EdgeWorldView(private val world: World, private val dims: List<Int>, private val pos: List<Int>, private val edge: Int): IWorldView {
    override fun getCell(vararg relPos: Int): Int {
        val overEdge = relPos.mapIndexed { index, p -> (pos[index] + p) >= dims[index] || (pos[index] + p) < 0 }.contains(true)
        return if (overEdge) {
            edge
        } else {
            world.getCell(*relPos.mapIndexed { index, p -> pos[index] + p }.toIntArray())
        }
    }
}

enum class DimensionType {
    WRAPPING,
    EDGE
}

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
            world.getCell(*relPos.mapIndexed { index, p ->
                val x = (pos[index] + p).rem(dims[index])
                // We need a bit of logic to convert remainder operator to modulus operator,
                // otherwise we are gonna have problems with negative numbers
                if (x < 0) {
                    x + dims[index]
                } else {
                    x
                }
            }.toIntArray())
        }
    }
}

interface IWorldType {
    fun getWorldView(world: World, dims: List<Int>, pos: List<Int>): IWorldView
}

@Deprecated("Use MultiWorldType")
class WrappingWorldType: IWorldType {
    override fun getWorldView(world: World, dims: List<Int>, pos: List<Int>): WrappingWorldView {
        return WrappingWorldView(world, dims, pos)
    }
}

@Deprecated("Use MultiWorldType")
class EdgeWorldType(private val edge: Int): IWorldType {
    override fun getWorldView(world: World, dims: List<Int>, pos: List<Int>): IWorldView {
        return EdgeWorldView(world, dims, pos, edge)
    }
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
                var x = subDims[0] - 1
                while (x >= 0) {
                    val currentPos = pos + listOf(x)
                    worldNext.setCell(
                        *currentPos.toIntArray(),
                        state = program.updateCell(
                            worldView = worldType.getWorldView(worldCurrent, worldConfig.dims, currentPos)
                        )
                    )
                    x--
                }
            } else {
                var x = subDims[0] - 1
                while (x >= 0) {
                    @Suppress("UNCHECKED_CAST")
                    iterate(subDims.subList(1, subDims.size), pos + listOf(x))
                    x--
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
        var x = dims[0]
        if (dims.size == 1) {
            return generateSequence {
                x--
                if (x >= 0) {
                    0
                } else {
                    null
                }
            }.toMutableList()
        } else {
            val subDims = dims.subList(1, dims.size)

            return generateSequence {
                x--
                if (x >= 0) {
                    generateWorld(subDims)
                } else {
                    null
                }
            }.toMutableList()
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