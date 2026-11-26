package main

import (
	"fmt"
	"os"
)

func main() {
	neighbours := [][]int{{-1, 0}, {1, 0}, {0, -1}, {0, 1}}
	directions := []string{"UP", "DOWN", "LEFT", "RIGHT"}
	var numRows, numCols, rounds int
	fmt.Scan(&numRows, &numCols, &rounds)

	UNSET := 100_000
	maze := make([][]string, numRows)
	costMatrix := make([][]int, numRows)
	var moveMatrix [][]int
	for i := 0; i < numRows; i++ {
		maze[i] = make([]string, numCols)
		costMatrix[i] = make([]int, numCols)
		for j := 0; j < numCols; j++ {
			costMatrix[i][j] = UNSET
		}
	}
	edges := make([][]int, 0)
	telRow, telCol := -1, -1
	mode := "search"
	for {
		var rickRow, rickCol int
		fmt.Scan(&rickRow, &rickCol)

		// Read the ASCII maze.
		for i := 0; i < numRows; i++ {
			// ROW: C of the characters in '#.TC?' (i.e. one line of the ASCII maze).
			var ROW string
			fmt.Scan(&ROW)
			for j := 0; j < numCols; j++ {
				maze[i][j] = string(ROW[j])
			}
		}

		if (telRow == -1) && (telCol == -1) {
			telRow, telCol = rickRow, rickCol
			edges = append(edges, []int{rickRow, rickCol})
			costMatrix[rickRow][rickCol] = 0
		}

		for _, edge := range edges {
			for _, neighbour := range neighbours {
				ny := edge[0] + neighbour[0]
				nx := edge[1] + neighbour[1]
				if ny >= 0 && ny < len(maze) && nx >= 0 && nx < len(maze[ny]) {
					if costMatrix[ny][nx] == UNSET+1 {
						continue
					}

					if costMatrix[ny][nx] > costMatrix[edge[0]][edge[1]]+1 {
						if maze[ny][nx] == "." || maze[ny][nx] == "C" {
							costMatrix[ny][nx] = costMatrix[edge[0]][edge[1]] + 1
							edges = append(edges, []int{ny, nx})
							fmt.Fprintln(os.Stderr, "Added edge", ny, nx)
						} else if maze[ny][nx] == "#" {
							costMatrix[ny][nx] = UNSET + 1
						}
					} else if costMatrix[ny][nx] < costMatrix[edge[0]][edge[1]]-1 {
						fmt.Fprintln(os.Stderr, "Cost matrix recalc")
						queue := make([][]int, 0)
						queue = append(queue, []int{ny, nx})
						for len(queue) > 0 {
							y, x := queue[0][0], queue[0][1]
							queue = queue[1:]
							for _, neighbour := range neighbours {
								ny2 := y + neighbour[0]
								nx2 := x + neighbour[1]
								if ny2 >= 0 && ny2 < len(maze) && nx2 >= 0 && nx2 < len(maze[ny2]) {
									if costMatrix[ny2][nx2] != UNSET && costMatrix[ny2][nx2] > costMatrix[y][x]+1 {
										if maze[ny2][nx2] == "." || maze[ny2][nx2] == "C" {
											costMatrix[ny2][nx2] = costMatrix[y][x] + 1
											queue = append(queue, []int{ny2, nx2})
										}
									}
								}
							}
						}
					}
				}
			}
		}

		fmt.Fprintln(os.Stderr, "Edges: ", edges)
		// prunes edges where all neighbours are within 1 of each other
		for i := 0; i < len(edges); i++ {
			edge := edges[i]
			allSet := true
			for _, neighbour := range neighbours {
				ny := edge[0] + neighbour[0]
				nx := edge[1] + neighbour[1]
				if ny >= 0 && ny < len(maze) && nx >= 0 && nx < len(maze[ny]) {
					if costMatrix[ny][nx] != UNSET+1 && costMatrix[ny][nx] > costMatrix[edge[0]][edge[1]]+1 {
						allSet = false
						break
					}
				}
			}
			if allSet {
				edges = append(edges[:i], edges[i+1:]...)
				i--
			}
		}
		fmt.Fprintln(os.Stderr, "Edges: ", edges)

		target := []int{telRow, telCol}
		if maze[rickRow][rickCol] == "C" {
			mode = "escape"
			moveMatrix = costMatrix
		}

		if mode == "search" {
			found := false
			if len(edges) == 0 {
				for i := 0; i < numRows && !found; i++ {
					for j := 0; j < numCols; j++ {
						if maze[i][j] == "C" {
							target = bfs(maze, "C", rickRow, rickCol)
							if (target[0] == -1) && (target[1] == -1) {
								fmt.Fprintln(os.Stderr, "No path to control room")
								target = []int{telRow, telCol}
							}
							found = true
							break
						}
					}
				}
			}

			if (target[0] == telRow) && (target[1] == telCol) {
				fmt.Fprintln(os.Stderr, "Searching")
				if len(edges) == 0 {
					fmt.Fprintln(os.Stderr, "No more edges")
				} else {
					target = edges[len(edges)-1]
				}
			} else {
				fmt.Fprintln(os.Stderr, "Found control room at ", target)
			}
			potentialField := make([][]int, len(maze))
			for i := 0; i < len(maze); i++ {
				potentialField[i] = make([]int, len(maze[i]))
				for j := 0; j < len(maze[i]); j++ {
					potentialField[i][j] = UNSET
				}
			}
			populatePotentialField(maze, &potentialField, target[0], target[1], rickRow, rickCol)
			moveMatrix = potentialField
		}

		fmt.Fprintln(os.Stderr, "Target: ", target)
		minI := -1
		for i, neighbour := range neighbours {
			ny := rickRow + neighbour[0]
			nx := rickCol + neighbour[1]
			if ny >= 0 && ny < len(maze) && nx >= 0 && nx < len(maze[ny]) {
				if moveMatrix[ny][nx] == UNSET {
					continue
				}

				if moveMatrix[ny][nx] < moveMatrix[rickRow][rickCol] {
					if minI == -1 {
						minI = i
					} else if moveMatrix[ny][nx] < moveMatrix[rickRow+neighbours[minI][0]][rickCol+neighbours[minI][1]] {
						minI = i
					}
				}
			}
		}
		if minI == -1 {
			for i := 0; i < len(moveMatrix); i++ {
				fmt.Fprintln(os.Stderr, moveMatrix[i])
			}
			fmt.Fprintln(os.Stderr, "No move")
			for i := 0; i < len(maze); i++ {
				for j := 0; j < len(maze[i]); j++ {
					if i == rickRow && j == rickCol {
						fmt.Fprint(os.Stderr, "x")
					} else {
						fmt.Fprint(os.Stderr, maze[i][j])
					}
				}
				fmt.Fprintln(os.Stderr)
			}
		}
		fmt.Println(directions[minI])
	}
}

