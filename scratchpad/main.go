package main

import (
	"bufio"
	"fmt"
	"os"
	"strings"
)

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/

func main() {
	scanner := bufio.NewScanner(os.Stdin)
	scanner.Buffer(make([]byte, 1000000), 1000000)

	scanner.Scan()
	name := scanner.Text()
	name = strings.ToUpper(name)

	// fmt.Fprintln(os.Stderr, "Debug messages...")
	replace := []string{"Alpha", "Bravo", "Charlie", "Delta", "Echo", "Foxtrot", "Golf", "Hotel", "India", "Juliett", "Kilo", "Lima", "Mike", "November", "Oscar", "Papa", "Quebec", "Romeo", "Sierra", "Tango", "Uniform", "Victor", "Whiskey", "X-ray", "Yankee", "Zulu"}
	alpha := []string{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"}
	for i := 0; i < len(name); i++ {
		for j := 0; j < len(alpha); j++ {
			if string(name[i]) == alpha[j] {
				fmt.Print(replace[j])
			}
		}
		if i < len(name)-1 {
			fmt.Print(" ")
		}
	}
	fmt.Println()
}
