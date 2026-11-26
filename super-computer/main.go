package main

import (
	"fmt"
	"sort"
)

/**
 * Activity selection problem
 **/

func main() {
	var N int
	fmt.Scan(&N)

	jobs := make([][]int, N)
	for i := 0; i < N; i++ {
		var J, D int
		fmt.Scan(&J, &D)
		jobs[i] = []int{J, J + D - 1}
	}

	// sort by end time
	sort.Slice(jobs, func(i, j int) bool {
		return jobs[i][1] < jobs[j][1]
	})

	schedule := make([][]int, 0)
	schedule = append(schedule, jobs[0])
	for i := 1; i < N; i++ {
		if jobs[i][0] > schedule[len(schedule)-1][1] {
			schedule = append(schedule, jobs[i])
		}
	}
	//fmt.Fprintln(os.Stderr, jobs, schedule)
	fmt.Println(len(schedule))
}
