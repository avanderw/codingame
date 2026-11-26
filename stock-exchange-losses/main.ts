/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/

declare function readline(): string;

const n: number = parseInt(readline());
var inputs: string[] = readline().split(' ');
const values: number[] = [];
for (let i = 0; i < n; i++) {
    const v: number = parseInt(inputs[i]);
    values.push(v);
}

let maxSoFar = values[0];
let maxLoss = 0;

for (let i = 1; i < n; i++) {
    const current = values[i];
    const loss = current - maxSoFar;

    if (loss < maxLoss) {
        maxLoss = loss;
    }

    if (current > maxSoFar) {
        maxSoFar = current;
    }
}

console.log(maxLoss.toString());
