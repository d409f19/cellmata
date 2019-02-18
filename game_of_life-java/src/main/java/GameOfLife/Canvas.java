package GameOfLife;

import java.util.Date;
import java.util.Random;

class Canvas {
    private final int x;
    private final int y;
    private Cell[][] canvas;
    private Cell[][] tempCanvas;

    Canvas(int x, int y) {
        this.x = x;
        this.y = y;
        // set two canvasses up, such that one can be calculated from,
        // and the resulting state can be stored in the other
        this.canvas = new Cell[x][y];
        this.tempCanvas = new Cell[x][y];
        // seed canvas with randomness
        Random random = new Random(new Date().getTime());
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                // 50/50 chance of each cell being alive at initial configuration
                this.canvas[i][j] = new Cell(random.nextInt(2) == 0);
                // initialise tempCanvas to false, doesn't affect runtime, but each cell needs to have a Cell
                this.tempCanvas[i][j] = new Cell(false);
            }
        }
    }

    void tick() {
        int neighbours;
        // temp canvas-array for swapping before return
        Cell[][] temp;
        for (int i = 0; i < this.x; i++) {
            for (int j = 0; j < this.y; j++) {
                neighbours = countMooreNeighbours(i, j);

                // set new state in tempCanvas based on the life of the given cell in canvas
                tempCanvas[i][j].setState(cellLife(canvas[i][j], neighbours));
            }
        }
        // swap references to arrays
        temp = canvas;
        canvas = tempCanvas;
        tempCanvas = temp;
    }

    private int countMooreNeighbours(int x, int y) {
        int neighbours = 0;
        // iterate nearest eight neighbours
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                // set canvas position to actual
                int xPos = x + i;
                int yPos = y + j;

                // don't count neighbours of a given cell, by short-circuiting, or if out of bounds
                if (!(i == 0 && j == 0 || xPos < 0 || xPos >= this.x || yPos < 0 || yPos >= this.y)) {
                    // if neighbour is alive, count up neighbours
                    if (this.canvas[x + i][y + j].isAlive())
                        neighbours++;
                }
            }
        }
        return neighbours;
    }

    private boolean cellLife(Cell cell, int neighbours) {
        // if cell is alive, and has between 2 and 3 alive neighbours, stay alive, else die
        if (cell.isAlive()) {
            return neighbours >= 2 && neighbours <= 3;
        // if cell is dead, become alive if it has 3 neighbours, else stay dead
        } else return neighbours == 3;
    }

    Cell[][] getCanvas() {
        return canvas;
    }
}
