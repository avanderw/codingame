package main

import (
	"fmt"
	"os"
)

/**
 * Save the Planet.
 * Use less Fossil Fuel.
 **/

type surfacePoint struct {
	x int
	y int
}

func absInt(v int) int {
	if v < 0 {
		return -v
	}
	return v
}

func terrainHeightAtX(points []surfacePoint, x int) int {
	if len(points) == 0 {
		return 0
	}

	if x <= points[0].x {
		return points[0].y
	}

	for i := 1; i < len(points); i++ {
		p1 := points[i-1]
		p2 := points[i]
		if x <= p2.x {
			if p2.x == p1.x {
				return p2.y
			}
			dx := p2.x - p1.x
			num := (x - p1.x) * (p2.y - p1.y)
			return p1.y + num/dx
		}
	}

	return points[len(points)-1].y
}

func debugTerrain(points []surfacePoint) {
	fmt.Fprintln(os.Stderr, "[debug] Terrain segments:")
	for i := 1; i < len(points); i++ {
		p1 := points[i-1]
		p2 := points[i]
		flat := "slope"
		if p1.y == p2.y {
			flat = "flat"
		}
		fmt.Fprintf(os.Stderr, "[debug]   (%d,%d) -> (%d,%d) %s\n", p1.x, p1.y, p2.x, p2.y, flat)
	}
}

// estimateFutureMinClearance predicts the minimum terrain clearance in the next few seconds.
// The vertical forecast is conservative (ballistic-ish), which is useful for hazard detection.
func estimateFutureMinClearance(points []surfacePoint, X, Y, HS, VS int) int {
	minClearance := 1 << 30
	for t := 1; t <= 8; t++ {
		futureX := X + HS*t
		futureY := Y + VS*t - 2*t*t
		groundY := terrainHeightAtX(points, futureX)
		clearance := futureY - groundY
		if clearance < minClearance {
			minClearance = clearance
		}
	}
	return minClearance
}

// applyTerrainSafety overrides nominal commands when terrain collision risk is high.
func applyTerrainSafety(points []surfacePoint, X, Y, HS, VS, landX1, landX2 int, rotate, power int) (int, int, string, int) {
	overPad := X >= landX1 && X <= landX2
	groundY := terrainHeightAtX(points, X)
	clearance := Y - groundY
	futureMinClearance := estimateFutureMinClearance(points, X, Y, HS, VS)
	padBuffer := 350
	nearPad := X >= landX1-padBuffer && X <= landX2+padBuffer

	mode := "normal"
	hazard := !overPad && !nearPad && (futureMinClearance < 250 || clearance < 220)

	if hazard {
		mode = "terrain_avoid"
		power = 4

		absHS := absInt(HS)
		movingTowardPad := (X < landX1 && HS > 0) || (X > landX2 && HS < 0)
		highSpeedEmergency := absHS > 60

		if X < landX1 {
			if highSpeedEmergency || !movingTowardPad {
				// Brake current horizontal motion.
				if HS > 0 {
					rotate = 30
				} else {
					rotate = -30
				}
			} else {
				// Keep moving toward pad/right when it is already helping.
				rotate = -25
			}
		} else if X > landX2 {
			if highSpeedEmergency || !movingTowardPad {
				// Brake current horizontal motion.
				if HS > 0 {
					rotate = 30
				} else {
					rotate = -30
				}
			} else {
				// Keep moving toward pad/left when it is already helping.
				rotate = 25
			}
		} else {
			// Over/near pad in terrain hazard: brake lateral motion.
			if HS > 0 {
				rotate = 30
			} else if HS < 0 {
				rotate = -30
			} else {
				rotate = 0
			}
		}

		if VS <= -45 {
			if rotate > 30 {
				rotate = 30
			} else if rotate < -30 {
				rotate = -30
			}
		} else if VS <= -40 {
			if rotate > 35 {
				rotate = 35
			} else if rotate < -35 {
				rotate = -35
			}
		}
	}

	// Near-pad capture: once close to pad laterally, prioritize entering and
	// staying over the flat zone instead of continuing terrain-escape behavior.
	if !overPad && nearPad {
		mode = "near_pad_capture"
		if power < 4 {
			power = 4
		}

		if X < landX1 {
			dist := landX1 - X
			targetHS := dist / 18
			if targetHS < 8 {
				targetHS = 8
			} else if targetHS > 30 {
				targetHS = 30
			}
			rotate = (HS - targetHS) * 2
		} else if X > landX2 {
			dist := X - landX2
			targetHS := -(dist / 18)
			if targetHS > -8 {
				targetHS = -8
			} else if targetHS < -30 {
				targetHS = -30
			}
			rotate = (HS - targetHS) * 2
		}

		if rotate > 35 {
			rotate = 35
		} else if rotate < -35 {
			rotate = -35
		}

		if VS <= -40 {
			if rotate > 25 {
				rotate = 25
			} else if rotate < -25 {
				rotate = -25
			}
		}
	}

	if rotate > 45 {
		rotate = 45
	} else if rotate < -45 {
		rotate = -45
	}

	if power < 0 {
		power = 0
	} else if power > 4 {
		power = 4
	}

	return rotate, power, mode, futureMinClearance
}

