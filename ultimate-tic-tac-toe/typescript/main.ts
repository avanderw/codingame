type Cell = 0 | 1 | 2;
type SmallStatus = 0 | 1 | 2 | 3;
type Move = { row: number; col: number };

const EMPTY: Cell = 0;
const ME: Cell = 1;
const OPP: Cell = 2;

const SMALL_OPEN: SmallStatus = 0;
const SMALL_ME: SmallStatus = 1;
const SMALL_OPP: SmallStatus = 2;
const SMALL_DRAW: SmallStatus = 3;

const board: Cell[][] = Array.from({ length: 9 }, () =>
    Array.from({ length: 9 }, () => EMPTY)
);
const smallStatus: SmallStatus[][] = Array.from({ length: 3 }, () =>
    Array.from({ length: 3 }, () => SMALL_OPEN)
);

const lines3: Array<[[number, number], [number, number], [number, number]]> = [
    [[0, 0], [0, 1], [0, 2]],
    [[1, 0], [1, 1], [1, 2]],
    [[2, 0], [2, 1], [2, 2]],
    [[0, 0], [1, 0], [2, 0]],
    [[0, 1], [1, 1], [2, 1]],
    [[0, 2], [1, 2], [2, 2]],
    [[0, 0], [1, 1], [2, 2]],
    [[0, 2], [1, 1], [2, 0]],
];

function localCell(br: number, bc: number, lr: number, lc: number): Cell {
    return board[br * 3 + lr][bc * 3 + lc];
}

function resolveSmallBoard(br: number, bc: number): void {
    if (smallStatus[br][bc] !== SMALL_OPEN) {
        return;
    }

    for (const [[r1, c1], [r2, c2], [r3, c3]] of lines3) {
        const a = localCell(br, bc, r1, c1);
        const b = localCell(br, bc, r2, c2);
        const c = localCell(br, bc, r3, c3);
        if (a !== EMPTY && a === b && b === c) {
            smallStatus[br][bc] = a === ME ? SMALL_ME : SMALL_OPP;
            return;
        }
    }

    let full = true;
    for (let lr = 0; lr < 3; lr++) {
        for (let lc = 0; lc < 3; lc++) {
            if (localCell(br, bc, lr, lc) === EMPTY) {
                full = false;
                break;
            }
        }
        if (!full) {
            break;
        }
    }
    if (full) {
        smallStatus[br][bc] = SMALL_DRAW;
    }
}

function macroWinner(): Cell {
    for (const [[r1, c1], [r2, c2], [r3, c3]] of lines3) {
        const a = smallStatus[r1][c1];
        const b = smallStatus[r2][c2];
        const c = smallStatus[r3][c3];
        if (a === SMALL_ME && b === SMALL_ME && c === SMALL_ME) {
            return ME;
        }
        if (a === SMALL_OPP && b === SMALL_OPP && c === SMALL_OPP) {
            return OPP;
        }
    }
    return EMPTY;
}

function play(move: Move, player: Cell): SmallStatus {
    board[move.row][move.col] = player;
    const br = Math.floor(move.row / 3);
    const bc = Math.floor(move.col / 3);
    const prev = smallStatus[br][bc];
    resolveSmallBoard(br, bc);
    return prev;
}

function unplay(move: Move, prevSmallStatus: SmallStatus): void {
    board[move.row][move.col] = EMPTY;
    const br = Math.floor(move.row / 3);
    const bc = Math.floor(move.col / 3);
    smallStatus[br][bc] = prevSmallStatus;
}

