const magicPhrase: string = readline();

const NUM_ZONES = 30;
const ALPHABET = " ABCDEFGHIJKLMNOPQRSTUVWXYZ"; // 27 characters

function charToIndex(c: string): number {
    if (c === " ") return 0;
    return c.charCodeAt(0) - 64; // A=1, B=2, ..., Z=26
}

function rollCost(from: number, to: number): number {
    const forward = (to - from + 27) % 27;
    const backward = (from - to + 27) % 27;
    return Math.min(forward, backward);
}

function rollString(from: number, to: number): string {
    const forward = (to - from + 27) % 27;
    const backward = (from - to + 27) % 27;
    if (forward <= backward) {
        return "+".repeat(forward);
    } else {
        return "-".repeat(backward);
    }
}

function moveCost(from: number, to: number): number {
    const right = (to - from + NUM_ZONES) % NUM_ZONES;
    const left = (from - to + NUM_ZONES) % NUM_ZONES;
    return Math.min(right, left);
}

function moveString(from: number, to: number): string {
    const right = (to - from + NUM_ZONES) % NUM_ZONES;
    const left = (from - to + NUM_ZONES) % NUM_ZONES;
    if (right <= left) {
        return ">".repeat(right);
    } else {
        return "<".repeat(left);
    }
}

// Greedy approach: for each character, find the zone that minimizes move + roll cost
const zones: number[] = new Array(NUM_ZONES).fill(0); // all start as space (index 0)
let pos = 0;
let output = "";

for (let i = 0; i < magicPhrase.length; i++) {
    const target = charToIndex(magicPhrase[i]);

    let bestZone = 0;
    let bestCost = Infinity;

    for (let z = 0; z < NUM_ZONES; z++) {
        const cost = moveCost(pos, z) + rollCost(zones[z], target);
        if (cost < bestCost) {
            bestCost = cost;
            bestZone = z;
        }
    }

    output += moveString(pos, bestZone);
    output += rollString(zones[bestZone], target);
    output += ".";

    pos = bestZone;
    zones[bestZone] = target;
}

console.log(output);
