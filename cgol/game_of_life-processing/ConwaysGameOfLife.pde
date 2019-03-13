
final int CELL_SIZE = 6;
final int WORLD_SIZE = 120;
final int SCREEN_SIZE = CELL_SIZE * WORLD_SIZE;

int[][] world;
int f = 0;
int updateDelay = 5;

void setup() {
  size(720, 720);
  
  world = new int[WORLD_SIZE][WORLD_SIZE];
  
  for (int i = 0; i < WORLD_SIZE; i++) {
    for (int j = 0; j < WORLD_SIZE; j++) {
      world[i][j] = random(100) < 30 ? 1 : 0;
    }
  }
  
  noStroke();
}

void update() {
  int[][] newWorld = new int[WORLD_SIZE][WORLD_SIZE];
  
  for (int i = 0; i < WORLD_SIZE; i++) {
    for (int j = 0; j < WORLD_SIZE; j++) {
      int state = world[i][j];
      ArrayList<Integer> neighbours = neighbours(i, j);
      int nextState = nextCellState(state, neighbours);
      newWorld[i][j] = nextState;
    }
  }
  
  world = newWorld;
}

int nextCellState(int state, ArrayList<Integer> neighbours) {
  int aliveNeighbours = 0;
  for (int ns : neighbours) {
    if (ns == 1) aliveNeighbours++;
  }
  
  if (state == 0) {
    
    // Dead cell rules
    if (aliveNeighbours == 3) {
      return 1;
    } else {
      return 0;
    }
    
  } else if (state == 1) {
    
    // Live cell rules
    if (aliveNeighbours < 2) {
      return 0;
    } else if (aliveNeighbours < 4) {
      return 1;
    } else {
      return 0;
    }
    
  } else {
    // Other?
    return 0;
  }
}

void draw() {
  for (int i = 0; i < WORLD_SIZE; i++) {
    for (int j = 0; j < WORLD_SIZE; j++) {
      fill(world[i][j] == 0 ? color(255, 255, 255) : color(0, 0, 0));
      rect(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
    }
  }
  
  f++;
  if (f == updateDelay) {
    update();
    f = 0;
  }
}

ArrayList<Integer> neighbours(int x, int y) {
  ArrayList<Integer> list = new ArrayList<Integer>();
  
  for (int i = -1; i <= 1; i++) {
    for (int j = -1; j <= 1; j++) {
      if (i != 0 || j != 0) {
        int sx = wrap(x + i);
        int sy = wrap(y + j);
        list.add(world[sx][sy]);
      }
    }
  }
  
  return list;
}

int wrap(int x) {
  x = x % WORLD_SIZE;
  if (x < 0) x = WORLD_SIZE + x;
  return x;
}