function smallBoardLinePressure(br: number, bc: number, player: Cell): number {
    let score = 0;
    const enemy = player === ME ? OPP : ME;

    for (const [[r1, c1], [r2, c2], [r3, c3]] of lines3) {
        const values = [
            localCell(br, bc, r1, c1),
            localCell(br, bc, r2, c2),
            localCell(br, bc, r3, c3),
        ];
        let mine = 0;
        let theirs = 0;
        let empties = 0;
        for (const v of values) {
            if (v === player) {
                mine++;
            } else if (v === enemy) {
                theirs++;
            } else {
                empties++;
            }
        }

        if (mine === 2 && empties === 1) {
            score += 6;
        }
        if (mine === 1 && empties === 2) {
            score += 2;
        }
        if (theirs === 2 && empties === 1) {
            score -= 5;
        }
    }
    return score;
}

function macroLinePressure(player: Cell): number {
    let score = 0;
    const mineStatus = player === ME ? SMALL_ME : SMALL_OPP;
    const oppStatus = player === ME ? SMALL_OPP : SMALL_ME;

    for (const [[r1, c1], [r2, c2], [r3, c3]] of lines3) {
        const values = [
            smallStatus[r1][c1],
            smallStatus[r2][c2],
            smallStatus[r3][c3],
        ];
        let mine = 0;
        let theirs = 0;
        let open = 0;

        for (const v of values) {
            if (v === mineStatus) {
                mine++;
            } else if (v === oppStatus) {
                theirs++;
            } else if (v === SMALL_OPEN) {
                open++;
            }
        }

        if (mine === 2 && open === 1) {
            score += 60;
        }
        if (mine === 1 && open === 2) {
            score += 18;
        }
        if (theirs === 2 && open === 1) {
            score -= 54;
        }
    }
    return score;
}

function generateValidMoves(lastMove: Move): Move[] {
    const targetBr = lastMove.row % 3;
    const targetBc = lastMove.col % 3;
    const moves: Move[] = [];

    if (smallStatus[targetBr][targetBc] === SMALL_OPEN) {
        for (let lr = 0; lr < 3; lr++) {
            for (let lc = 0; lc < 3; lc++) {
                const row = targetBr * 3 + lr;
                const col = targetBc * 3 + lc;
                if (board[row][col] === EMPTY) {
                    moves.push({ row, col });
                }
            }
        }
        if (moves.length > 0) {
            return moves;
        }
    }

    for (let br = 0; br < 3; br++) {
        for (let bc = 0; bc < 3; bc++) {
            if (smallStatus[br][bc] !== SMALL_OPEN) {
                continue;
            }
            for (let lr = 0; lr < 3; lr++) {
                for (let lc = 0; lc < 3; lc++) {
                    const row = br * 3 + lr;
                    const col = bc * 3 + lc;
                    if (board[row][col] === EMPTY) {
                        moves.push({ row, col });
                    }
                }
            }
        }
    }
    return moves;
}

function evaluatePosition(): number {
    const win = macroWinner();
    if (win === ME) {
        return 100000;
    }
    if (win === OPP) {
        return -100000;
    }

    let score = 0;
    let myWon = 0;
    let oppWon = 0;

    for (let br = 0; br < 3; br++) {
        for (let bc = 0; bc < 3; bc++) {
            const st = smallStatus[br][bc];
            if (st === SMALL_ME) {
                myWon++;
                score += 120;
            } else if (st === SMALL_OPP) {
                oppWon++;
                score -= 120;
            } else if (st === SMALL_OPEN) {
                score += smallBoardLinePressure(br, bc, ME);
                score -= smallBoardLinePressure(br, bc, OPP);
            }
        }
    }

    score += (myWon - oppWon) * 40;
    score += macroLinePressure(ME);
    score -= macroLinePressure(OPP);
    return score;
}

function countSmallWins(): { me: number; opp: number } {
    let me = 0;
    let opp = 0;
    for (let br = 0; br < 3; br++) {
        for (let bc = 0; bc < 3; bc++) {
            if (smallStatus[br][bc] === SMALL_ME) {
                me++;
            } else if (smallStatus[br][bc] === SMALL_OPP) {
                opp++;
            }
        }
    }
    return { me, opp };
}

