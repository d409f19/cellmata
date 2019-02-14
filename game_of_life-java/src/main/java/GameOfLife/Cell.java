package GameOfLife;

public class Cell {
    boolean alive;

    Cell(boolean alive) {
        this.alive = alive;
    }

    boolean isAlive() {
        return this.alive;
    }

    void setState(boolean alive) {
        this.alive = alive;
    }
}
