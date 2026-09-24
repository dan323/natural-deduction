import { CONNECTIVES, SYNTAX_HINT, connectivesFor, insertAtCursor, syntaxHint } from '../connectives';

describe('insertAtCursor', () => {
    test('inserts in the middle of the text and puts the caret after the insertion', () => {
        expect(insertAtCursor('p  q', 2, 2, '->')).toEqual({ value: 'p -> q', caret: 4 });
    });

    test('replaces the selection', () => {
        expect(insertAtCursor('p & q', 2, 3, '->')).toEqual({ value: 'p -> q', caret: 4 });
    });

    test('a selection made backwards is replaced the same way', () => {
        expect(insertAtCursor('p & q', 3, 2, '|')).toEqual({ value: 'p | q', caret: 3 });
    });

    test('appends at the end', () => {
        expect(insertAtCursor('p ', 2, 2, '&')).toEqual({ value: 'p &', caret: 3 });
    });

    test('inserts into an empty text', () => {
        expect(insertAtCursor('', 0, 0, '-')).toEqual({ value: '-', caret: 1 });
    });

    test('positions past the end are clamped to it', () => {
        expect(insertAtCursor('p', 5, 9, '->')).toEqual({ value: 'p->', caret: 3 });
    });
});

describe('connectives', () => {
    test('are shown with the symbols of the proof table and inserted in the ASCII the parser reads', () => {
        expect(CONNECTIVES.map(({ symbol, ascii }) => [symbol, ascii])).toEqual([
            ['→', '->'], ['∧', '&'], ['∨', '|'], ['¬', '-'],
        ]);
    });

    test('the syntax hint names every connective', () => {
        expect(SYNTAX_HINT).toBe('-> implies, & and, | or, - not');
    });

    test.each([undefined, 'classical', 'intuitionistic'])('a proof of logic %p gets only the classical connectives', (logic) => {
        expect(connectivesFor(logic)).toEqual(CONNECTIVES);
        expect(syntaxHint(logic)).toBe(SYNTAX_HINT);
    });

    test('a modal proof also gets □ and ◇, and no Until, which the modal parser cannot read', () => {
        expect(connectivesFor('modal').map(({ symbol, ascii }) => [symbol, ascii])).toEqual([
            ['→', '->'], ['∧', '&'], ['∨', '|'], ['¬', '-'], ['□', '[]'], ['◇', '<>'],
        ]);
        expect(connectivesFor('modal').some(({ ascii }) => ascii === 'U')).toBe(false);
    });

    test('the modal syntax hint names every operator of the modal parser, relations between states included', () => {
        expect(syntaxHint('modal')).toBe(
            '-> implies, & and, | or, - not, [] necessarily, <> possibly; '
            + 'relations between states: s0 <= s1 (s1 is reachable from s0), s0 = s1 (the same state)'
        );
    });

    test('a modal-next-until proof also gets X and U, which are typed with spaces around them', () => {
        expect(connectivesFor('modal-next-until').map(({ symbol, ascii, insert }) => [symbol, insert ?? ascii])).toEqual([
            ['→', '->'], ['∧', '&'], ['∨', '|'], ['¬', '-'], ['□', '[]'], ['◇', '<>'], ['X', 'X '], ['U', ' U '],
        ]);
    });

    test('the modal-next-until syntax hint adds X, U and successor states', () => {
        expect(syntaxHint('modal-next-until')).toBe(
            '-> implies, & and, | or, - not, [] necessarily, <> possibly, X next, U until; '
            + 'relations between states: s0 <= s1 (s1 is reachable from s0), s0 = s1 (the same state); '
            + 'X and U only as words of their own (Xp and pUq are names); s0+1 is the state after s0'
        );
    });
});