// findLandingZone returns the widest contiguous flat plateau.
func findLandingZone(points []surfacePoint) (int, int, int) {
	if len(points) < 2 {
		return 0, 0, 0
	}

	bestX1, bestX2, bestY := 0, 0, 0
	bestWidth := -1

	inFlat := false
	flatStartX := 0
	flatY := 0

	for i := 1; i < len(points); i++ {
		prev := points[i-1]
		curr := points[i]

		if curr.y == prev.y {
			if !inFlat {
				inFlat = true
				flatStartX = prev.x
				flatY = curr.y
			}
		} else if inFlat {
			width := prev.x - flatStartX
			if width > bestWidth {
				bestWidth = width
				bestX1 = flatStartX
				bestX2 = prev.x
				bestY = flatY
			}
			inFlat = false
		}
	}

	if inFlat {
		lastX := points[len(points)-1].x
		width := lastX - flatStartX
		if width > bestWidth {
			bestX1 = flatStartX
			bestX2 = lastX
			bestY = flatY
		}
	}

	return bestX1, bestX2, bestY
}

// computeControl calculates the desired rotation and power based on current state.
func computeControl(X, Y, HS, VS, R int, landX1, landX2, landY int) (int, int) {
	_ = R
	altitudeAboveLanding := Y - landY
	overPad := X >= landX1 && X <= landX2
	absHorizontalSpeed := absInt(HS)

	distanceToPad := 0
	if X < landX1 {
		distanceToPad = X - landX1
	} else if X > landX2 {
		distanceToPad = X - landX2
	}

	maxTargetHS := 15
	if altitudeAboveLanding > 2200 {
		maxTargetHS = 80
	} else if altitudeAboveLanding > 1500 {
		maxTargetHS = 60
	} else if altitudeAboveLanding > 900 {
		maxTargetHS = 45
	} else if altitudeAboveLanding > 500 {
		maxTargetHS = 30
	}

	targetHS := 0
	if distanceToPad != 0 {
		targetHS = -distanceToPad / 25
		if targetHS > maxTargetHS {
			targetHS = maxTargetHS
		} else if targetHS < -maxTargetHS {
			targetHS = -maxTargetHS
		}
	}

	rotate := (HS - targetHS) * 2
	if rotate > 45 {
		rotate = 45
	} else if rotate < -45 {
		rotate = -45
	}

	if overPad && altitudeAboveLanding < 180 {
		if absHorizontalSpeed > 20 {
			if HS > 0 {
				rotate = 30
			} else {
				rotate = -30
			}
		} else {
			rotate = 0
		}
	}

	if overPad && altitudeAboveLanding < 700 && absHorizontalSpeed <= 20 {
		rotate = 0
	}

	// Over-pad high-HS recovery: start killing lateral speed earlier.
	if overPad && altitudeAboveLanding < 900 && absHorizontalSpeed > 25 {
		if HS > 0 {
			rotate = 30
		} else {
			rotate = -30
		}
	}

	if altitudeAboveLanding < 170 && absHorizontalSpeed <= 8 {
		rotate = 0
	}

	if altitudeAboveLanding < 90 && absHorizontalSpeed <= 10 {
		rotate = 0
	}

	// Vertical emergency handling: when VS is too negative, reduce tilt so thrust
	// contributes more to vertical braking before touchdown becomes unrecoverable.
	if VS <= -48 {
		if overPad {
			if absHorizontalSpeed <= 20 {
				rotate = 0
			} else {
				if rotate > 8 {
					rotate = 8
				} else if rotate < -8 {
					rotate = -8
				}
			}
		} else {
			if X < landX1 {
				rotate = -20
			} else if X > landX2 {
				rotate = 20
			} else {
				rotate = 0
			}
		}
	} else if VS <= -44 {
		if overPad && absHorizontalSpeed <= 20 {
			rotate = 0
		} else if !overPad {
			if rotate > 25 {
				rotate = 25
			} else if rotate < -25 {
				rotate = -25
			}
		} else if absHorizontalSpeed <= 35 {
			if rotate > 12 {
				rotate = 12
			} else if rotate < -12 {
				rotate = -12
			}
		} else {
			if rotate > 15 {
				rotate = 15
			} else if rotate < -15 {
				rotate = -15
			}
		}
	} else if VS <= -40 {
		if overPad {
			if rotate > 20 {
				rotate = 20
			} else if rotate < -20 {
				rotate = -20
			}
		} else {
			if rotate > 30 {
				rotate = 30
			} else if rotate < -30 {
				rotate = -30
			}
		}
	}

	// Final near-ground posture guard: keep vertical when low and HS is manageable.
	if altitudeAboveLanding < 170 && absHorizontalSpeed <= 10 {
		rotate = 0
	}

	if altitudeAboveLanding < 90 && absHorizontalSpeed <= 8 {
		rotate = 0
	}

	targetVS := -34
	if distanceToPad != 0 || absHorizontalSpeed > 25 {
		targetVS = -24
	} else if absHorizontalSpeed > 15 {
		targetVS = -28
	} else if altitudeAboveLanding < 250 {
		targetVS = -22
	} else if altitudeAboveLanding < 700 {
		targetVS = -26
	}

	power := 2
	if VS < targetVS-10 {
		power = 4
	} else if VS < targetVS-4 {
		power = 3
	} else if VS > targetVS+3 {
		power = 1
	}

	if VS <= -42 {
		power = 4
	}

	if altitudeAboveLanding < 300 {
		if power < 4 {
			power = 4
		}
	}

	if absInt(rotate) > 30 && power < 3 {
		power = 3
	}

	if power < 0 {
		power = 0
	} else if power > 4 {
		power = 4
	}

	return rotate, power
}

