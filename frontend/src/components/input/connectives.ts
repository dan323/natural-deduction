// The connectives the formula parser accepts, with the symbol the proof table shows for each.
export type Connective = { symbol: string; ascii: string; name: string };

export const CONNECTIVES: readonly Connective[] = [
    { symbol: '→', ascii: '->', name: 'implies' },
    { symbol: '∧', ascii: '&', name: 'and' },
    { symbol: '∨', ascii: '|', name: 'or' },
    { symbol: '¬', ascii: '-', name: 'not' },
];

// What an expression input accepts. The table shows → ∧ ∨ ¬, but the parser only reads the ASCII forms.
export const SYNTAX_HINT = CONNECTIVES.map(({ ascii, name }) => `${ascii} ${name}`).join(', ');

// Puts `text` in place of value[start, end) (the selection, or just the caret when start === end) and returns the new
// value with the caret position right after the inserted text.
export function insertAtCursor(value: string, start: number, end: number, text: string): { value: string; caret: number } {
    const from = Math.max(0, Math.min(start, end, value.length));
    const to = Math.min(value.length, Math.max(start, end, from));
    return { value: value.slice(0, from) + text + value.slice(to), caret: from + text.length };
}
