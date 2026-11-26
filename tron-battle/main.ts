const W = 30, H = 20;

const DX = [0, 0, -1, 1];  // UP DOWN LEFT RIGHT
const DY = [-1, 1, 0, 0];
const DIR_NAME = ['UP', 'DOWN', 'LEFT', 'RIGHT'];

// ── Pre-allocated buffers for voronoi (leaf-only calls – never nested) ────────
const _vOwner = new Uint8Array(W * H);
const _vDist  = new Uint16Array(W * H);
const _vQPos  = new Int32Array(W * H);
const _vQOwn  = new Uint8Array(W * H);
const _vQDst  = new Uint16Array(W * H);

const V_NONE = 255;  // Uint8 sentinel; player indices 0..N-1 are valid owners, N = TIE

type Pos = [number, number] | null;  // null means the player is dead

/**
 * Multi-player simultaneous BFS (Voronoi diagram).
 * Seeds one BFS wavefront per alive player; assigns each free cell to the
 * nearest player (ties are marked contested and not counted for anyone).
 * Score = my territory − Σ opponents' territory.
 */
function voronoiN(g: boolean[][], pos: Pos[], P: number): number {
    const N = pos.length;
    const TIE = N;            // contested sentinel (fits in a Uint8 for N ≤ 4)
    _vOwner.fill(V_NONE);
    _vDist.fill(0xFFFF);
    let qH = 0, qT = 0;

    for (let i = 0; i < N; i++) {
        if (pos[i] === null) continue;
        const [x, y] = pos[i]!;
        if (x < 0 || x >= W || y < 0 || y >= H) continue;
        const p = y * W + x;
        // No blocked-cell guard: head IS on a trail cell, but BFS seeds from it
        // and expands only into free neighbours via the g[ny][nx] check below.
        if (_vOwner[p] === V_NONE) { _vOwner[p] = i; _vDist[p] = 0; }
        else if (_vOwner[p] !== i)  { _vOwner[p] = TIE; }
        _vQPos[qT] = p; _vQOwn[qT] = i; _vQDst[qT] = 0; qT++;
    }

    while (qH < qT) {
        const p = _vQPos[qH], pl = _vQOwn[qH], d = _vQDst[qH];
        qH++;
        const cx = p % W, cy = (p / W) | 0;
        for (let di = 0; di < 4; di++) {
            const nx = cx + DX[di], ny = cy + DY[di];
            if (nx < 0 || nx >= W || ny < 0 || ny >= H || g[ny][nx]) continue;
            const np = ny * W + nx, nd = d + 1;
            if (_vOwner[np] === V_NONE) {
                _vOwner[np] = pl; _vDist[np] = nd;
                _vQPos[qT] = np; _vQOwn[qT] = pl; _vQDst[qT] = nd; qT++;
            } else if (_vOwner[np] !== pl && _vOwner[np] !== TIE && _vDist[np] === nd) {
                _vOwner[np] = TIE;
            }
        }
    }

    // Count only FREE cells (skip trail/head cells and contested cells).
    const counts = new Int32Array(N);
    for (let y = 0; y < H; y++) {
        for (let x = 0; x < W; x++) {
            if (g[y][x]) continue;
            const o = _vOwner[y * W + x];
            if (o < N) counts[o]++;
        }
    }
    let score = counts[P];
    for (let i = 0; i < N; i++) if (i !== P) score -= counts[i];
    return score;
}

/** BFS flood-fill: count cells reachable from (sx, sy). */
function floodFill(g: boolean[][], sx: number, sy: number): number {
    // No blocked check for the source – the head IS on an occupied cell but we
    // start the BFS from it and count only its free neighbors onward.
    if (sx < 0 || sx >= W || sy < 0 || sy >= H) return 0;
    const vis = new Uint8Array(W * H);
    const q: number[] = [sy * W + sx];
    vis[sy * W + sx] = 1;
    let cnt = 0, qi = 0;
    while (qi < q.length) {
        const p = q[qi++]; cnt++;
        const cx = p % W, cy = (p / W) | 0;
        for (let d = 0; d < 4; d++) {
            const nx = cx + DX[d], ny = cy + DY[d];
            if (nx < 0 || nx >= W || ny < 0 || ny >= H || g[ny][nx]) continue;
            const np = ny * W + nx;
            if (!vis[np]) { vis[np] = 1; q.push(np); }
        }
    }
    return cnt;
}

