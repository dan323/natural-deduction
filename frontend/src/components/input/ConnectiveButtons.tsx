import { FC, useLayoutEffect, useRef } from 'react';
import { connectivesFor, insertAtCursor } from './connectives';
import './connectives.css';

type ConnectiveButtonsProps = {
    // The expression input the connectives go into.
    getInput: () => HTMLInputElement | null;
    // Receives the new text; it has to go through the same path as typed text so that the parent state follows.
    onInsert: (value: string) => void;
    // Names the input in the buttons' accessible names when several inputs have their own buttons, e.g. "Goal".
    target?: string;
    disabled?: boolean;
    // The logic of the proof the formula is for: a modal proof also gets the □ and ◇ buttons, a modal-next-until one X and U too.
    logic?: string;
};

// One button per connective, labelled with the symbol of the proof table, that types its ASCII form at the caret of
// the input (replacing the selection), then gives the focus back to the input with the caret after the insertion.
const ConnectiveButtons: FC<ConnectiveButtonsProps> = ({ getInput, onInsert, target, disabled, logic }) => {
    // Where the caret goes once the parent has rendered the new value; setting it earlier would be undone by React.
    const pendingCaret = useRef<number | null>(null);

    useLayoutEffect(() => {
        const input = getInput();
        if (pendingCaret.current === null || !input) return;
        input.focus();
        input.setSelectionRange(pendingCaret.current, pendingCaret.current);
        pendingCaret.current = null;
    });

    const insert = (text: string) => {
        const input = getInput();
        if (!input) return;
        const start = input.selectionStart ?? input.value.length;
        const end = input.selectionEnd ?? start;
        const result = insertAtCursor(input.value, start, end, text);
        if (result.value === input.value) {
            // Same text (e.g. "&" selected and ∧ clicked): the parent will not re-render, so the layout effect would not
            // run now but on some later, unrelated render, and pull the focus back here. Place the caret directly.
            pendingCaret.current = null;
            input.focus();
            input.setSelectionRange(result.caret, result.caret);
            return;
        }
        pendingCaret.current = result.caret;
        onInsert(result.value);
    };

    const where = target ? ` in ${target}` : '';

    return (
        <div className="connective-buttons" role="group" aria-label={target ? `Connectives for ${target}` : 'Connectives'}>
            {connectivesFor(logic).map(({ symbol, ascii, name, insert: text }) => (
                <button
                    key={ascii}
                    type="button"
                    className="connective-btn"
                    onClick={() => insert(text ?? ascii)}
                    // Keeps the selection of the input while the button is pressed.
                    onMouseDown={(event) => event.preventDefault()}
                    disabled={disabled}
                    aria-label={`Insert ${name} (${ascii})${where}`}
                    title={`Insert ${name} (${ascii})`}
                >
                    {symbol}
                </button>
            ))}
        </div>
    );
};

export default ConnectiveButtons;
