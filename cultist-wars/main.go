package main

import (
	"errors"
	"fmt"
	"math"
)

/**
 * Convert neutral units and attack enemy ones
 **/

type Unit struct {
	id, unitType, hp, x, y, owner int
}

func main() {
	// myId: 0 - you are the first player, 1 - you are the second player
	var myId int
	fmt.Scan(&myId)
	enemyId := 1 - myId
	neutralId := 2

	// width: Width of the board
	// height: Height of the board
	var width, height int
	fmt.Scan(&width, &height)

	var obstacles = make([][]bool, height)
	for i := 0; i < height; i++ {
		obstacles[i] = make([]bool, width)
	}
	for i := 0; i < height; i++ {
		// y: A y of the board: "." is empty, "x" is obstacle
		var y string
		fmt.Scan(&y)
		for j := 0; j < len(y); j++ {
			switch y[j] {
			case 'x':
				obstacles[i][j] = true
			case '.':
				obstacles[i][j] = false
			}
		}
	}

	for {
		var allUnits = make([]Unit, 0)
		var neutralCultists = make([]Unit, 0)
		var enemyCultists = make([]Unit, 0)
		var myCultists = make([]Unit, 0)
		var myLeader Unit
		var enemyLeader Unit

		// numOfUnits: The total number of units on the board
		var numOfUnits int
		fmt.Scan(&numOfUnits)

		for i := 0; i < numOfUnits; i++ {
			// unitId: The unit's ID
			// unitType: The unit's type: 0 = Cultist, 1 = Cult Leader
			// hp: Health points of the unit
			// x: X coordinate of the unit
			// y: Y coordinate of the unit
			// owner: id of owner player
			var unitId, unitType, hp, x, y, owner int
			fmt.Scan(&unitId, &unitType, &hp, &x, &y, &owner)

			unit := Unit{unitId, unitType, hp, x, y, owner}
			allUnits = append(allUnits, unit)
			switch owner {
			case myId:
				if unitType == 1 {
					myLeader = unit
				} else {
					myCultists = append(myCultists, unit)
				}
			case enemyId:
				if unitType == 1 {
					enemyLeader = unit
				} else {
					enemyCultists = append(enemyCultists, unit)
				}
			case neutralId:
				neutralCultists = append(neutralCultists, unit)
			}
		}

		enemyTeam := append(enemyCultists, enemyLeader)
		closestNeutral := findClosestUnit(myLeader, neutralCultists)
		closestDefender := findClosestUnit(enemyLeader, myCultists)
		closestAttacker, closestTarget, closestErr := findClosest(myCultists, enemyTeam)

		// fmt.Fprintln(os.Stderr, "Debug messages...")
		// WAIT | unitId MOVE x y | unitId SHOOT target| unitId CONVERT target
		pathingMap := make([][]bool, height)
		for i := 0; i < height; i++ {
			pathingMap[i] = make([]bool, width)
		}
		pathingMap = overlay(pathingMap, obstacles)
		//pathingMap = overlay(pathingMap, unitsToPathingMap(allUnits, height, width))
		if closestErr == nil && distance(closestAttacker, closestTarget) < 4 {
			fmt.Printf("%d %s %d\n", closestAttacker.id, "SHOOT", closestTarget.id)
		} else if pathFound(myLeader, closestNeutral, pathingMap) {
			fmt.Printf("%d %s %d\n", myLeader.id, "CONVERT", closestNeutral.id)
		} else if distance(closestDefender, enemyLeader) < 7 {
			fmt.Printf("%d %s %d\n", closestDefender.id, "SHOOT", enemyLeader.id)
		} else {
			fmt.Printf("%d %s %d %d\n", closestDefender.id, "MOVE", enemyLeader.x, enemyLeader.y)
		}

	}
}

func filterAttackers(attackingTeam, targetTeam []Unit) []Unit {
	var attackers = make([]Unit, 0)
	for _, attacker := range attackingTeam {
		for _, enemy := range targetTeam {
			if distance(attacker, enemy) < 7 {
				attackers = append(attackers, attacker)
			}
		}
	}
	return attackers
}

