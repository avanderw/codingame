type CellType = 0 | 1 | 2;

type Cell = {
    index: number;
    type: CellType;
    initialResources: number;
    neighbors: number[];
    resources: number;
    myAnts: number;
    oppAnts: number;
};

type Target = {
    cell: Cell;
    baseIndex: number;
    distance: number;
    oppDistance: number;
    path: number[];
    score: number;
};

type GameProfile = {
    initialEggs: number;
    initialCrystals: number;
    averageEggDistance: number;
    averageCrystalDistance: number;
    openingTurnLimit: number;
    expansionAntTarget: number;
    lateEggRatio: number;
};

const EGG = 1;
const CRYSTAL = 2;
const cells: Cell[] = [];

declare function readline(): string;

function readInts(): number[] {
    return readline().split(' ').map((value: string) => parseInt(value, 10));
}

const numberOfCells = parseInt(readline(), 10);
for (let i = 0; i < numberOfCells; i++) {
    const inputs = readInts();
    cells.push({
        index: i,
        type: inputs[0] as CellType,
        initialResources: inputs[1],
        neighbors: inputs.slice(2, 8),
        resources: 0,
        myAnts: 0,
        oppAnts: 0,
    });
}

const numberOfBases = parseInt(readline(), 10);
const myBases = readInts();
const oppBases = readInts();

const distancesFromBase = myBases.map((baseIndex: number) => computeDistances(baseIndex));
const distancesFromOppBase = oppBases.map((baseIndex: number) => computeDistances(baseIndex));
const turnPaths = new Map<string, number[]>();
const gameProfile = buildGameProfile();
let turn = 0;

function computeDistances(start: number): number[] {
    const distances = new Array<number>(numberOfCells).fill(-1);
    const queue: number[] = [start];
    distances[start] = 0;
    for (let head = 0; head < queue.length; head++) {
        const current = queue[head];
        for (const neighbor of cells[current].neighbors) {
            if (neighbor !== -1 && distances[neighbor] === -1) {
                distances[neighbor] = distances[current] + 1;
                queue.push(neighbor);
            }
        }
    }
    return distances;
}

function getClosestDistance(distanceMaps: number[][], cellIndex: number): number {
    let best = Number.MAX_SAFE_INTEGER;
    for (const map of distanceMaps) {
        const d = map[cellIndex];
        if (d !== -1 && d < best) best = d;
    }
    return best;
}

function buildGameProfile(): GameProfile {
    let initialEggs = 0;
    let initialCrystals = 0;
    let eggDistSum = 0;
    let crystalDistSum = 0;
    let eggCount = 0;
    let crystalCount = 0;

    for (const cell of cells) {
        const dist = getClosestDistance(distancesFromBase, cell.index);
        if (dist === Number.MAX_SAFE_INTEGER) continue;
        if (cell.type === EGG) {
            initialEggs += cell.initialResources;
            eggDistSum += dist;
            eggCount++;
        } else if (cell.type === CRYSTAL) {
            initialCrystals += cell.initialResources;
            crystalDistSum += dist;
            crystalCount++;
        }
    }

    const averageEggDistance = eggCount > 0 ? eggDistSum / eggCount : 0;
    const averageCrystalDistance = crystalCount > 0 ? crystalDistSum / crystalCount : averageEggDistance;
    // Opening ends when we've had time to reach and drain most eggs
    const openingTurnLimit = Math.max(8, Math.floor(averageEggDistance * 3 + numberOfCells / 12));
    // Expansion target: the ant army size we want before pivoting to crystals
    const expansionAntTarget = Math.max(
        numberOfBases * 8,
        Math.floor(initialEggs / Math.max(1, numberOfBases)) + Math.ceil(averageEggDistance * 3),
    );
    // Fraction of eggs remaining at which we stop treating this as the egg phase
    const lateEggRatio = Math.max(0.12, Math.min(0.30,
        averageCrystalDistance / Math.max(1, averageEggDistance + averageCrystalDistance),
    ));

    return {
        initialEggs,
        initialCrystals,
        averageEggDistance,
        averageCrystalDistance,
        openingTurnLimit,
        expansionAntTarget,
        lateEggRatio,
    };
}

function getClosestBase(cellIndex: number): { baseIndex: number; distance: number } {
    let bestBase = myBases[0];
    let bestDist = Number.MAX_SAFE_INTEGER;
    for (let i = 0; i < myBases.length; i++) {
        const d = distancesFromBase[i][cellIndex];
        if (d !== -1 && d < bestDist) {
            bestDist = d;
            bestBase = myBases[i];
        }
    }
    return { baseIndex: bestBase, distance: bestDist };
}

function getPath(from: number, to: number): number[] {
    const key = `${from}:${to}`;
    const cached = turnPaths.get(key);
    if (cached) return cached;

    const prev = new Array<number>(numberOfCells).fill(-1);
    prev[from] = from;
    const queue = [from];
    for (let head = 0; head < queue.length; head++) {
        const cur = queue[head];
        if (cur === to) break;
        for (const nb of cells[cur].neighbors) {
            if (nb !== -1 && prev[nb] === -1) {
                prev[nb] = cur;
                queue.push(nb);
            }
        }
    }
    if (prev[to] === -1) return [];

    const path: number[] = [];
    let cur = to;
    while (cur !== from) { path.push(cur); cur = prev[cur]; }
    path.push(from);
    path.reverse();
    turnPaths.set(key, path);
    return path;
}

