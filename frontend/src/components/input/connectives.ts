// The connectives the formula parser accepts, with the symbol the proof table shows for each.
export type Connective = { symbol: string; ascii: string; name: string };

export const CONNECTIVES: readonly Connective[] = [
    { symbol: '→', ascii: '->', name: 'implies' },
    { symbol: '∧', ascii: '&', name: 'and' },
    { symbol: '∨', ascii: '|', name: 'or' },
    { symbol: '¬', ascii: '-', name: 'not' },
];

// The logic whose parser (`ModalLogicParser`) also reads the modal operators and relations between states.
export const MODAL_LOGIC = 'modal';

// The modal operators, on top of the connectives. The model also has `Until`, but the parser has no operator for it, so
// it cannot be typed and is left out.
export const MODAL_CONNECTIVES: readonly Connective[] = [
    { symbol: '□', ascii: '[]', name: 'necessarily' },
    { symbol: '◇', ascii: '<>', name: 'possibly' },
];

// The connectives an expression input of a proof of `logic` offers.
export function connectivesFor(logic?: string): readonly Connective[] {
    return logic === MODAL_LOGIC ? [...CONNECTIVES, ...MODAL_CONNECTIVES] : CONNECTIVES;
}

const hintOf = (connectives: readonly Connective[]) => connectives.map(({ ascii, name }) => `${ascii} ${name}`).join(', ');

// What an expression input accepts. The table shows → ∧ ∨ ¬, but the parser only reads the ASCII forms.
export const SYNTAX_HINT = hintOf(CONNECTIVES);

// Modal formulas can also relate two states, as the modal rules use them (`s <= t` for □E, ◇I, Refl, Trans...).
export const RELATION_HINT = 'relations between states: s0 <= s1 (s1 is reachable from s0), s0 = s1 (the same state)';

// The syntax hint of an expression input of a proof of `logic`: every operator its parser accepts.
export function syntaxHint(logic?: string): string {
    return logic === MODAL_LOGIC ? `${hintOf(connectivesFor(logic))}; ${RELATION_HINT}` : SYNTAX_HINT;
}

// Puts `text` in place of value[start, end) (the selection, or just the caret when start === end) and returns the new
// value with the caret position right after the inserted text.
export function insertAtCursor(value: string, start: number, end: number, text: string): { value: string; caret: number } {
    const from = Math.max(0, Math.min(start, end, value.length));
    const to = Math.min(value.length, Math.max(start, end, from));
    return { value: value.slice(0, from) + text + value.slice(to), caret: from + text.length };
}