function isGameClosed(): boolean {
    for (let br = 0; br < 3; br++) {
        for (let bc = 0; bc < 3; bc++) {
            if (smallStatus[br][bc] === SMALL_OPEN) {
                return false;
            }
        }
    }
    return true;
}

function terminalResult(): number | null {
    const winner = macroWinner();
    if (winner === ME) {
        return 1;
    }
    if (winner === OPP) {
        return -1;
    }
    if (!isGameClosed()) {
        return null;
    }

    const wins = countSmallWins();
    if (wins.me > wins.opp) {
        return 1;
    }
    if (wins.opp > wins.me) {
        return -1;
    }
    return 0;
}

function toUnitScore(score: number): number {
    if (score > 600) {
        return 1;
    }
    if (score < -600) {
        return -1;
    }
    return score / 600;
}

let rngState = 2463534242;
let searchAborted = false;
let searchNodes = 0;

function randInt(maxExclusive: number): number {
    rngState ^= rngState << 13;
    rngState ^= rngState >>> 17;
    rngState ^= rngState << 5;
    return (rngState >>> 0) % maxExclusive;
}

function tacticalMoveScore(move: Move, player: Cell): number {
    const prev = play(move, player);
    let score = evaluatePosition();
    const winner = macroWinner();

    if (winner === player) {
        score += 200000;
    }

    const br = Math.floor(move.row / 3);
    const bc = Math.floor(move.col / 3);
    if (prev === SMALL_OPEN && smallStatus[br][bc] !== SMALL_OPEN) {
        if (smallStatus[br][bc] === (player === ME ? SMALL_ME : SMALL_OPP)) {
            score += 500;
        }
    }

    const lr = move.row % 3;
    const lc = move.col % 3;
    if (lr === 1 && lc === 1) {
        score += 8;
    } else if ((lr === 0 || lr === 2) && (lc === 0 || lc === 2)) {
        score += 4;
    }

    unplay(move, prev);
    return player === ME ? score : -score;
}

function quickOrderScore(move: Move): number {
    let score = 0;

    const localR = move.row % 3;
    const localC = move.col % 3;
    if (localR === 1 && localC === 1) {
        score += 5;
    } else if ((localR === 0 || localR === 2) && (localC === 0 || localC === 2)) {
        score += 3;
    }

    const macroR = Math.floor(move.row / 3);
    const macroC = Math.floor(move.col / 3);
    if (macroR === 1 && macroC === 1) {
        score += 2;
    }

    const sendBr = move.row % 3;
    const sendBc = move.col % 3;
    if (smallStatus[sendBr][sendBc] !== SMALL_OPEN) {
        score += 1;
    }

    return score;
}

function orderedMoves(lastMove: Move, player: Cell, branchLimit: number): Move[] {
    const moves = generateValidMoves(lastMove);
    if (moves.length <= 1) {
        return moves;
    }

    // Keep ordering inexpensive inside minimax.
    moves.sort((a, b) => quickOrderScore(b) - quickOrderScore(a));
    if (moves.length > branchLimit) {
        return moves.slice(0, branchLimit);
    }
    return moves;
}

