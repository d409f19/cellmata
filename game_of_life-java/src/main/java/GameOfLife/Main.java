package GameOfLife;

import java.awt.*;
import java.util.concurrent.TimeUnit;

import static GameOfLife.Graphics.drawCanvas;
import static GameOfLife.Graphics.setupGUI;

class Main {
    static final int CELLSIZE = 5;
    static final int WORLD_X = 200;
    static final int WORLD_Y = 200;
    private static final int VIEWPORT_X = WORLD_X * CELLSIZE;
    private static final int VIEWPORT_Y = WORLD_Y * CELLSIZE;

    public static void main(String[] args) {
        Frame frame = setupGUI(VIEWPORT_X, VIEWPORT_Y);
        Canvas canvas = new Canvas(WORLD_X, WORLD_Y);

        while (true) {
            drawCanvas(canvas, frame);
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            canvas.tick();
        }
    }
}
