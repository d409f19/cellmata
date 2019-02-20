package GameOfLife;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static GameOfLife.Main.CELLSIZE;
import static GameOfLife.Main.WORLD_X;
import static GameOfLife.Main.WORLD_Y;

class Graphics {

    public static Frame setupGUI(int x, int y) {
        Frame frame = new Frame("Conway's Game of Life");
        frame.setSize(x, y);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        return frame;
    }

    public static void drawCanvas(Canvas canvas, Frame frame) {
        for (int i = 0; i < WORLD_X; i++) {
            for (int j = 0; j < WORLD_Y; j++) {
                if (canvas.getCanvas()[i][j].isAlive()) {
                    frame.getGraphics().fillRect(i * CELLSIZE, j * CELLSIZE, CELLSIZE, CELLSIZE);
                } else {
                    frame.getGraphics().clearRect(i * CELLSIZE, j * CELLSIZE, CELLSIZE, CELLSIZE);
                }
            }
        }
    }
}
