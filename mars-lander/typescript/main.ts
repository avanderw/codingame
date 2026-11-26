/**
 * Save the Planet.
 * Use less Fossil Fuel.
 **/

interface Point {
    x: number;
    y: number;
}

interface LandingSite {
    x1: number;
    x2: number;
    y: number;
    midX: number;
}

function findLandingSite(surface: Point[]): LandingSite {
    for (let i = 0; i < surface.length - 1; i++) {
        if (surface[i].y === surface[i + 1].y && surface[i + 1].x - surface[i].x >= 1000) {
            return {
                x1: surface[i].x,
                x2: surface[i + 1].x,
                y: surface[i].y,
                midX: Math.floor((surface[i].x + surface[i + 1].x) / 2),
            };
        }
    }
    throw new Error('No landing site found');
}

const surface: Point[] = [];
const N: number = parseInt(readline()); // the number of points used to draw the surface of Mars.
for (let i = 0; i < N; i++) {
    var inputs: string[] = readline().split(' ');
    const landX: number = parseInt(inputs[0]); // X coordinate of a surface point. (0 to 6999)
    const landY: number = parseInt(inputs[1]); // Y coordinate of a surface point. By linking all the points together in a sequential fashion, you form the surface of Mars.
    surface.push({ x: landX, y: landY });
}

/**
 * Returns the highest Y value on the surface between two X coordinates.
 * Interpolates at range boundaries to handle wide segments correctly.
 */
function findHighestPointBetween(surface: Point[], fromX: number, toX: number): number {
    const minX = Math.min(fromX, toX);
    const maxX = Math.max(fromX, toX);
    let maxY = 0;

    for (let i = 0; i < surface.length - 1; i++) {
        const a = surface[i];
        const b = surface[i + 1];
        if (b.x <= minX || a.x >= maxX) continue;

        // Collect X positions to sample within the overlapping range
        const checkXs: number[] = [];
        if (a.x >= minX) checkXs.push(a.x);
        if (b.x <= maxX) checkXs.push(b.x);
        if (a.x < minX)  checkXs.push(minX); // interpolate at left boundary
        if (b.x > maxX)  checkXs.push(maxX); // interpolate at right boundary

        for (const cx of checkXs) {
            const t = (cx - a.x) / (b.x - a.x);
            const y = a.y + t * (b.y - a.y);
            maxY = Math.max(maxY, y);
        }
    }

    return maxY;
}

function clamp(val: number, min: number, max: number): number {
    return Math.max(min, Math.min(max, val));
}

const landingSite: LandingSite = findLandingSite(surface);
console.error(`Landing site: x1=${landingSite.x1}, x2=${landingSite.x2}, y=${landingSite.y}, midX=${landingSite.midX}`);

// game loop
while (true) {
    var inputs: string[] = readline().split(' ');
    const X: number = parseInt(inputs[0]);
    const Y: number = parseInt(inputs[1]);
    const HS: number = parseInt(inputs[2]); // the horizontal speed (in m/s), can be negative.
    const VS: number = parseInt(inputs[3]); // the vertical speed (in m/s), can be negative.
    const F: number = parseInt(inputs[4]); // the quantity of remaining fuel in liters.
    const R: number = parseInt(inputs[5]); // the rotation angle in degrees (-90 to 90).
    const P: number = parseInt(inputs[6]); // the thrust power (0 to 4).

    const highestY      = findHighestPointBetween(surface, X, landingSite.midX);
    const dX            = landingSite.midX - X;   // positive = landing site is to the right
    const altAboveObs   = Y - highestY;            // clearance above the highest obstacle
    const altAbovePad   = Y - landingSite.y;       // height above the landing pad surface
    const overPad       = X >= landingSite.x1 && X <= landingSite.x2;

    let targetAngle: number;
    let targetPower: number;

    if (!overPad && altAboveObs < 300) {
        // ── PHASE 1: CLEAR ──────────────────────────────────────────────────────
        const maxClearAngle = altAboveObs < 100
            ? 20
            : clamp(Math.abs(HS) * 0.5, 20, 60);  // was 0.4/50 — steeper at high speed
        targetAngle = clamp(HS * 2, -maxClearAngle, maxClearAngle);
        targetPower = 4;

    } else if (!overPad) {
        // ── PHASE 2: APPROACH ───────────────────────────────────────────────────
        const maxDecel  = 2.0;
        const safeSpeed = Math.sqrt(2 * maxDecel * Math.abs(dX));
        // Cap safeSpeed at 60 (was 80) — commit to braking sooner.
        const targetHS  = dX >= 0 ? Math.min(safeSpeed, 60) : Math.max(-safeSpeed, -60);
        const hsError   = targetHS - HS;

        const braking  = (HS > 0 && HS > targetHS) || (HS < 0 && HS < targetHS);
        // Scale cap with |HS|; allow up to 90° at extreme speeds (pure horizontal thrust).
        // At HS=62 → 50°, HS=88 → 70°, HS=113 → 90°.
        const maxAngle = braking ? clamp(Math.abs(HS) * 0.8, 50, 90) : 45;
        targetAngle    = clamp(-hsError * 1.5, -maxAngle, maxAngle);

        // Always use power=4 when braking steeply — vertical component collapses at 90°.
        const absAngle = Math.abs(targetAngle);
        targetPower = (absAngle > 30 || VS < -30 || altAbovePad < 500) ? 4 : 3;

    } else {
        // ── PHASE 3: LAND ───────────────────────────────────────────────────────
        // Over the pad — bleed HS as aggressively as altitude allows, same
        // speed-scaled logic as APPROACH, tightening only near the ground.
        const hsError  = -HS; // want HS → 0
        const braking  = Math.abs(HS) > 1;
        // High above pad: same aggressive scaling as APPROACH (up to 80°).
        // Mid-range: tighten cap as we descend toward touchdown.
        // Close: must be near-vertical for a legal landing (angle = 0 required).
        let maxAngle: number;
        if (altAbovePad > 300) {
            maxAngle = braking ? clamp(Math.abs(HS) * 0.8, 30, 80) : 30;
        } else if (altAbovePad > 100) {
            maxAngle = braking ? clamp(Math.abs(HS) * 0.6, 15, 45) : 15;
        } else if (altAbovePad > 50) {
            maxAngle = clamp(Math.abs(HS) * 0.4, 5, 20);
        } else {
            maxAngle = 0; // final metres: perfectly vertical
        }

        targetAngle = clamp(-hsError * 1.5, -maxAngle, maxAngle);

        const absAngle = Math.abs(targetAngle);
        // Keep VS in check alongside horizontal braking.
        if (absAngle > 30 || VS < -35) targetPower = 4;
        else if (VS > -15 && absAngle < 10) targetPower = 2;
        else targetPower = 3;
    }

    const finalAngle = Math.round(clamp(targetAngle, -90, 90));
    const finalPower = Math.round(clamp(targetPower, 0, 4));

    const phase = (!overPad && altAboveObs < 300) ? 'CLEAR' : overPad ? 'LAND' : 'APPROACH';
    console.error(`[${phase}] dX=${dX} altObs=${altAboveObs.toFixed(0)} altPad=${altAbovePad.toFixed(0)} VS=${VS} HS=${HS} → ${finalAngle} ${finalPower}`);
    console.log(`${finalAngle} ${finalPower}`);
}