function minimax(
    lastMove: Move,
    depth: number,
    alpha: number,
    beta: number,
    player: Cell,
    branchLimit: number,
    deadlineMs: number
): number {
    searchNodes++;
    if ((searchNodes & 31) === 0 && Date.now() >= deadlineMs) {
        searchAborted = true;
        return evaluatePosition();
    }

    const term = terminalResult();
    if (term !== null) {
        return term * 200000;
    }
    if (depth === 0) {
        return evaluatePosition();
    }

    const moves = orderedMoves(lastMove, player, branchLimit);
    if (moves.length === 0) {
        return evaluatePosition();
    }

    if (player === ME) {
        let best = -Infinity;
        for (const move of moves) {
            if (Date.now() >= deadlineMs) {
                searchAborted = true;
                break;
            }
            const prev = play(move, ME);
            const score = minimax(
                move,
                depth - 1,
                alpha,
                beta,
                OPP,
                branchLimit,
                deadlineMs
            );
            unplay(move, prev);

            if (score > best) {
                best = score;
            }
            if (score > alpha) {
                alpha = score;
            }
            if (beta <= alpha) {
                break;
            }
        }
        return best;
    }

    let best = Infinity;
    for (const move of moves) {
        if (Date.now() >= deadlineMs) {
            searchAborted = true;
            break;
        }
        const prev = play(move, OPP);
        const score = minimax(
            move,
            depth - 1,
            alpha,
            beta,
            ME,
            branchLimit,
            deadlineMs
        );
        unplay(move, prev);

        if (score < best) {
            best = score;
        }
        if (score < beta) {
            beta = score;
        }
        if (beta <= alpha) {
            break;
        }
    }
    return best;
}

function cheapRolloutScore(move: Move): number {
    let score = 0;

    const lr = move.row % 3;
    const lc = move.col % 3;
    if (lr === 1 && lc === 1) {
        score += 3;
    } else if ((lr === 0 || lr === 2) && (lc === 0 || lc === 2)) {
        score += 2;
    }

    const sendBr = move.row % 3;
    const sendBc = move.col % 3;
    if (smallStatus[sendBr][sendBc] !== SMALL_OPEN) {
        score -= 1;
    }

    return score;
}

