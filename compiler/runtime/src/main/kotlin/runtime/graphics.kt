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
        panel.preferredSize = Dimension(worldConfig.dims[0] * cellSize, worldConfig.dims[1] * cellSize)
        frame.isResizable = true
        frame.setLocationRelativeTo(null)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.pack()
        frame.isVisible = true
        val g = panel.graphics as Graphics2D

        Timer().scheduleAtFixedRate(object: TimerTask() {
            override fun run() {
                update()

                println("Start tick")
                var x = 0
                while(x < worldConfig.dims[0]) {
                    var y = 0
                    while(y < worldConfig.dims[1]) {
                        println("Pos $x $y")
                        val state = worldCurrent.getCell(x, y)
                        val color = worldConfig.colors[state]
                        g.color = Color(color.red, color.green, color.blue)
                        g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize)
                        y++
                    }
                    x++
                }
                println("End tick")
            }
        }, 0, tickrate)
    }
}