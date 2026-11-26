const board: string[][] = [];
for (let i = 0; i < 8; i++) {
    board.push(readline().split(''));
}
const inputs: string[] = readline().split(' ');
const colour: string = inputs[0];
const move: string = inputs[1];

const col: number = move.charCodeAt(0) - 'a'.charCodeAt(0);
const row: number = parseInt(move[1]) - 1;
const opponent: string = colour === 'B' ? 'W' : 'B';

if (board[row][col] !== '-') {
    console.log('NOPE');
} else {
    const directions: number[][] = [[-1,-1],[-1,0],[-1,1],[0,-1],[0,1],[1,-1],[1,0],[1,1]];
    const toFlip: [number, number][] = [];

    for (const [dr, dc] of directions) {
        const candidates: [number, number][] = [];
        let r = row + dr;
        let c = col + dc;
        while (r >= 0 && r < 8 && c >= 0 && c < 8 && board[r][c] === opponent) {
            candidates.push([r, c]);
            r += dr;
            c += dc;
        }
        if (candidates.length > 0 && r >= 0 && r < 8 && c >= 0 && c < 8 && board[r][c] === colour) {
            toFlip.push(...candidates);
        }
    }

    if (toFlip.length === 0) {
        console.log('NULL');
    } else {
        board[row][col] = colour;
        for (const [r, c] of toFlip) {
            board[r][c] = colour;
        }
        let wCount = 0, bCount = 0;
        for (let r = 0; r < 8; r++) {
            for (let c = 0; c < 8; c++) {
                if (board[r][c] === 'W') wCount++;
                else if (board[r][c] === 'B') bCount++;
            }
        }
        console.log(`${wCount} ${bCount}`);
    }
}