function pickRolloutMove(moves: Move[], player: Cell): Move {
    if (moves.length === 1) {
        return moves[0];
    }

    // Cheaply biased rollouts with light exploration.
    if (randInt(100) < 65) {
        let bestMove = moves[0];
        let bestScore = -Infinity;
        for (const move of moves) {
            const score = cheapRolloutScore(move);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        return bestMove;
    }

    return moves[randInt(moves.length)];
}

function rolloutFrom(lastMove: Move, nextPlayer: Cell, maxDepth: number): number {
    const stackMoves: Move[] = [];
    const stackPrev: SmallStatus[] = [];

    let currentLast = lastMove;
    let currentPlayer = nextPlayer;

    for (let ply = 0; ply < maxDepth; ply++) {
        const term = terminalResult();
        if (term !== null) {
            for (let i = stackMoves.length - 1; i >= 0; i--) {
                unplay(stackMoves[i], stackPrev[i]);
            }
            return term;
        }

        const moves = generateValidMoves(currentLast);
        if (moves.length === 0) {
            break;
        }

        const move = pickRolloutMove(moves, currentPlayer);
        const prev = play(move, currentPlayer);
        stackMoves.push(move);
        stackPrev.push(prev);
        currentLast = move;
        currentPlayer = currentPlayer === ME ? OPP : ME;
    }

    const approx = toUnitScore(evaluatePosition());
    for (let i = stackMoves.length - 1; i >= 0; i--) {
        unplay(stackMoves[i], stackPrev[i]);
    }
    return approx;
}

function chooseMove(validMoves: Move[], turnIndex: number): Move {
    if (validMoves.length === 1) {
        return validMoves[0];
    }

    // Instant tactical finish.
    for (const move of validMoves) {
        const prev = play(move, ME);
        const won = macroWinner() === ME;
        unplay(move, prev);
        if (won) {
            return move;
        }
    }

    const rootOrdered = [...validMoves].sort(
        (a, b) => tacticalMoveScore(b, ME) - tacticalMoveScore(a, ME)
    );
    const candidateCount = validMoves.length <= 6 ? validMoves.length : 6;
    const candidates = rootOrdered.slice(0, candidateCount);

    const branchLimit = validMoves.length <= 10 ? 7 : 5;

    const minimaxScore = new Map<string, number>();
    for (const move of candidates) {
        minimaxScore.set(`${move.row},${move.col}`, tacticalMoveScore(move, ME));
    }

    const start = Date.now();
    const turnBudgetMs = turnIndex === 0 ? 930 : 92;
    const hardDeadline = start + turnBudgetMs - 3;
    const minimaxSliceMs = turnIndex === 0 ? 420 : 30;
    const minimaxDeadline = Math.min(hardDeadline, start + minimaxSliceMs);

    let lastCompletedDepth = 0;
    for (let depth = 1; depth <= 6; depth++) {
        if (Date.now() >= minimaxDeadline) {
            break;
        }

        const depthScore = new Map<string, number>();
        let depthComplete = true;

        for (const move of candidates) {
            if (Date.now() >= minimaxDeadline) {
                depthComplete = false;
                break;
            }

            const prev = play(move, ME);
            searchAborted = false;
            searchNodes = 0;
            const score = minimax(
                move,
                depth - 1,
                -Infinity,
                Infinity,
                OPP,
                branchLimit,
                minimaxDeadline
            );
            unplay(move, prev);

            if (searchAborted) {
                depthComplete = false;
                break;
            }
            depthScore.set(`${move.row},${move.col}`, score);
        }

        if (!depthComplete) {
            break;
        }

        lastCompletedDepth = depth;
        for (const move of candidates) {
            const key = `${move.row},${move.col}`;
            const score = depthScore.get(key);
            if (score !== undefined) {
                minimaxScore.set(key, score);
            }
        }
    }

    for (const move of candidates) {
        const key = `${move.row},${move.col}`;
        minimaxScore.set(key, (minimaxScore.get(key) || 0) + lastCompletedDepth * 2);
    }

    const mcValue = new Map<string, number>();
    const mcVisits = new Map<string, number>();
    for (const move of candidates) {
        const key = `${move.row},${move.col}`;
        mcValue.set(key, 0);
        mcVisits.set(key, 0);
    }

    const rolloutDepth = validMoves.length <= 9 ? 18 : 14;

    let idx = 0;
    while (Date.now() < hardDeadline) {
        const move = candidates[idx];
        const key = `${move.row},${move.col}`;

        const prev = play(move, ME);
        const value = rolloutFrom(move, OPP, rolloutDepth);
        unplay(move, prev);

        mcValue.set(key, (mcValue.get(key) || 0) + value);
        mcVisits.set(key, (mcVisits.get(key) || 0) + 1);

        idx++;
        if (idx >= candidates.length) {
            idx = 0;
        }
    }

    let bestMove = candidates[0];
    let bestScore = -Infinity;

    for (const move of candidates) {
        const key = `${move.row},${move.col}`;
        const mini = minimaxScore.get(key) || -Infinity;
        const visits = mcVisits.get(key) || 0;
        const meanRollout = visits > 0 ? (mcValue.get(key) || 0) / visits : 0;

        const combined = mini + meanRollout * 1200;
        if (combined > bestScore) {
            bestScore = combined;
            bestMove = move;
        }
    }

    return bestMove;
}

// game loop
let turnIndex = 0;
while (true) {
    const firstLine = readline();
    if (!firstLine) {
        break;
    }

    const inputs = firstLine.split(' ');
    const opponentRow = parseInt(inputs[0], 10);
    const opponentCol = parseInt(inputs[1], 10);

    if (opponentRow >= 0 && opponentCol >= 0) {
        play({ row: opponentRow, col: opponentCol }, OPP);
    }

    const validActionCount = parseInt(readline(), 10);
    const validMoves: Move[] = [];

    for (let i = 0; i < validActionCount; i++) {
        const action = readline().split(' ');
        const row = parseInt(action[0], 10);
        const col = parseInt(action[1], 10);
        validMoves.push({ row, col });
    }

    const myMove = chooseMove(validMoves, turnIndex);
    play(myMove, ME);
    console.log(`${myMove.row} ${myMove.col}`);
    turnIndex++;
}
