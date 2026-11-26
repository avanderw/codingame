const inputs: string[] = readline().split(' ');
const L: number = parseInt(inputs[0], 10);
const H: number = parseInt(inputs[1], 10);

const alphabetLines: string[] = [];
for (let i = 0; i < H; i++) {
    alphabetLines.push(readline());
}

const digitGlyphs: string[][] = Array.from({ length: 20 }, () => [] as string[]);
for (let d = 0; d < 20; d++) {
    for (let row = 0; row < H; row++) {
        const line = alphabetLines[row];
        digitGlyphs[d].push(line.substr(d * L, L));
    }
}

const glyphToDigit = new Map<string, number>();
for (let d = 0; d < 20; d++) {
    glyphToDigit.set(digitGlyphs[d].join('\n'), d);
}

function readMayanNumber(totalLines: number): bigint {
    const sectionCount = totalLines / H;
    const sections: string[][] = [];

    for (let i = 0; i < sectionCount; i++) {
        const glyph: string[] = [];
        for (let row = 0; row < H; row++) {
            glyph.push(readline());
        }
        sections.push(glyph);
    }

    let value = 0n;
    for (const section of sections) {
        const key = section.join('\n');
        const digit = glyphToDigit.get(key);
        if (digit === undefined) {
            throw new Error('Unknown Mayan glyph');
        }
        value = value * 20n + BigInt(digit);
    }

    return value;
}

function toMayanGlyphs(value: bigint): string[] {
    if (value === 0n) {
        return [...digitGlyphs[0]];
    }

    const digits: number[] = [];
    let n = value;
    while (n > 0n) {
        digits.push(Number(n % 20n));
        n /= 20n;
    }
    digits.reverse();

    const out: string[] = [];
    for (const d of digits) {
        out.push(...digitGlyphs[d]);
    }
    return out;
}

const S1: number = parseInt(readline(), 10);
const n1 = readMayanNumber(S1);

const S2: number = parseInt(readline(), 10);
const n2 = readMayanNumber(S2);

const operation: string = readline();

let result: bigint;
if (operation === '+') {
    result = n1 + n2;
} else if (operation === '-') {
    result = n1 - n2;
} else if (operation === '*') {
    result = n1 * n2;
} else {
    result = n1 / n2;
}

const output = toMayanGlyphs(result);
for (const line of output) {
    console.log(line);
}
