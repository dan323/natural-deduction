import React, { FC, useState, ChangeEventHandler } from 'react';
import './glowing.css'

type GlowingInputProps = {
    label: string;
    glowColor: string;
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
        if (isNumeric(inputValue)) {
            const numInput = parseInt(inputValue);
            onInput(index, numInput);
            // Trigger glow color change only for line number inputs
            if (shouldGlow && inputValue) {
                onColorChange(glowColor, numInput - 1);
            } else if (shouldGlow) {
                onColorChange(glowColor, -1);  // Remove glow when input is empty
            }
        } else if (inputValue === '') {
            onColorChange(glowColor, -1)
        } else {
            onInput(index, inputValue);
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