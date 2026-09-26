// The connectives the formula parser accepts, with the symbol the proof table shows for each. `insert` is what its button
// types when that is not just `ascii` (the word operators X and U need spaces around them, see below).
export type Connective = { symbol: string; ascii: string; name: string; insert?: string };

export const CONNECTIVES: readonly Connective[] = [
    { symbol: '→', ascii: '->', name: 'implies' },
    { symbol: '∧', ascii: '&', name: 'and' },
    { symbol: '∨', ascii: '|', name: 'or' },
    { symbol: '¬', ascii: '-', name: 'not' },
];

// The logic whose parser (`ModalLogicParser`) also reads the modal operators and relations between states.
export const MODAL_LOGIC = 'modal';

// The modal operators, on top of the connectives. The model also has `Until`, but the modal parser has no operator for
// it, so it cannot be typed there and is left out.
export const MODAL_CONNECTIVES: readonly Connective[] = [
    { symbol: '□', ascii: '[]', name: 'necessarily' },
    { symbol: '◇', ascii: '<>', name: 'possibly' },
];

// The logic whose parser (`ModalNextUntilLogicParser`) reads the modal language plus Next and Until, over states that
// can be successors (`s0+1`).
export const NEXT_UNTIL_LOGIC = 'modal-next-until';

// Next (`X A`, unary like `-`) and Until (`A U B`, binary), on top of the modal operators. The backend reads them only as
// words of their own (`Xp` and `pUq` are names), so their buttons type them with spaces around them. X gets no space
// before it (it usually starts the input or follows `(`), but `insertAtCursor` adds one when a name or number is right
// before the caret, so that `p U` + X gives `p U X `, not the name `UX`.
export const NEXT_UNTIL_CONNECTIVES: readonly Connective[] = [
    { symbol: 'X', ascii: 'X', name: 'next', insert: 'X ' },
    { symbol: 'U', ascii: 'U', name: 'until', insert: ' U ' },
];

// Whether the logic's formulas are modal ones, with □, ◇ and relations between states.
function isModal(logic?: string): boolean {
    return logic === MODAL_LOGIC || logic === NEXT_UNTIL_LOGIC;
}

// The connectives an expression input of a proof of `logic` offers.
export function connectivesFor(logic?: string): readonly Connective[] {
    if (logic === NEXT_UNTIL_LOGIC) return [...CONNECTIVES, ...MODAL_CONNECTIVES, ...NEXT_UNTIL_CONNECTIVES];
    return logic === MODAL_LOGIC ? [...CONNECTIVES, ...MODAL_CONNECTIVES] : CONNECTIVES;
}

const hintOf = (connectives: readonly Connective[]) => connectives.map(({ ascii, name }) => `${ascii} ${name}`).join(', ');

// What an expression input accepts. The table shows → ∧ ∨ ¬, but the parser only reads the ASCII forms.
export const SYNTAX_HINT = hintOf(CONNECTIVES);

// Modal formulas can also relate two states, as the modal rules use them (`s <= t` for □E, ◇I, Refl, Trans...).
export const RELATION_HINT = 'relations between states: s0 <= s1 (s1 is reachable from s0), s0 = s1 (the same state)';

// In `modal-next-until`, X and U are words of their own, and a state can be the one after another.
export const NEXT_UNTIL_HINT = 'X and U only as words of their own (Xp and pUq are names); s0+1 is the state after s0';

// The syntax hint of an expression input of a proof of `logic`: every operator its parser accepts.
export function syntaxHint(logic?: string): string {
    if (!isModal(logic)) return SYNTAX_HINT;
    const modalHint = `${hintOf(connectivesFor(logic))}; ${RELATION_HINT}`;
    return logic === NEXT_UNTIL_LOGIC ? `${modalHint}; ${NEXT_UNTIL_HINT}` : modalHint;
}

// Puts `text` in place of value[start, end) (the selection, or just the caret when start === end) and returns the new
// value with the caret position right after the inserted text. A `text` that starts with a word character (the X
// operator) gets a space in front when a word character is right before it, so that it never joins a preceding name.
export function insertAtCursor(value: string, start: number, end: number, text: string): { value: string; caret: number } {
    const from = Math.max(0, Math.min(start, end, value.length));
    const to = Math.min(value.length, Math.max(start, end, from));
    const inserted = /^\w/.test(text) && /\w$/.test(value.slice(0, from)) ? ` ${text}` : text;
    return { value: value.slice(0, from) + inserted + value.slice(to), caret: from + inserted.length };
}
