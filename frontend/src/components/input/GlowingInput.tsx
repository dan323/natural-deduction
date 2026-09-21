import { CSSProperties, FC, useState, ChangeEventHandler } from 'react';
import './glowing.css'

type GlowingInputProps = {
    label: string;
    glowColor: string;
    // True for line-number inputs (they glow the referenced proof line), false for expression inputs.
    shouldGlow: boolean;
    onColorChange: (color: string, line: number) => void;
    // A line-number input reports a number, or null while its text is empty or not a whole number; an expression
    // input reports its text.
    onInput: (index: number, input: number | string | null) => void;
    index: number;
    // A problem with the value that only the parent can see, e.g. a line that is not in the proof.
    error?: string;
    // The text the input starts with, e.g. a line that was picked by clicking a row of the proof. Only read on mount.
    initialValue?: string;
};

const NOT_A_LINE_NUMBER = 'Enter a whole number, like 3.';

function isNumeric(value: string) {
    return /^\d+$/.test(value);
}

const GlowingInput: FC<GlowingInputProps> = ({ label, glowColor, shouldGlow, onColorChange, onInput, index, error, initialValue }) => {
    const [value, setValue] = useState<string>(initialValue ?? '');

    const inputId = `glowing-input-${index}`;
    const errorId = `${inputId}-error`;
    const ownError = shouldGlow && value !== '' && !isNumeric(value) ? NOT_A_LINE_NUMBER : undefined;
    const message = ownError ?? error;
    const glowing = shouldGlow && value !== '' && !message;

    const handleChange: ChangeEventHandler<HTMLInputElement> = (event) => {
        const inputValue = event.target.value;
        setValue(inputValue);
        if (!shouldGlow) {
            // Expression input: the text is always passed on as a string, even when it looks like a number.
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
                id={inputId}
                type="text"
                className={`input-field ${glowing ? 'glowing-input' : ''}`}
                style={glowing ? { '--glow-color': glowColor } as CSSProperties : undefined}
                onChange={handleChange}
                value={value}
                aria-invalid={message ? true : undefined}
                aria-describedby={message ? errorId : undefined}
            />
            {message && <p id={errorId} className="input-error">{message}</p>}
        </div>
    );
};

export default GlowingInput;