// breadth-first search
func bfs(maze [][]string, target string, y int, x int) []int {
	visited := make([][]bool, len(maze))
	for i := 0; i < len(maze); i++ {
		visited[i] = make([]bool, len(maze[i]))
	}

	neighbours := [][]int{{-1, 0}, {1, 0}, {0, -1}, {0, 1}}
	queue := [][]int{{y, x}}
	for len(queue) > 0 {
		y, x := queue[0][0], queue[0][1]
		queue = queue[1:]
		visited[y][x] = true

		if maze[y][x] == target {
			return []int{y, x}
		}

		for _, neighbour := range neighbours {
			ny := y + neighbour[0]
			nx := x + neighbour[1]
			if ny >= 0 && ny < len(maze) && nx >= 0 && nx < len(maze[ny]) {
				if visited[ny][nx] {
					continue
				}
				if maze[ny][nx] == "." || maze[ny][nx] == "T" || maze[ny][nx] == target {
					queue = append(queue, []int{ny, nx})
				}
			}
		}
	}

	return []int{-1, -1}
}

func populatePotentialField(maze [][]string, field *[][]int, y int, x int, KR int, KC int) {
	neighbours := [][]int{{-1, 0}, {1, 0}, {0, -1}, {0, 1}}
	queue := [][]int{{y, x, 0}}

	for len(queue) > 0 {
		y, x, value := queue[0][0], queue[0][1], queue[0][2]
		queue = queue[1:]
		(*field)[y][x] = value

		if (y == KR) && (x == KC) {
			return
		}

		for _, neighbour := range neighbours {
			ny := y + neighbour[0]
			nx := x + neighbour[1]
			if ny >= 0 && ny < len(maze) && nx >= 0 && nx < len(maze[ny]) {
				if (*field)[ny][nx] != 100_000 {
					continue
				}
				if maze[ny][nx] == "." || maze[ny][nx] == "T" || maze[ny][nx] == "C" {
					queue = append(queue, []int{ny, nx, value + 1})
				}
			}
		}
	}
}