/**
 * True when ALL alive opponents are unreachable from my head.
 * Temporarily unmarks every head so the BFS can pass through them.
 */
function allSeparated(g: boolean[][], pos: Pos[], P: number): boolean {
    if (pos[P] === null) return true;
    const [mx, my] = pos[P]!;
    const opps: [number, number][] = [];
    for (let i = 0; i < pos.length; i++) {
        if (i !== P && pos[i] !== null) opps.push(pos[i]!);
    }
    if (opps.length === 0) return true;

    // Temporarily unmark all heads for the connectivity BFS.
    g[my][mx] = false;
    for (const [ox, oy] of opps) g[oy][ox] = false;

    const vis = new Uint8Array(W * H);
    const q: number[] = [my * W + mx];
    vis[my * W + mx] = 1;
    const targets = new Set(opps.map(([ox, oy]) => oy * W + ox));
    let qi = 0, found = false;
    while (qi < q.length && !found) {
        const p = q[qi++];
        if (targets.has(p)) { found = true; break; }
        const cx = p % W, cy = (p / W) | 0;
        for (let d = 0; d < 4; d++) {
            const nx = cx + DX[d], ny = cy + DY[d];
            if (nx < 0 || nx >= W || ny < 0 || ny >= H || g[ny][nx]) continue;
            const np = ny * W + nx;
            if (!vis[np]) { vis[np] = 1; q.push(np); }
        }
    }

    g[my][mx] = true;
    for (const [ox, oy] of opps) g[oy][ox] = true;
    return !found;
}

/** Next alive player index after `from` (wraps; returns `from` if only one left). */
function nextAlive(pos: Pos[], from: number): number {
    const N = pos.length;
    let t = (from + 1) % N;
    for (let i = 0; i < N; i++, t = (t + 1) % N) {
        if (pos[t] !== null) return t;
    }
    return from;  // everyone dead or only self remains
}

const SEARCH_DEPTH = 6;

/**
 * Paranoid minimax with alpha-beta pruning – works for 2 or 3 players.
 *
 * "Paranoid" means: my turn → I maximise; every opponent's turn → they
 * minimise my score (i.e., they collaborate against me).  Alpha-beta is
 * valid under this assumption.
 *
 * `turn`  – index of the player who moves at this node.
 * `pos`   – mutable; moves are applied then undone (standard negamax style).
 */
