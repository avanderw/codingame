package main

import (
	"fmt"
	"os"
)

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/

func main() {
    var N int
    fmt.Scan(&N)
    
    for i := 0; i < N; i++ {
        var J, D int
        fmt.Scan(&J, &D)
        fmt.Fprintln(os.Stderr, J, D)
    }
    
    // fmt.Fprintln(os.Stderr, "Debug messages...")
    fmt.Println("answer")// Write answer to stdout
}