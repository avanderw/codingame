/**
 * SameGame solver.
 *
 * Strategy: on the first turn (20 s budget) run iterated biased-greedy Monte
 * Carlo rollouts to find a good complete sequence of moves, then execute that
 * sequence one move per turn.  If the pre-planned move is unexpectedly invalid
 * (simulation mismatch) fall back to greedy (pick the largest region).
 *
 * Board layout (internal):
 *   grid[row * COLS + col], row 0 = bottom, row 14 = top.
 * Input lines arrive top-to-bottom (row 14 first, row 0 last).
 * Output: "col row [optional message]"  (column then row, 0-indexed).
 */

declare function readline(): string;

const ROWS = 15;
const COLS = 15;
const SIZE = ROWS * COLS;

type Grid = Int8Array;

// ---------------------------------------------------------------------------
// Board I/O
// ---------------------------------------------------------------------------

function readGrid(): Grid {
    const grid = new Int8Array(SIZE);
    for (let i = 0; i < ROWS; i++) {
        const row = ROWS - 1 - i; // first input line = row 14 (top)
        const line = readline().split(' ');
        for (let c = 0; c < COLS; c++) {
            grid[row * COLS + c] = parseInt(line[c]);
        }
    }
    return grid;
}

// ---------------------------------------------------------------------------
// Region finding  (BFS with pre-allocated arrays to reduce GC pressure)
// ---------------------------------------------------------------------------

const _vis = new Uint8Array(SIZE);
const _stk = new Int32Array(SIZE);

/**
 * Returns [startRow, startCol, size] for every connected region of size >= 2.
 * Uses the global _vis / _stk buffers — must not be called re-entrantly.
 */
function getAllRegions(grid: Grid): Array<[number, number, number]> {
    _vis.fill(0);
    const result: Array<[number, number, number]> = [];

    for (let startR = 0; startR < ROWS; startR++) {
        for (let startC = 0; startC < COLS; startC++) {
            const startIdx = startR * COLS + startC;
            if (_vis[startIdx] || grid[startIdx] === -1) continue;

            const color = grid[startIdx];
            let count = 0;
            let top = 0;
            _stk[top++] = startIdx;
            _vis[startIdx] = 1;

            while (top > 0) {
                const idx = _stk[--top];
                count++;
                const r = (idx / COLS) | 0;
                const c = idx % COLS;
                let ni: number;

                if (r > 0) {
                    ni = idx - COLS;
                    if (!_vis[ni] && grid[ni] === color) { _vis[ni] = 1; _stk[top++] = ni; }
                }
                if (r < ROWS - 1) {
                    ni = idx + COLS;
                    if (!_vis[ni] && grid[ni] === color) { _vis[ni] = 1; _stk[top++] = ni; }
                }
                if (c > 0) {
                    ni = idx - 1;
                    if (!_vis[ni] && grid[ni] === color) { _vis[ni] = 1; _stk[top++] = ni; }
                }
                if (c < COLS - 1) {
                    ni = idx + 1;
                    if (!_vis[ni] && grid[ni] === color) { _vis[ni] = 1; _stk[top++] = ni; }
                }
            }

            if (count >= 2) result.push([startR, startC, count]);
        }
    }

    return result;
}

/** Returns all cell indices belonging to the region connected to (row, col). */
function findRegion(grid: Grid, startRow: number, startCol: number): number[] {
    const startIdx = startRow * COLS + startCol;
    const color = grid[startIdx];
    if (color === -1) return [];

    const cells: number[] = [];
    const vis = new Uint8Array(SIZE);
    const stk: number[] = [startIdx];
    vis[startIdx] = 1;

    while (stk.length > 0) {
        const idx = stk.pop()!;
        cells.push(idx);
        const r = (idx / COLS) | 0;
        const c = idx % COLS;
        let ni: number;

        if (r > 0) { ni = idx - COLS; if (!vis[ni] && grid[ni] === color) { vis[ni] = 1; stk.push(ni); } }
        if (r < ROWS - 1) { ni = idx + COLS; if (!vis[ni] && grid[ni] === color) { vis[ni] = 1; stk.push(ni); } }
        if (c > 0) { ni = idx - 1; if (!vis[ni] && grid[ni] === color) { vis[ni] = 1; stk.push(ni); } }
        if (c < COLS - 1) { ni = idx + 1; if (!vis[ni] && grid[ni] === color) { vis[ni] = 1; stk.push(ni); } }
    }

    return cells;
}

// ---------------------------------------------------------------------------
// Physics: gravity + column compaction
// ---------------------------------------------------------------------------