func main() {
	// N: the number of points used to draw the surface of Mars.
	var N int
	fmt.Scan(&N)

	points := make([]surfacePoint, 0, N)

	for i := 0; i < N; i++ {
		// landX: X coordinate of a surface point. (0 to 6999)
		// landY: Y coordinate of a surface point. By linking all the points together in a sequential fashion, you form the surface of Mars.
		var landX, currentY int
		fmt.Scan(&landX, &currentY)
		points = append(points, surfacePoint{x: landX, y: currentY})
	}

	landX1, landX2, landY := findLandingZone(points)

	landingCenter := (landX1 + landX2) / 2
	fmt.Fprintf(os.Stderr, "[debug] Landing pad: x1=%d x2=%d y=%d center=%d\n", landX1, landX2, landY, landingCenter)
	debugTerrain(points)

	firstTurn := true

	for {
		// HS: the horizontal speed (in m/s), can be negative.
		// VS: the vertical speed (in m/s), can be negative.
		// F: the quantity of remaining fuel in liters.
		// R: the rotation angle in degrees (-90 to 90).
		// P: the thrust power (0 to 4).
		var X, Y, HS, VS, F, R, P int
		fmt.Scan(&X, &Y, &HS, &VS, &F, &R, &P)

		if firstTurn {
			fmt.Fprintf(os.Stderr, "[debug] Start state: X=%d Y=%d HS=%d VS=%d Fuel=%d Angle=%d Power=%d\n", X, Y, HS, VS, F, R, P)
			firstTurn = false
		}

		rotate, power := computeControl(X, Y, HS, VS, R, landX1, landX2, landY)
		rotate, power, mode, futureMinClearance := applyTerrainSafety(points, X, Y, HS, VS, landX1, landX2, rotate, power)

		groundY := terrainHeightAtX(points, X)
		clearance := Y - groundY
		padDistance := X - landingCenter
		fmt.Fprintf(os.Stderr, "[debug] turn: X=%d Y=%d HS=%d VS=%d fuel=%d angle=%d pwr=%d cmd=(%d,%d) groundY=%d clr=%d futureClr=%d distToPad=%d mode=%s\n", X, Y, HS, VS, F, R, P, rotate, power, groundY, clearance, futureMinClearance, padDistance, mode)

		fmt.Printf("%d %d\n", rotate, power)
	}
}
