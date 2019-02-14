package GameOfLife;

import java.util.Date;
import java.util.Random;

public class Canvas {
    int x;
    int y;
    Cell[][] canvas;
    Cell[][] tempCanvas;

    Random random = new Random(new Date().getTime());

    public Canvas(int x, int y) {
        this.x = x;
        this.y = y;
        this.canvas = new Cell[x][y];
        this.tempCanvas = this.canvas;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                this.canvas[i][j] = new Cell(random.nextInt(2) == 1);
            }
        }
    }

    void tick() {
        int neighbours = 0;
        for (int i = 0; i < this.x; i++) {
            for (int j = 0; j < this.y; j++) {
                neighbours = countMooreNeighbours(i, j);

                // deep copy cell
                tempCanvas[i][j] = canvas[i][j];

                // set new state based on the life of the cell
                tempCanvas[i][j].setState(cellLife(tempCanvas[i][j], neighbours));
            }
        }
        canvas = tempCanvas;
    }

    private int countMooreNeighbours(int x, int y) {
        int neighbours = 0;
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                // bound coordinate within constraints
                if (this.canvas[constrainToRange(x + i, 0, this.x)][constrainToRange(y + j, 0, this.y)].isAlive())
                    neighbours++;
            }
        }
        return neighbours;
    }

    private static int constrainToRange(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    private boolean cellLife(Cell cell, int neighbours) {
        if (cell.isAlive()) {
            if (neighbours < 2) return false;
            if (2 < neighbours && neighbours < 4) return true;
            if (neighbours > 3) return false;
        } else if (neighbours == 3) {
            return true;
        }
        return false;
    }
}
