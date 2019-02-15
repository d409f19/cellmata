import pygame

from pygame.locals import *
from random import randint


WORLD_SIZE = 120
CELL_SIZE = 6

DEAD = 0
ALIVE = 1

pygame.init()
screen = pygame.display.set_mode((WORLD_SIZE * CELL_SIZE, WORLD_SIZE * CELL_SIZE))
world = [[randint(0, 1) for x in range(WORLD_SIZE)] for y in range(WORLD_SIZE)]
running = True
clock = pygame.time.Clock()


def render():
	for x in range(WORLD_SIZE):
		for y in range(WORLD_SIZE):
			color = (255, 255, 255) if world[x][y] == DEAD else (0, 0, 0)
			pygame.draw.rect(screen, color, [x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE])


def count_alive_neighbours(x, y):
	count = 0
	for dx in (-1, 0, 1):
		for dy in (-1, 0, 1):
			if dx != 0 or dy != 0:
				cell_state = world[(x + dx) % WORLD_SIZE][(y + dy) % WORLD_SIZE]
				if cell_state == ALIVE:
					count += 1
	return count


def get_next_state(state, alive_neighbours):
	if state == ALIVE:
		if 2 <= alive_neighbours <= 3:
			return ALIVE
		else:
			return DEAD
		
	else:
		if alive_neighbours == 3:
			return ALIVE
		else:
			return DEAD

def update():
	global world
	new_world = [[0]*WORLD_SIZE for i in range(WORLD_SIZE)]
	
	for x in range(WORLD_SIZE):
		for y in range(WORLD_SIZE):
			alive_neighbours = count_alive_neighbours(x, y)
			new_world[x][y] = get_next_state(world[x][y], alive_neighbours)
	
	world = new_world


while running:
    for event in pygame.event.get():
        if event.type == KEYDOWN:
            if event.key == K_ESCAPE:
                running = False
        elif event.type == QUIT:
            running = False

    render()
    pygame.display.flip()
    pygame.time.delay(5)
    update()

pygame.quit()