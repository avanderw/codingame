package main

import (
	"bufio"
	"fmt"
	"os"
	"sort"
	"strconv"
	"strings"
)

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/

type Cell struct {
	id               int
	_type            int
	initialResources int
	resources        int
	neigh            [6]int
	myAnts           int
	oppAnts          int
	distance         int
}

var graph []Cell

func crystalFilter(c Cell) bool {
	return c._type == 2 && c.resources > 0
}
func eggFilter(c Cell) bool {
	return c._type == 1 && c.resources > 0
}

func main() {
	scanner := bufio.NewScanner(os.Stdin)
	scanner.Buffer(make([]byte, 1000000), 1000000)
	var inputs []string

	// numberOfCells: amount of hexagonal cells in this map
	var numberOfCells int
	scanner.Scan()
	fmt.Sscan(scanner.Text(), &numberOfCells)

	graph = make([]Cell, numberOfCells)
	for i := 0; i < numberOfCells; i++ {
		// _type: 0 for empty, 1 for eggs, 2 for crystal
		// initialResources: the initial amount of eggs/crystals on this cell
		// neigh0: the index of the neighbouring cell for each direction
		var _type, initialResources, neigh0, neigh1, neigh2, neigh3, neigh4, neigh5 int
		scanner.Scan()
		fmt.Sscan(scanner.Text(), &_type, &initialResources, &neigh0, &neigh1, &neigh2, &neigh3, &neigh4, &neigh5)
		graph[i].id = i
		graph[i]._type = _type
		graph[i].initialResources = initialResources
		graph[i].neigh[0] = neigh0
		graph[i].neigh[1] = neigh1
		graph[i].neigh[2] = neigh2
		graph[i].neigh[3] = neigh3
		graph[i].neigh[4] = neigh4
		graph[i].neigh[5] = neigh5
		graph[i].distance = -1
	}

	var numberOfBases int
	scanner.Scan()
	fmt.Sscan(scanner.Text(), &numberOfBases)

	var sourceBaseIdx int
	scanner.Scan()
	inputs = strings.Split(scanner.Text(), " ")
	for i := 0; i < numberOfBases; i++ {
		myBaseIndex, _ := strconv.ParseInt(inputs[i], 10, 32)
		sourceBaseIdx = int(myBaseIndex)
	}
	floodFill(graph, sourceBaseIdx, 0)

	scanner.Scan()
	inputs = strings.Split(scanner.Text(), " ")
	for i := 0; i < numberOfBases; i++ {
		oppBaseIndex, _ := strconv.ParseInt(inputs[i], 10, 32)
		_ = oppBaseIndex
	}
	for {
		for i := 0; i < numberOfCells; i++ {
			// resources: the current amount of eggs/crystals on this cell
			// myAnts: the amount of your ants on this cell
			// oppAnts: the amount of opponent ants on this cell
			var resources, myAnts, oppAnts int
			scanner.Scan()
			fmt.Sscan(scanner.Text(), &resources, &myAnts, &oppAnts)
			graph[i].resources = resources
			graph[i].myAnts = myAnts
			graph[i].oppAnts = oppAnts
		}

		// fmt.Fprintln(os.Stderr, "Debug messages...")

		crystalGraph := filter(graph, crystalFilter)
		sort.Slice(crystalGraph, func(i, j int) bool {
			return crystalGraph[i].distance < crystalGraph[j].distance
		})

		eggGraph := filter(graph, eggFilter)
		fmt.Fprintln(os.Stderr, "crystalGraph", crystalGraph)
		fmt.Fprintln(os.Stderr, "eggGraph", eggGraph)

		for i, v := range crystalGraph {
			str := len(crystalGraph) - i
			fmt.Print("LINE ", sourceBaseIdx, v.id, str)
			if i < len(crystalGraph)-1 {
				fmt.Print(";")
			}
		}
		if len(eggGraph) > 0 {
			fmt.Print(";")
		}

		sort.Slice(eggGraph, func(i, j int) bool {
			return eggGraph[i].distance > eggGraph[j].distance
		})
		for i, v := range eggGraph {
			str := (i + 1) * v.resources
			fmt.Print("LINE ", sourceBaseIdx, v.id, str)
			if i < len(eggGraph)-1 {
				fmt.Print(";")
			}
		}

		fmt.Println()
		// WAIT | LINE <sourceIdx> <targetIdx> <strength> | BEACON <cellIdx> <strength> | MESSAGE <text>

	}
}

func floodFill(graph []Cell, source int, distance int) {
	queue := []int{source, distance}
	for len(queue) > 0 {
		var curr, dist int
		curr, dist, queue = queue[0], queue[1], queue[2:]
		if graph[curr].distance == -1 {
			graph[curr].distance = dist
			fmt.Fprintln(os.Stderr, "flood fill", curr, dist)
			for _, v := range graph[curr].neigh {
				if v != -1 {
					queue = append(queue, v, dist+1)
				}
			}
		}
	}
}

func filter(s []Cell, fn func(Cell) bool) []Cell {
	var p []Cell
	for _, v := range s {
		if fn(v) {
			p = append(p, v)
		}
	}
	return p
}
