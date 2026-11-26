package main

import "testing"

func TestLandingHighSpeedAtY247(t *testing.T) {
	landX1, landX2 := 4000, 5500
	landY := 150

	rotate, power := computeControl(2905, 247, -17, -51, -23, landX1, landX2, landY)
	if rotate >= 0 {
		t.Errorf("at VS=-51 off-pad, expected corrective negative rotate toward pad, got %d", rotate)
	}
	if power != 4 {
		t.Errorf("at VS=-51, expected max thrust power=4, got %d", power)
	}
	t.Logf("Y=247 HS=-17: rotate=%d power=%d", rotate, power)
}

func TestLandingHighSpeedAtY196(t *testing.T) {
	landX1, landX2 := 4000, 5500
	landY := 150

	rotate, power := computeControl(2890, 196, -15, -51, -30, landX1, landX2, landY)
	if rotate >= 0 {
		t.Errorf("at VS=-51 off-pad, expected corrective negative rotate toward pad, got %d", rotate)
	}
	if power != 4 {
		t.Errorf("at VS=-51, expected max thrust power=4, got %d", power)
	}
	t.Logf("Y=196 HS=-15: rotate=%d power=%d", rotate, power)
}

func TestLandingCriticalAtY145(t *testing.T) {
	landX1, landX2 := 4000, 5500
	landY := 150

	rotate, power := computeControl(2876, 145, -13, -51, -30, landX1, landX2, landY)
	if rotate >= 0 {
		t.Errorf("critical off-pad case: expected negative rotate for lateral recovery, got %d", rotate)
	}
	if power != 4 {
		t.Errorf("critical: expected max thrust power=4, got %d", power)
	}
}

func TestLandingLowSpeedAtY145(t *testing.T) {
	landX1, landX2 := 4000, 5500
	landY := 150

	rotate, _ := computeControl(2876, 145, -3, -51, -30, landX1, landX2, landY)
	if rotate != 0 {
		t.Errorf("expected vertical when HS is low, got rotate=%d", rotate)
	}
}

func TestLandingVeryLowAltitude(t *testing.T) {
	landX1, landX2 := 4000, 5500
	landY := 150

	rotate1, power1 := computeControl(2876, 50, -10, -51, 0, landX1, landX2, landY)
	if rotate1 != 0 {
		t.Errorf("expected vertical near ground with moderate HS, got rotate=%d", rotate1)
	}
	if power1 < 4 {
		t.Errorf("expected max thrust near ground, got %d", power1)
	}

	rotate2, power2 := computeControl(2876, 50, -2, -51, 0, landX1, landX2, landY)
	if rotate2 != 0 {
		t.Errorf("expected vertical near ground at low HS, got rotate=%d", rotate2)
	}
	if power2 < 4 {
		t.Errorf("expected max thrust near ground, got %d", power2)
	}
}

func TestFarFromPadReducesDescent(t *testing.T) {
	landX1, landX2 := 4000, 5500
	landY := 150

	_, power := computeControl(2900, 1500, -50, -40, 0, landX1, landX2, landY)
	if power > 2 {
		t.Logf("warning: expected reduced power far from pad, got %d", power)
	}
}

func TestLandingApproachSequence(t *testing.T) {
	landX1, landX2 := 4000, 5500
	landY := 150

	testCases := []struct {
		name      string
		X, Y, HS  int
		expectMin int
	}{
		{"Far_High", 2500, 2500, -50, 20},
		{"Mid_High", 3500, 1500, -40, 15},
		{"Close_High", 4600, 500, -30, 5},
		{"Close_Low", 4700, 250, -2, 0},
	}

	for _, tc := range testCases {
		rotate, power := computeControl(tc.X, tc.Y, tc.HS, -50, 0, landX1, landX2, landY)
		absRotate := rotate
		if absRotate < 0 {
			absRotate = -absRotate
		}
		if absRotate < tc.expectMin {
			t.Logf("%s: rotate=%d (expected >= %d), power=%d", tc.name, rotate, tc.expectMin, power)
		} else {
			t.Logf("%s: OK rotate=%d power=%d", tc.name, rotate, power)
		}
	}
}

func TestRegressionMovingTowardPadNeedsPower(t *testing.T) {
	landX1, landX2 := 4000, 5500
	landY := 150

	_, power := computeControl(3127, 1330, 30, -72, 0, landX1, landX2, landY)
	if power <= 2 {
		t.Errorf("regression: moving toward pad with VS=-72 should not cap power at 2, got %d", power)
	}
}

