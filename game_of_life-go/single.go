package main

import (
	"fmt"
	"strings"
)

const WIDTH = 10
const HEIGHT = 10

type Board *[WIDTH][HEIGHT]bool

var current Board
var next Board

func main() {
	// Initialize boards
	current = Board(&[WIDTH][HEIGHT]bool{})
	for i := 0; i < WIDTH; i++ {
		current[i] = [HEIGHT]bool{}
	}

	next = Board(&[WIDTH][HEIGHT]bool{})
	for i := 0; i < WIDTH; i++ {
		next[i] = [HEIGHT]bool{}
	}

	// Setup initial board
	current[4][3] = true
	current[4][4] = true
	current[4][5] = true

	// Run n iterations
	for i := 0; i < 1e6; i++ {
		// Start and synchronize worker for each row
		for x := 0; x < WIDTH; x++ {
			for y := 0; y < HEIGHT; y++ {
				updateCell(x, y)
			}
		}

		// Swap current and next board
		flipBoards()
	}

	flipBoards()
	fmt.Println()
	printBoard()
}

type Cord struct {
	x int
	y int
}

var neighs = []Cord{
	{-1, -1},
	{-1, 0},
	{-1, 1},
	{1, -1},
	{1, 0},
	{1, 1},
	{0, -1},
	{0, 1},
}

func updateCell(x int, y int) {
	// Count neighbours
	n := 0
	for _, offset := range neighs {
		if getCell(x+offset.x, y+offset.y) {
			n += 1
		} else {
			n += 0
		}
	}

	// Compute state
	s := getCell(x, y)
	next[x][y] = (s == true && (n == 2 || n == 3)) || (s == false && n == 3)
}

func abs(x int) int {
	if x < 0 {
		return -x
	}
	return x
}

// Get state of cell
func getCell(x int, y int) bool {
	return current[abs(x%WIDTH)][abs(y%HEIGHT)]
}

// Swap current and next board
func flipBoards() {
	n := current
	current = next
	next = n
}

// Pretty print current board
func printBoard() {
	var buf strings.Builder

	for x := 0; x < WIDTH; x++ {
		for y := 0; y < HEIGHT; y++ {
			if getCell(x, y) {
				buf.WriteString("1")
			} else {
				buf.WriteString("0")
			}
		}
		buf.WriteString("  ")
		for y := 0; y < HEIGHT; y++ {
			if next[x][y] {
				buf.WriteString("1")
			} else {
				buf.WriteString("0")
			}
		}
		buf.WriteString("\n")
	}
	fmt.Print(buf.String())
}
