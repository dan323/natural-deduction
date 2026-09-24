import { CONNECTIVES, SYNTAX_HINT, insertAtCursor } from '../connectives';

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
});
