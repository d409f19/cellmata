package dk.aau.cs.d409f18.cellumata.codegen.kotlin.runtime

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.util.*
import javax.swing.JFrame
import javax.swing.JPanel

class GraphicalDriver(private val worldConfig: WorldConfiguration, program: IProgram, worldType: IWorldType): Driver(worldConfig, program, worldType) {

    override fun run() {
        assert(worldConfig.dims.size == 2)

        val cellSize = worldConfig.cellSize
        val tickrate = worldConfig.tickrate

        val frame = JFrame("Cellmata")
        val panel = frame.add(JPanel())
        panel.preferredSize = Dimension(
            worldConfig.dims[0] * worldConfig.cellSize,
            worldConfig.dims[1] * worldConfig.cellSize
        )
        frame.isResizable = true
        frame.setLocationRelativeTo(null)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.pack()
        frame.isVisible = true
        val g = panel.graphics as Graphics2D

        drawWorld(g)

        Timer().scheduleAtFixedRate(object: TimerTask() {
            override fun run() {
                update()
                drawWorld(g)
            }
        }, 0, (1000F / tickrate).toLong())
    }

    fun drawWorld(g: Graphics2D) {
        var x = 0
        while(x < worldConfig.dims[0]) {
            var y = 0
            while(y < worldConfig.dims[1]) {
                val state = worldCurrent.getCell(x, y)
                val color = worldConfig.colors[state]
                g.color = Color(color.red, color.green, color.blue)
                g.fillRect(
                    x * worldConfig.cellSize,
                    y * worldConfig.cellSize,
                    worldConfig.cellSize,
                    worldConfig.cellSize
                )
                y++
            }
            x++
        }
    }
}