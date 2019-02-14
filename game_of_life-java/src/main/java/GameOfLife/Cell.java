package GameOfLife;

class Cell {
    private boolean alive;

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
