package main

import "fmt"

/**
 * Save humans, destroy zombies!
 **/

func main() {
	for {
		var x, y int
		fmt.Scan(&x, &y)

		var humanCount int
		fmt.Scan(&humanCount)
		humans := make([][]int, humanCount)
		for i := 0; i < humanCount; i++ {
			var humanId, humanX, humanY int
			fmt.Scan(&humanId, &humanX, &humanY)
			humans[i] = []int{humanId, humanX, humanY}
		}

		var zombieCount int
		fmt.Scan(&zombieCount)
		zombies := make([][]int, zombieCount)
		for i := 0; i < zombieCount; i++ {
			var zombieId, zombieX, zombieY, zombieXNext, zombieYNext int
			fmt.Scan(&zombieId, &zombieX, &zombieY, &zombieXNext, &zombieYNext)
			zombies[i] = []int{zombieId, zombieX, zombieY, zombieXNext, zombieYNext}
		}

		fmt.Println("0 0") // Your destination coordinates
	}
}