function applyGravity(grid: Grid): void {
    // Step 1: cells fall to the bottom of each column.
    for (let c = 0; c < COLS; c++) {
        let wr = 0;
        for (let r = 0; r < ROWS; r++) {
            const v = grid[r * COLS + c];
            if (v !== -1) {
                if (wr !== r) {
                    grid[wr * COLS + c] = v;
                    grid[r * COLS + c] = -1;
                }
                wr++;
            }
        }
    }

    // Step 2: empty columns are filled by shifting remaining columns left.
    let wc = 0;
    for (let c = 0; c < COLS; c++) {
        if (grid[c] !== -1) { // row 0 non-empty ⇒ column is non-empty after gravity
            if (wc !== c) {
                for (let r = 0; r < ROWS; r++) {
                    grid[r * COLS + wc] = grid[r * COLS + c];
                    grid[r * COLS + c] = -1;
                }
            }
            wc++;
        }
    }
}

// ---------------------------------------------------------------------------
// Move application
// ---------------------------------------------------------------------------

/**
 * Removes the region containing (row, col), applies gravity, and returns the
 * move score = (n-2)^2.  Returns -1 if the move is illegal (size < 2).
 */
function applyMove(grid: Grid, row: number, col: number): number {
    const region = findRegion(grid, row, col);
    if (region.length < 2) return -1;
    for (const idx of region) grid[idx] = -1;
    applyGravity(grid);
    const n = region.length;
    return (n - 2) * (n - 2);
}

function isEmpty(grid: Grid): boolean {
    return grid[0] === -1;
}

// ---------------------------------------------------------------------------
// Monte Carlo simulation
// ---------------------------------------------------------------------------

/**
 * Runs a full game simulation on a COPY of grid.
 *
 * noiseLevel ∈ [0, 1]:
 *   0   → pure greedy (always pick the largest region)
 *   >0  → with probability noiseLevel pick a region weighted by size²,
 *          otherwise pick the largest region.
 *
 * Returns [totalScore, moveSequence].
 */
function simulate(grid: Grid, noiseLevel: number): [number, Array<[number, number]>] {
    const g = grid.slice() as Grid;
    const moves: Array<[number, number]> = [];
    let score = 0;

    while (true) {
        const regions = getAllRegions(g);
        if (regions.length === 0) break;

        let selected: [number, number, number];

        if (regions.length === 1 || Math.random() >= noiseLevel) {
            // Greedy: largest region
            selected = regions[0];
            for (const r of regions) if (r[2] > selected[2]) selected = r;
        } else {
            // Weighted random: favour larger regions (weight = size²)
            let total = 0;
            for (const r of regions) total += r[2] * r[2];
            let rnd = Math.random() * total;
            selected = regions[regions.length - 1]; // fallback
            for (const r of regions) {
                rnd -= r[2] * r[2];
                if (rnd <= 0) { selected = r; break; }
            }
        }

        moves.push([selected[0], selected[1]]);
        score += applyMove(g, selected[0], selected[1]);
    }

    if (isEmpty(g)) score += 1000;
    return [score, moves];
}

// ---------------------------------------------------------------------------
// Planner: iterated random-restart Monte Carlo
// ---------------------------------------------------------------------------

function plan(grid: Grid, ms: number): Array<[number, number]> {
    const deadline = Date.now() + ms - 200;

    // Greedy baseline
    let [bestScore, bestMoves] = simulate(grid, 0);
    let iters = 0;

    while (Date.now() < deadline) {
        // Vary noise so we explore a spectrum from near-greedy to more random.
        const noise = 0.05 + Math.random() * 0.65;
        const [score, moves] = simulate(grid, noise);
        if (score > bestScore) {
            bestScore = score;
            bestMoves = moves.slice();
        }
        iters++;
    }

    console.error(`Planned ${iters} rollouts, best estimated score: ${bestScore}`);
    return bestMoves;
}

// ---------------------------------------------------------------------------
// Main game loop
// ---------------------------------------------------------------------------

let queue: Array<[number, number]> = [];
let first = true;

while (true) {
    const grid = readGrid();

    if (first) {
        first = false;
        queue = plan(grid, 18500); // use almost all of the 20 s first-turn budget
    }

    // Find the next valid move from the pre-planned sequence.
    let row = -1, col = -1;

    while (queue.length > 0) {
        const [r, c] = queue.shift()!;
        // Verify the cell still exists and belongs to a removable region
        // (guards against simulation/server mismatch).
        if (grid[r * COLS + c] !== -1 && findRegion(grid, r, c).length >= 2) {
            row = r;
            col = c;
            break;
        }
    }

    // Fallback: greedy (pick largest available region).
    if (row === -1) {
        const regions = getAllRegions(grid);
        if (regions.length > 0) {
            let best = regions[0];
            for (const r of regions) if (r[2] > best[2]) best = r;
            row = best[0];
            col = best[1];
        }
    }

    // Output format: "col row"
    console.log(`${col} ${row}`);
}
