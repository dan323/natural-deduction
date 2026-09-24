import { CSSProperties, FC, useRef, useState } from 'react';
import ConnectiveButtons from './ConnectiveButtons';
import { syntaxHint } from './connectives';
import './glowing.css'

type GlowingInputProps = {
    label: string;
    glowColor: string;
    // True for line-number inputs (they glow the referenced proof line), false for text inputs.
    shouldGlow: boolean;
    // True for formula inputs (EXPRESSION params): they get a formula placeholder, the syntax hint and the connective
    // buttons. Other text inputs, like a modal STATE, get none of these.
    isExpression?: boolean;
    onColorChange: (color: string, line: number) => void;
    // A line-number input reports a number, or null while its text is empty or not a whole number; an expression
    // input reports its text.
    onInput: (index: number, input: number | string | null) => void;
    index: number;
    // A problem with the value that only the parent can see, e.g. a line that is not in the proof.
    error?: string;
    // The text the input starts with, e.g. a line that was picked by clicking a row of the proof. Only read on mount.
    initialValue?: string;
    disabled?: boolean;
    // The logic of the proof: the syntax hint and the connective buttons of a formula input follow it (modal adds □ ◇).
    logic?: string;
};

const NOT_A_LINE_NUMBER = 'Enter a whole number, like 3.';

function isNumeric(value: string) {
    return /^\d+$/.test(value);
}

const GlowingInput: FC<GlowingInputProps> = ({ label, glowColor, shouldGlow, isExpression = false, onColorChange, onInput, index, error, initialValue, disabled, logic }) => {
    const [value, setValue] = useState<string>(initialValue ?? '');

    const inputId = `glowing-input-${index}`;
    const errorId = `${inputId}-error`;
    const hintId = `${inputId}-hint`;
    const inputRef = useRef<HTMLInputElement>(null);
    const ownError = shouldGlow && value !== '' && !isNumeric(value) ? NOT_A_LINE_NUMBER : undefined;
    const message = ownError ?? error;
    const glowing = shouldGlow && value !== '' && !message;
    // A formula input always describes its syntax; the error, when there is one, comes first.
    const describedBy = [message ? errorId : undefined, isExpression ? hintId : undefined].filter(Boolean).join(' ') || undefined;

    // Typed text and inserted connectives both go through here.
    const handleChange = (inputValue: string) => {
        setValue(inputValue);
        if (!shouldGlow) {
            // Text input: the text is always passed on as a string, even when it looks like a number.
            onInput(index, inputValue);
        } else if (isNumeric(inputValue)) {
            const numInput = Number.parseInt(inputValue, 10);
            onInput(index, numInput);
            onColorChange(glowColor, numInput - 1);
        } else {
            // Empty or invalid line number: there is no line to report, and the previous one loses its glow.
            onInput(index, null);
            onColorChange(glowColor, -1);
        }
    };

    return (
        <div className="input-item">
            <label htmlFor={inputId} className="input-label">{label}</label>
            <input
                ref={inputRef}
                id={inputId}
                type="text"
                className={`input-field ${glowing ? 'glowing-input' : ''}`}
                style={glowing ? { '--glow-color': glowColor } as CSSProperties : undefined}
                onChange={(event) => handleChange(event.target.value)}
                value={value}
                placeholder={isExpression ? 'p -> q' : undefined}
                disabled={disabled}
                aria-invalid={message ? true : undefined}
                aria-describedby={describedBy}
            />
            {message && <p id={errorId} className="input-error">{message}</p>}
            {isExpression && (
                <>
                    <p id={hintId} className="syntax-hint">Syntax: {syntaxHint(logic)}</p>
                    <ConnectiveButtons getInput={() => inputRef.current} onInsert={handleChange} disabled={disabled} logic={logic} />
                </>
            )}
        </div>
    );
};

export default GlowingInput;
