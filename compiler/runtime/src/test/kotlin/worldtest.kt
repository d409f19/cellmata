package dk.aau.cs.d409f18.cellumata.codegen.kotlin.runtime

import org.junit.jupiter.api.Test

class WorldTest {

    /*@Test
    fun single_iteration_test() {
        val worldConfig = WorldConfiguration(
            dims = listOf(2, 2),
            colors = listOf(),
            cellSize = 2,
            tickrate = 500
        )
        val program = object : IProgram {
            override fun updateCell(worldView: IWorldView): Int {
                return 1
            }
        }
        val driver = Driver(worldConfig, program, worldType = WrappingWorldType())

        driver.worldCurrent.setCell(0, 0, state = 0)
        driver.worldCurrent.setCell(0, 1, state = 0)
        driver.worldCurrent.setCell(1, 0, state = 0)
        driver.worldCurrent.setCell(1, 1, state = 0)

        driver.update()

        assert(driver.worldCurrent.getCell(0, 0) == 1)
        assert(driver.worldCurrent.getCell(0, 1) == 1)
        assert(driver.worldCurrent.getCell(1, 0) == 1)
        assert(driver.worldCurrent.getCell(1, 1) == 1)
    }

    @Test
    fun wrapping_world_view_test() {
        val dims = listOf(3, 3)
        val world = World(dims)

        val worldView = WrappingWorldView(world, dims, listOf(0, 0))

        world.setCell(0, 0, state = 1)
        world.setCell(0, 1, state = 2)
        world.setCell(0, 2, state = 3)
        world.setCell(1, 0, state = 4)
        world.setCell(1, 1, state = 5)
        world.setCell(1, 2, state = 6)
        world.setCell(2, 0, state = 7)
        world.setCell(2, 1, state = 8)
        world.setCell(2, 2, state = 9)


        // Inbound
        assert(worldView.getCell(0, 0) == 1)
        assert(worldView.getCell(0, 1) == 2)
        assert(worldView.getCell(0, 2) == 3)
        assert(worldView.getCell(1, 0) == 4)
        assert(worldView.getCell(1, 1) == 5)
        assert(worldView.getCell(1, 2) == 6)
        assert(worldView.getCell(2, 0) == 7)
        assert(worldView.getCell(2, 1) == 8)
        assert(worldView.getCell(2, 2) == 9)

        // Wrapping
        assert(worldView.getCell(-1, 0) == 7)
        assert(worldView.getCell(0, -1) == 3)
        assert(worldView.getCell(3, 0) == 1)
        assert(worldView.getCell(0, 3) == 1)
    }

    @Test
    fun edge_world_view_test() {
        val dims = listOf(3, 3)
        val world = World(dims)

        val worldView = EdgeWorldView(world, dims, listOf(0, 0), -1)

        world.setCell(0, 0, state = 1)
        world.setCell(0, 1, state = 2)
        world.setCell(0, 2, state = 3)
        world.setCell(1, 0, state = 4)
        world.setCell(1, 1, state = 5)
        world.setCell(1, 2, state = 6)
        world.setCell(2, 0, state = 7)
        world.setCell(2, 1, state = 8)
        world.setCell(2, 2, state = 9)


        // Inbound
        assert(worldView.getCell(0, 0) == 1)
        assert(worldView.getCell(0, 1) == 2)
        assert(worldView.getCell(0, 2) == 3)
        assert(worldView.getCell(1, 0) == 4)
        assert(worldView.getCell(1, 1) == 5)
        assert(worldView.getCell(1, 2) == 6)
        assert(worldView.getCell(2, 0) == 7)
        assert(worldView.getCell(2, 1) == 8)
        assert(worldView.getCell(2, 2) == 9)

        // Wrapping
        assert(worldView.getCell(-1, 0) == -1)
        assert(worldView.getCell(0, -1) == -1)
        assert(worldView.getCell(3, 0) == -1)
        assert(worldView.getCell(0, 3) == -1)
    }*/
}