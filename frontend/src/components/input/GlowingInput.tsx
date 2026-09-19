import React, { FC, useState, ChangeEventHandler } from 'react';
import './glowing.css'

type GlowingInputProps = {
    label: string;
    glowColor: string;
    // True for line-number inputs (they glow the referenced proof line), false for expression inputs.
    shouldGlow: boolean;
    onColorChange: (color: string, line: number) => void;
    onInput: (index: number, input: number | string) => void;
    index: number;
};

function isNumeric(value: string) {
    return /^\d+$/.test(value);
}

const GlowingInput: FC<GlowingInputProps> = ({ label, glowColor, shouldGlow, onColorChange, onInput, index}) => {
    const [value, setValue] = useState<string>('');

    const inputId = `glowing-input-${index}`;
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
            // Empty or invalid line number: forget the previous line and its glow.
            onInput(index, -1);
            onColorChange(glowColor, -1);  // Remove the glow of the previous line
        }
    };

    return (
        <div className="input-item">
            <label htmlFor={inputId} className="input-label">{label}</label>
            <input
                id={inputId}
                type="text"
                className={`input-field ${shouldGlow && value ? 'glowing-input' : ''}`}
                style={{ boxShadow: value && shouldGlow ? `0 0 10px ${glowColor}, 0 0 40px ${glowColor}, 0 0 80px ${glowColor}` : 'none' }}
                onChange={handleChange}
                value={value}
            />
        </div>
    );
};

export default GlowingInput;
