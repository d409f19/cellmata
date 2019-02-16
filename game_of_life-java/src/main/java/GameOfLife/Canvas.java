package GameOfLife;

import java.util.Date;
import java.util.Random;

class Canvas {
    private final int x;
    private final int y;
    private Cell[][] canvas;
    private final Cell[][] tempCanvas;

    Canvas(int x, int y) {
        this.x = x;
        this.y = y;
        this.canvas = new Cell[x][y];
        this.tempCanvas = new Cell[x][y];
        // seed canvas with randomness
        Random random = new Random(new Date().getTime());
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                this.canvas[i][j] = new Cell(random.nextInt(20) == 0);
            }
        }
    }

    void tick() {
        int neighbours;
        for (int i = 0; i < this.x; i++) {
            for (int j = 0; j < this.y; j++) {
                neighbours = countMooreNeighbours(i, j);

                // deep copy cell
                tempCanvas[i][j] = canvas[i][j];

                // set new state based on the life of the cell
                tempCanvas[i][j].setState(cellLife(canvas[i][j], neighbours));
            }
        }
        canvas = tempCanvas;
    }

    private int countMooreNeighbours(int x, int y) {
        int neighbours = 0;
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                int xPos = x + i;
                int yPos = y + j;

                if (!(xPos < 0 || xPos >= this.x || yPos < 0 || yPos >= this.y)) {
                    // bound coordinate within constraints
                    if (this.canvas[x + i][y + j].isAlive())
                        neighbours++;
                }
            }
        }
        return neighbours;
    }

    private boolean cellLife(Cell cell, int neighbours) {
        if (cell.isAlive()) {
            return neighbours >= 2 && neighbours <= 3;
        } else return neighbours == 3;
    }

    public Cell[][] getCanvas() {
        return canvas;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
