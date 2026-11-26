// Parse input
const grid: number[][] = [];
const cageMap: string[][] = [];

for (let i = 0; i < 9; i++) {
    const inputs: string[] = readline().split(' ');
    grid.push(inputs[0].split('').map(c => c === '.' ? 0 : parseInt(c)));
    cageMap.push(inputs[1].split(''));
}

const cagesLine: string = readline();
const cageValues: Map<string, number> = new Map();
for (const part of cagesLine.split(' ')) {
    const [id, val] = part.split('=');
    cageValues.set(id, parseInt(val));
}

// Build cage cell lists
const cageCells: Map<string, [number, number][]> = new Map();
for (let r = 0; r < 9; r++) {
    for (let c = 0; c < 9; c++) {
        const id = cageMap[r][c];
        if (!cageCells.has(id)) cageCells.set(id, []);
        cageCells.get(id)!.push([r, c]);
    }
}

// For each cell, store its cage id
function getCageId(r: number, c: number): string {
    return cageMap[r][c];
}

// Check if placing val at (r,c) is valid
function isValid(r: number, c: number, val: number): boolean {
    // Row check
    for (let cc = 0; cc < 9; cc++) {
        if (cc !== c && grid[r][cc] === val) return false;
    }
    // Column check
    for (let rr = 0; rr < 9; rr++) {
        if (rr !== r && grid[rr][c] === val) return false;
    }
    // Region check
    const br = Math.floor(r / 3) * 3;
    const bc = Math.floor(c / 3) * 3;
    for (let rr = br; rr < br + 3; rr++) {
        for (let cc = bc; cc < bc + 3; cc++) {
            if ((rr !== r || cc !== c) && grid[rr][cc] === val) return false;
        }
    }
    // Cage check: no duplicate in cage
    const cageId = getCageId(r, c);
    const cells = cageCells.get(cageId)!;
    for (const [cr, cc] of cells) {
        if ((cr !== r || cc !== c) && grid[cr][cc] === val) return false;
    }
    // Cage sum check: if cage is full, sum must equal target; if partial, sum must not exceed target
    const target = cageValues.get(cageId)!;
    let sum = val;
    let filled = 1;
    for (const [cr, cc] of cells) {
        if (cr !== r || cc !== c) {
            if (grid[cr][cc] !== 0) {
                sum += grid[cr][cc];
                filled++;
            }
        }
    }
    if (sum > target) return false;
    if (filled === cells.length && sum !== target) return false;
    // Check if remaining empty cells can possibly reach the target
    const remaining = cells.length - filled;
    if (remaining > 0) {
        const needed = target - sum;
        // Minimum possible sum of 'remaining' distinct digits not already used
        const usedInCage = new Set<number>();
        usedInCage.add(val);
        for (const [cr, cc] of cells) {
            if ((cr !== r || cc !== c) && grid[cr][cc] !== 0) {
                usedInCage.add(grid[cr][cc]);
            }
        }
        // Get smallest 'remaining' digits not in usedInCage
        let minSum = 0, maxSum = 0, count = 0;
        for (let d = 1; d <= 9 && count < remaining; d++) {
            if (!usedInCage.has(d)) { minSum += d; count++; }
        }
        count = 0;
        for (let d = 9; d >= 1 && count < remaining; d--) {
            if (!usedInCage.has(d)) { maxSum += d; count++; }
        }
        if (needed < minSum || needed > maxSum) return false;
    }
    return true;
}

// Collect empty cells
const emptyCells: [number, number][] = [];
for (let r = 0; r < 9; r++) {
    for (let c = 0; c < 9; c++) {
        if (grid[r][c] === 0) emptyCells.push([r, c]);
    }
}

// Backtracking solver
function solve(idx: number): boolean {
    if (idx === emptyCells.length) return true;
    const [r, c] = emptyCells[idx];
    for (let val = 1; val <= 9; val++) {
        if (isValid(r, c, val)) {
            grid[r][c] = val;
            if (solve(idx + 1)) return true;
            grid[r][c] = 0;
        }
    }
    return false;
}

solve(0);

for (let r = 0; r < 9; r++) {
    console.log(grid[r].join(''));
}