function minimaxN(
    g: boolean[][], pos: Pos[], P: number,
    turn: number, depth: number, alpha: number, beta: number
): number {
    // Skip slots for players who died earlier in this search branch.
    if (pos[turn] === null) {
        const nt = nextAlive(pos, turn);
        if (nt === turn) return (turn === P) ? -10000 : 10000 + depth;
        return minimaxN(g, pos, P, nt, depth, alpha, beta);
    }

    const [cx, cy] = pos[turn]!;
    const moves: number[] = [];
    for (let d = 0; d < 4; d++) {
        const nx = cx + DX[d], ny = cy + DY[d];
        if (nx >= 0 && nx < W && ny >= 0 && ny < H && !g[ny][nx]) moves.push(d);
    }

    // No valid moves → this player dies.
    if (moves.length === 0) {
        if (turn === P) return -10000 - depth;  // I die: prefer dying later
        // Opponent dies: mark null and continue (trail stays as obstacle).
        pos[turn] = null;
        const nt = nextAlive(pos, turn);
        const v = minimaxN(g, pos, P, nt, depth, alpha, beta);  // depth unchanged
        pos[turn] = [cx, cy];  // undo
        return v;
    }

    if (depth === 0) return voronoiN(g, pos, P);

    const nt = nextAlive(pos, turn);

    if (turn === P) {
        let best = -Infinity;
        for (const d of moves) {
            const nx = cx + DX[d], ny = cy + DY[d];
            g[ny][nx] = true;
            pos[turn] = [nx, ny];
            const v = minimaxN(g, pos, P, nt, depth - 1, alpha, beta);
            pos[turn] = [cx, cy];
            g[ny][nx] = false;
            if (v > best) best = v;
            if (v > alpha) alpha = v;
            if (beta <= alpha) break;
        }
        return best;
    } else {
        // Paranoid: all opponents act to minimise my score.
        let best = Infinity;
        for (const d of moves) {
            const nx = cx + DX[d], ny = cy + DY[d];
            g[ny][nx] = true;
            pos[turn] = [nx, ny];
            const v = minimaxN(g, pos, P, nt, depth - 1, alpha, beta);
            pos[turn] = [cx, cy];
            g[ny][nx] = false;
            if (v < best) best = v;
            if (v < beta) beta = v;
            if (beta <= alpha) break;
        }
        return best;
    }
}

// ── Persistent grid + trail state ─────────────────────────────────────────────
const grid: boolean[][] = Array.from({ length: H }, () => new Array(W).fill(false));
const trails: Array<Array<[number, number]> | null> = [];
let initialized = false;

// ── Game loop ──────────────────────────────────────────────────────────────────
while (true) {
    const line1 = readline().split(' ');
    const N = +line1[0];   // 2 or 3
    const P = +line1[1];   // my player index

    if (!initialized) {
        initialized = true;
        for (let i = 0; i < N; i++) trails.push([]);
    }

    // pos[i] = current head of player i, or null if dead.
    const pos: Pos[] = [];

    for (let i = 0; i < N; i++) {
        const parts = readline().split(' ').map(Number);
        const x0 = parts[0], y0 = parts[1], x1 = parts[2], y1 = parts[3];

        if (x0 === -1) {
            if (trails[i] !== null) {
                for (const [tx, ty] of trails[i]!) grid[ty][tx] = false;
                trails[i] = null;
            }
            pos.push(null);
        } else {
            const trail = trails[i]!;
            if (trail.length === 0) {
                grid[y0][x0] = true;
                trail.push([x0, y0]);
            }
            const last = trail[trail.length - 1];
            if (last[0] !== x1 || last[1] !== y1) {
                grid[y1][x1] = true;
                trail.push([x1, y1]);
            }
            pos.push([x1, y1]);
        }
    }

    const [mx, my] = pos[P]!;

    const moves: number[] = [];
    for (let d = 0; d < 4; d++) {
        const nx = mx + DX[d], ny = my + DY[d];
        if (nx >= 0 && nx < W && ny >= 0 && ny < H && !grid[ny][nx]) moves.push(d);
    }

    if (moves.length === 0) { console.log('UP'); continue; }
    if (moves.length === 1) { console.log(DIR_NAME[moves[0]]); continue; }

    let bestDir = moves[0], bestScore = -Infinity;
    const isSep = allSeparated(grid, pos, P);

    for (const d of moves) {
        const nx = mx + DX[d], ny = my + DY[d];
        grid[ny][nx] = true;
        pos[P] = [nx, ny];

        let score: number;
        if (isSep) {
            // All opponents in separate chambers – maximise reachable space.
            score = floodFill(grid, nx, ny);
        } else {
            // Shared space: paranoid minimax, first opponent moves next.
            const nt = nextAlive(pos, P);
            score = minimaxN(grid, pos, P, nt, SEARCH_DEPTH, -Infinity, Infinity);
        }

        pos[P] = [mx, my];   // undo root move in pos
        grid[ny][nx] = false;
        if (score > bestScore) { bestScore = score; bestDir = d; }
    }

    console.log(DIR_NAME[bestDir]);
}
