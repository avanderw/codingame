const W = 6; // word length

// Compute the response pattern for a given answer and guess.
// Returns an integer encoding 6 states in base-4.
// State 3 = correct position, 2 = letter present anywhere in word (no consumption),
// 1 = letter not present in word at all.
// NOTE: this game does NOT use letter consumption — a letter gets state 2 as long as
// it appears anywhere in the answer, even if another occurrence was already matched
// as state 3. e.g. ORBIT vs ABBEY: both Bs score 2 and 3 respectively.
function computePattern(answer: string, guess: string): number {
    const pat = [1, 1, 1, 1, 1, 1];

    for (let i = 0; i < W; i++) {
        if (guess[i] === answer[i]) {
            pat[i] = 3;
        } else if (answer.includes(guess[i])) {
            pat[i] = 2;
        }
    }

    return pat[0] + pat[1] * 4 + pat[2] * 16 + pat[3] * 64 + pat[4] * 256 + pat[5] * 1024;
}

// Encode an observed states array as the same base-4 integer.
function encodeStates(states: number[]): number {
    return states[0] + states[1] * 4 + states[2] * 16 + states[3] * 64 + states[4] * 256 + states[5] * 1024;
}

// Keep only candidates consistent with the observed response for the given guess.
function filterCandidates(candidates: string[], guess: string, stateKey: number): string[] {
    return candidates.filter(w => computePattern(w, guess) === stateKey);
}

// Compute Shannon entropy of the partition induced by guessing `guess` over `candidates`.
function computeEntropy(guess: string, candidates: string[]): number {
    const counts = new Map<number, number>();
    for (const w of candidates) {
        const p = computePattern(w, guess);
        counts.set(p, (counts.get(p) || 0) + 1);
    }
    const n = candidates.length;
    let entropy = 0;
    for (const count of counts.values()) {
        const p = count / n;
        entropy -= p * Math.log2(p);
    }
    return entropy;
}

// Score a word by how many of its unique letters appear frequently in candidates.
// Higher score = covers more of the remaining information.
function frequencyScore(word: string, letterFreq: Map<string, number>): number {
    let score = 0;
    const seen = new Set<string>();
    for (let i = 0; i < W; i++) {
        if (!seen.has(word[i])) {
            score += letterFreq.get(word[i]) || 0;
            seen.add(word[i]);
        }
    }
    return score;
}

// Pick the best next guess from the remaining candidates.
// Uses entropy for small sets (fast, optimal), frequency heuristic for large sets.
function selectGuess(candidates: string[], letterFreq: Map<string, number>): string {
    if (candidates.length === 1) return candidates[0];
    if (candidates.length === 2) return candidates[0];

    if (candidates.length <= 500) {
        // Entropy-based: maximise information from the next guess
        let bestWord = candidates[0];
        let bestEntropy = -1;
        for (const w of candidates) {
            const e = computeEntropy(w, candidates);
            if (e > bestEntropy) {
                bestEntropy = e;
                bestWord = w;
            }
        }
        return bestWord;
    }

    // Frequency heuristic for large candidate sets (O(n), fits first-turn 1 s budget)
    let bestWord = candidates[0];
    let bestScore = -1;
    for (const w of candidates) {
        const score = frequencyScore(w, letterFreq);
        if (score > bestScore) {
            bestScore = score;
            bestWord = w;
        }
    }
    return bestWord;
}

// ── Initialisation ──────────────────────────────────────────────────────────

const wordCount: number = parseInt(readline());
const allWords: string[] = readline().split(' ');

// Precompute per-letter presence frequency across all words (for heuristic)
const letterFreq = new Map<string, number>();
for (const w of allWords) {
    const seen = new Set<string>();
    for (const c of w) {
        if (!seen.has(c)) {
            letterFreq.set(c, (letterFreq.get(c) || 0) + 1);
            seen.add(c);
        }
    }
}

let candidates: string[] = [...allWords];
let lastGuess = '';

// ── Game loop ────────────────────────────────────────────────────────────────

while (true) {
    const states: number[] = readline().split(' ').map(Number);

    // Filter candidates using the response to our last guess.
    // (On the first turn lastGuess is empty; states are all 0 – skip filtering.)
    if (lastGuess) {
        candidates = filterCandidates(candidates, lastGuess, encodeStates(states));
    }

    const guess = selectGuess(candidates, letterFreq);
    lastGuess = guess;
    console.log(guess);
}