func findClosest(fromUnits []Unit, toUnits []Unit) (Unit, Unit, error) {
	if (len(fromUnits) == 0) || (len(toUnits) == 0) {
		return Unit{}, Unit{}, errors.New("no units to find closest")
	}
	var closestFrom, closestTo Unit
	var closestDistance = 9999
	for _, fromUnit := range fromUnits {
		for _, toUnit := range toUnits {
			distance := distance(fromUnit, toUnit)
			if distance < closestDistance {
				closestDistance = distance
				closestFrom = fromUnit
				closestTo = toUnit
			}
		}
	}
	return closestFrom, closestTo, nil
}

func findClosestUnit(fromUnit Unit, toUnits []Unit) Unit {
	var closestUnit Unit
	var closestDistance = 9999
	for _, toUnit := range toUnits {
		distance := distance(fromUnit, toUnit)
		if distance < closestDistance {
			closestDistance = distance
			closestUnit = toUnit
		}
	}
	return closestUnit
}

func distance(from Unit, to Unit) int {
	return int(math.Abs(float64(from.x-to.x)) + math.Abs(float64(from.y-to.y)))
}

func pathFound(fromUnit Unit, toUnit Unit, obstacles [][]bool) bool {
	traverseMap := floodFill(fromUnit, obstacles)
	return traverseMap[toUnit.y][toUnit.x] != 0
}

func floodFill(fromUnit Unit, obstacles [][]bool) [][]int {
	traverseMap := make([][]int, len(obstacles))
	for i := 0; i < len(obstacles); i++ {
		traverseMap[i] = make([]int, len(obstacles[i]))
	}

	queue := make([]Unit, 0)
	queue = append(queue, fromUnit)
	traverseMap[fromUnit.y][fromUnit.x] = 1

	for len(queue) > 0 {
		currentUnit := queue[0]
		queue = queue[1:]
		neighbours := availableMoves(currentUnit, obstacles)
		for _, neighbour := range neighbours {
			if traverseMap[neighbour.y][neighbour.x] == 0 {
				traverseMap[neighbour.y][neighbour.x] = traverseMap[currentUnit.y][currentUnit.x] + 1
				queue = append(queue, neighbour)
			}
		}
	}

	return traverseMap
}

func availableMoves(unit Unit, obstacles [][]bool) []Unit {
	neighbours := make([]Unit, 0)
	if unit.x > 0 && !obstacles[unit.y][unit.x-1] {
		neighbours = append(neighbours, Unit{unit.id, unit.unitType, unit.hp, unit.x - 1, unit.y, unit.owner})
	}
	if unit.x < len(obstacles[0])-1 && !obstacles[unit.y][unit.x+1] {
		neighbours = append(neighbours, Unit{unit.id, unit.unitType, unit.hp, unit.x + 1, unit.y, unit.owner})
	}
	if unit.y > 0 && !obstacles[unit.y-1][unit.x] {
		neighbours = append(neighbours, Unit{unit.id, unit.unitType, unit.hp, unit.x, unit.y - 1, unit.owner})
	}
	if unit.y < len(obstacles)-1 && !obstacles[unit.y+1][unit.x] {
		neighbours = append(neighbours, Unit{unit.id, unit.unitType, unit.hp, unit.x, unit.y + 1, unit.owner})
	}
	return neighbours
}

func overlay(map1 [][]bool, map2 [][]bool) [][]bool {
	for y := 0; y < len(map1); y++ {
		for x := 0; x < len(map1[y]); x++ {
			map1[y][x] = map1[y][x] || map2[y][x]
		}
	}
	return map1
}

func unitsToPathingMap(units []Unit, height, width int) [][]bool {
	pathingMap := make([][]bool, height)
	for i := 0; i < height; i++ {
		pathingMap[i] = make([]bool, width)
	}
	for _, unit := range units {
		pathingMap[unit.y][unit.x] = true
	}
	return pathingMap
}