// ── Scoring ──────────────────────────────────────────────────────────────────
//
// Early phase: grow the ant army as fast as possible.
// Eggs that are closest to our base score highest.
// More resources breaks ties. Nothing else competes.
function scoreEgg(distance: number, resources: number): number {
    return resources * 6 - distance * 10;
}

// Late phase: intercept opponent-side crystals first, leave easy near-base
// crystals for cheap cleanup at the end.
//
// denialGap = ourDistance - oppDistance
//   > 0  opponent is closer → race them there (high priority)
//   < 0  we are closer      → low priority, collect later
function scoreCrystal(distance: number, oppDistance: number, resources: number, oppAnts: number): number {
    const denialGap = distance - oppDistance;
    return resources * 5 + denialGap * 5 - oppAnts * 3;
}

// ── Candidate building ───────────────────────────────────────────────────────

function buildCandidates(earlyGame: boolean): Target[] {
    const candidates: Target[] = [];
    const wantType = earlyGame ? EGG : CRYSTAL;

    for (const cell of cells) {
        if (cell.resources <= 0 || cell.type !== wantType) continue;

        const { baseIndex, distance } = getClosestBase(cell.index);
        if (distance === Number.MAX_SAFE_INTEGER) continue;

        const oppDistance = getClosestDistance(distancesFromOppBase, cell.index);
        const path = getPath(baseIndex, cell.index);
        if (path.length === 0) continue;

        const score = earlyGame
            ? scoreEgg(distance, cell.resources)
            : scoreCrystal(distance, oppDistance, cell.resources, cell.oppAnts);

        candidates.push({ cell, baseIndex, distance, oppDistance, path, score });
    }

    candidates.sort((a, b) => b.score - a.score);
    return candidates;
}

// ── Target selection ─────────────────────────────────────────────────────────
// Greedy: pick the highest-scoring candidate that adds new cells.
// Skip paths that are 80%+ overlap with already-claimed corridors.

function selectTargets(candidates: Target[], budget: number): Target[] {
    const selected: Target[] = [];
    const claimed = new Set<number>(myBases);

    for (const candidate of candidates) {
        if (selected.length >= budget) break;

        let fresh = 0;
        for (const idx of candidate.path) {
            if (!claimed.has(idx)) fresh++;
        }
        if (fresh === 0) continue;

        const overlapRatio = (candidate.path.length - fresh) / candidate.path.length;
        if (overlapRatio > 0.80 && selected.length > 0) continue;

        selected.push(candidate);
        for (const idx of candidate.path) claimed.add(idx);
    }

    return selected;
}

// ── Main decision ─────────────────────────────────────────────────────────────

function chooseActions(): string[] {
    const myTotalAnts = cells.reduce((sum, c) => sum + c.myAnts, 0);
    const eggsRemaining = cells.reduce((sum, c) => sum + (c.type === EGG ? c.resources : 0), 0);
    const eggRatio = gameProfile.initialEggs > 0 ? eggsRemaining / gameProfile.initialEggs : 0;

    // Stay in egg phase while eggs remain, the pool isn't mostly drained yet,
    // and we either haven't hit the turn limit or still need more ants.
    const earlyGame = eggsRemaining > 0
        && eggRatio > gameProfile.lateEggRatio
        && (turn <= gameProfile.openingTurnLimit || myTotalAnts < gameProfile.expansionAntTarget);

    let candidates = buildCandidates(earlyGame);
    // Fallback: if the preferred resource type is exhausted, switch immediately.
    if (candidates.length === 0) candidates = buildCandidates(!earlyGame);
    if (candidates.length === 0) return ['WAIT'];

    const sqrtAnts = Math.floor(Math.sqrt(Math.max(myTotalAnts, 1)));
    const budget = earlyGame
        ? myBases.length + sqrtAnts + Math.max(1, Math.ceil(gameProfile.averageEggDistance / 2))
        : myBases.length + sqrtAnts + Math.max(1, Math.floor(numberOfCells / 30));
    const maxTargets = Math.max(1, Math.min(candidates.length, budget));

    const selected = selectTargets(candidates, maxTargets);
    if (selected.length === 0) return ['WAIT'];

    const actions: string[] = [];
    for (const target of selected) {
        const pressure = target.path.reduce((sum, idx) => sum + cells[idx].myAnts, 0);
        // Boost beacon strength for opponent-side crystals we are racing to deny.
        const denialBoost = !earlyGame && target.oppDistance <= target.distance ? 3 : 0;
        const baseStrength = Math.max(1, Math.min(20, target.cell.resources + denialBoost));
        const strength = pressure > 0 ? Math.min(baseStrength + 2, 20) : baseStrength;
        actions.push(`LINE ${target.baseIndex} ${target.cell.index} ${strength}`);
    }

    return actions;
}

while (true) {
    turn++;
    turnPaths.clear();

    for (let i = 0; i < numberOfCells; i++) {
        const inputs = readInts();
        cells[i].resources = inputs[0];
        cells[i].myAnts = inputs[1];
        cells[i].oppAnts = inputs[2];
    }

    console.log(chooseActions().join(';'));
}