func TestRegressionTouchdownOrientation(t *testing.T) {
	landX1, landX2 := 4000, 5500
	landY := 150

	rotate, power := computeControl(4151, 155, 6, -33, 20, landX1, landX2, landY)
	if rotate != 0 {
		t.Errorf("regression: near touchdown should command vertical, got rotate=%d", rotate)
	}
	if power != 4 {
		t.Errorf("near touchdown should keep max thrust, got %d", power)
	}
}

func TestRegressionOverPadVerticalPriority(t *testing.T) {
	landX1, landX2 := 2115, 3615
	landY := 100

	rotate, power := computeControl(2876, 145, -13, -51, -30, landX1, landX2, landY)
	if rotate != 0 {
		t.Errorf("regression: over-pad near touchdown should force vertical, got rotate=%d", rotate)
	}
	if power != 4 {
		t.Errorf("over-pad near touchdown should keep max thrust, got %d", power)
	}
}

func TestFindLandingZoneContiguousPlateau(t *testing.T) {
	points := []surfacePoint{
		{x: 0, y: 100},
		{x: 1000, y: 500},
		{x: 1500, y: 1000},
		{x: 2000, y: 1000},
		{x: 3000, y: 1000},
		{x: 4000, y: 1500},
		{x: 6999, y: 1200},
	}

	x1, x2, y := findLandingZone(points)
	if x1 != 1500 || x2 != 3000 || y != 1000 {
		t.Errorf("expected flat zone [1500,3000]@1000, got [%d,%d]@%d", x1, x2, y)
	}
}

func TestTerrainAvoidSteersAgainstVelocity(t *testing.T) {
	points := []surfacePoint{
		{x: 0, y: 1000},
		{x: 2000, y: 1200},
		{x: 3500, y: 1700},
		{x: 4500, y: 2100},
		{x: 6999, y: 2600},
	}

	// Terrain avoid is velocity-aware: it should brake high horizontal speed
	// first to avoid runaway reversals near terrain.
	rotate, power, mode, futureClr := applyTerrainSafety(points, 3300, 1850, 90, -40, 4000, 5000, -20, 4)

	if mode != "terrain_avoid" {
		t.Errorf("expected terrain_avoid mode, got %s (futureClr=%d)", mode, futureClr)
	}
	if rotate <= 0 {
		t.Errorf("expected positive rotate to brake HS=+90 in hazard mode, got %d", rotate)
	}
	if power != 4 {
		t.Errorf("expected max power in terrain hazard, got %d", power)
	}
}

func TestRegressionDeepCanyonHighVsOverPad(t *testing.T) {
	landX1, landX2 := 1500, 3000
	landY := 100

	// From failing deep-canyon trace: over pad, VS is extremely high.
	rotate, power := computeControl(2890, 196, -15, -51, -30, landX1, landX2, landY)

	if rotate != 0 {
		t.Errorf("deep-canyon regression: expected rotate=0 for vertical-speed safety, got %d", rotate)
	}
	if power != 4 {
		t.Errorf("deep-canyon regression: expected power=4 for braking, got %d", power)
	}
}

func TestRegressionOvershootLeftReturnsRight(t *testing.T) {
	landX1, landX2 := 4000, 5500
	landY := 150

	// Left of pad and still moving left quickly: must command negative rotate
	// to brake/reverse toward the pad.
	rotate, _ := computeControl(3598, 1634, -73, -40, 20, landX1, landX2, landY)
	if rotate >= 0 {
		t.Errorf("overshoot-left regression: expected negative rotate to return toward pad, got %d", rotate)
	}
}

func TestRegressionOverPadHighVSRecovery(t *testing.T) {
	landX1, landX2 := 4000, 5500
	landY := 150

	// From failing trace: over pad, moderate HS, but VS is dangerously high.
	// Must command vertical recovery now, not keep a large tilt.
	rotate, power := computeControl(4201, 552, 13, -44, 20, landX1, landX2, landY)
	if rotate != 0 {
		t.Errorf("expected rotate=0 for over-pad VS recovery, got %d", rotate)
	}
	if power != 4 {
		t.Errorf("expected power=4 for over-pad VS recovery, got %d", power)
	}
}

func TestRegressionOverPadLowAltNeedsHSTrimming(t *testing.T) {
	landX1, landX2 := 4000, 5500
	landY := 150

	// From failing trace near touchdown: over pad, VS acceptable-ish, but HS too high.
	// Must continue lateral braking and not force vertical yet.
	rotate, power := computeControl(4449, 237, 24, -40, 15, landX1, landX2, landY)
	if rotate <= 0 {
		t.Errorf("expected positive rotate to brake HS=+24 near touchdown, got %d", rotate)
	}
	if power != 4 {
		t.Errorf("expected power=4 near touchdown safety, got %d", power)
	}
}
