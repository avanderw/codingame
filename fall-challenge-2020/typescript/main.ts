type Inv = [number, number, number, number];

interface Action {
    id: number;
    type: string;
    delta: Inv;
    price: number;
    tomeIndex: number;
    taxCount: number;
    castable: boolean;
    repeatable: boolean;
}

function addInv(a: Inv, b: Inv): Inv {
    return [a[0]+b[0], a[1]+b[1], a[2]+b[2], a[3]+b[3]];
}

function isValidInv(inv: Inv): boolean {
    return inv[0] >= 0 && inv[1] >= 0 && inv[2] >= 0 && inv[3] >= 0 &&
           inv[0]+inv[1]+inv[2]+inv[3] <= 10;
}

function canBrew(inv: Inv, brew: Action): boolean {
    return inv[0]+brew.delta[0] >= 0 && inv[1]+brew.delta[1] >= 0 &&
           inv[2]+brew.delta[2] >= 0 && inv[3]+brew.delta[3] >= 0;
}

/**
 * Single BFS over (inventory, exhausted-spell-bitmask) states for ALL brew targets at once.
 * Running one search instead of N separate ones avoids redundant state exploration.
 *
 * State key is a packed integer (not a string) for fast Set hashing:
 *   bits  0-3:  inv[0]  (0-10 fits in 4 bits)
 *   bits  4-7:  inv[1]
 *   bits  8-11: inv[2]
 *   bits 12-15: inv[3]
 *   bits 16-27: exhausted-spell mask (12 spells → max 4095)
 */
function bfsAll(
    startInv: Inv,
    spells: Action[],
    targets: Action[],
    maxDepth: number
): Map<number, { action: string; depth: number }> {
    const results = new Map<number, { action: string; depth: number }>();

    // Capture any immediately brewable targets at depth 0
    for (let ti = 0; ti < targets.length; ti++) {
        if (canBrew(startInv, targets[ti])) {
            results.set(ti, { action: `BREW ${targets[ti].id}`, depth: 0 });
        }
    }
    if (results.size === targets.length) return results;

    // Cap at 12 spells: 2^12 = 4096 mask states, safe for 32-bit bitwise ops
    const N = Math.min(spells.length, 12);
    let initMask = 0;
    for (let i = 0; i < N; i++) {
        if (!spells[i].castable) initMask |= (1 << i);
    }

    const stateKey = (inv: Inv, mask: number): number =>
        inv[0] | (inv[1] << 4) | (inv[2] << 8) | (inv[3] << 12) | (mask << 16);

    const visited = new Set<number>();
    type Node = [Inv, number, string, number];
    const queue: Node[] = [];
    let head = 0;

    visited.add(stateKey(startInv, initMask));

    const push = (inv: Inv, mask: number, action: string, depth: number) => {
        const k = stateKey(inv, mask);
        if (visited.has(k)) return;
        visited.add(k);
        queue.push([inv, mask, action, depth]);
    };

    for (let i = 0; i < N; i++) {
        if (initMask & (1 << i)) continue;
        const s = spells[i];
        let cur = startInv;
        const maxT = s.repeatable ? 5 : 1;
        for (let t = 1; t <= maxT; t++) {
            cur = addInv(cur, s.delta);
            if (!isValidInv(cur)) break;
            const cmd = t === 1 ? `CAST ${s.id}` : `CAST ${s.id} ${t}`;
            push(cur, initMask | (1 << i), cmd, 1);
        }
    }
    if (initMask !== 0) push(startInv, 0, 'REST', 1);

    while (head < queue.length) {
        const [inv, mask, action, depth] = queue[head++];

        // Check every unresolved target at this state
        for (let ti = 0; ti < targets.length; ti++) {
            if (!results.has(ti) && canBrew(inv, targets[ti])) {
                results.set(ti, { action, depth });
            }
        }
        if (results.size === targets.length) break; // all targets found

        if (depth >= maxDepth) continue;

        for (let i = 0; i < N; i++) {
            if (mask & (1 << i)) continue;
            const s = spells[i];
            let cur = inv;
            const maxT = s.repeatable ? 5 : 1;
            for (let t = 1; t <= maxT; t++) {
                cur = addInv(cur, s.delta);
                if (!isValidInv(cur)) break;
                push(cur, mask | (1 << i), action, depth + 1);
            }
        }
        if (mask !== 0) push(inv, 0, action, depth + 1);
    }

    return results;
}

/** Score a tome spell by how valuable learning it would be. */
function scoreTome(spell: Action): number {
    let score = spell.taxCount; // free tier-0 ingredients collected from the tax pile
    for (let t = 0; t < 4; t++) {
        if (spell.delta[t] > 0) score += spell.delta[t] * (t + 1) * 2;
    }
    score -= spell.tomeIndex * 1.5; // read-ahead tax we must pay up front
    return score;
}

let turn = 0;
let learnedCount = 0;

// game loop
while (true) {
    turn++;

    const actionCount: number = parseInt(readline());
    const actions: Action[] = [];
    for (let i = 0; i < actionCount; i++) {
        const p = readline().split(' ');
        actions.push({
            id: +p[0],
            type: p[1],
            delta: [+p[2], +p[3], +p[4], +p[5]],
            price: +p[6],
            tomeIndex: +p[7],
            taxCount: +p[8],
            castable: p[9] !== '0',
            repeatable: p[10] !== '0',
        });
    }

    const players: { inv: Inv; score: number }[] = [];
    for (let i = 0; i < 2; i++) {
        const p = readline().split(' ');
        players.push({ inv: [+p[0], +p[1], +p[2], +p[3]], score: +p[4] });
    }

    const myInv  = players[0].inv;
    const brews  = actions.filter(a => a.type === 'BREW');
    const spells = actions.filter(a => a.type === 'CAST');
    const tome   = actions.filter(a => a.type === 'LEARN');

    // ── Single BFS for all brews; pick the best price-per-turn ratio ──────────
    let bestAction = '';
    let bestScore  = -Infinity;

    const brewResults = bfsAll(myInv, spells, brews, 10);
    for (let ti = 0; ti < brews.length; ti++) {
        const r = brewResults.get(ti);
        if (r !== undefined) {
            // Linear penalty: each turn of delay costs 1.5 rupees.
            const score = brews[ti].price - r.depth * 1.5;
            if (score > bestScore) {
                bestScore  = score;
                bestAction = r.action;
            }
        }
    }

    // ── Early-game: learn high-value tome spells (unless brewing right now) ───
    // More spells = shorter paths to expensive tier-2/3 potions.
    if (learnedCount < 5 && turn <= 12 && !bestAction.startsWith('BREW')) {
        const affordable = tome.filter(s => s.tomeIndex <= myInv[0]);
        if (affordable.length > 0) {
            affordable.sort((a, b) => scoreTome(b) - scoreTome(a));
            const top = affordable[0];
            if (scoreTome(top) >= 2) {
                bestAction = `LEARN ${top.id}`;
                learnedCount++;
            }
        }
    }

    // ── Fallback: learn any affordable spell, cast any ready spell, or REST ───
    if (!bestAction) {
        const affordable = tome.filter(s => s.tomeIndex <= myInv[0]);
        if (affordable.length > 0) {
            affordable.sort((a, b) => scoreTome(b) - scoreTome(a));
            bestAction = `LEARN ${affordable[0].id}`;
            learnedCount++;
        } else {
            const ready = spells.find(s => s.castable);
            bestAction = ready ? `CAST ${ready.id}` : 'REST';
        }
    }

    console.log(bestAction);
}
