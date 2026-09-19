import React from 'react';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import GlowingInput from '../GlowingInput';

describe('Glowing input kinds', () => {
    test('a numeric text in an expression input is passed as a string', async () => {
        const user = userEvent.setup();
        const onInput = jest.fn();
        const onColorChange = jest.fn();
        render(
            <GlowingInput
                label="Expression:"
                glowColor="#ffcc00"
                shouldGlow={false}
                onColorChange={onColorChange}
                onInput={onInput}
                index={1}
            />
        );

        await user.type(screen.getByLabelText('Expression:'), '12');

        expect(onInput).toHaveBeenLastCalledWith(1, '12');
        expect(onInput.mock.calls.every(([, value]) => typeof value === 'string')).toBe(true);
        expect(onColorChange).not.toHaveBeenCalled();
    });

    test('clearing an expression input reports an empty string', async () => {
        const user = userEvent.setup();
        const onInput = jest.fn();
        render(
            <GlowingInput label="Expression:" glowColor="#ffcc00" shouldGlow={false}
                onColorChange={jest.fn()} onInput={onInput} index={0} />
        );
        const input = screen.getByLabelText('Expression:');

        await user.type(input, 'P');
        await user.clear(input);

        expect(onInput).toHaveBeenLastCalledWith(0, '');
    });

    test('clearing a line number input reports -1', async () => {
        const user = userEvent.setup();
        const onInput = jest.fn();
        render(
            <GlowingInput label="Line number:" glowColor="#ffcc00" shouldGlow={true}
                onColorChange={jest.fn()} onInput={onInput} index={0} />
        );
        const input = screen.getByLabelText('Line number:');

        await user.type(input, '3');
        expect(onInput).toHaveBeenLastCalledWith(0, 3);
        await user.clear(input);

        expect(onInput).toHaveBeenLastCalledWith(0, -1);
    });

    test('a non numeric line number input reports -1 instead of text and drops the glow', async () => {
        const user = userEvent.setup();
        const onInput = jest.fn();
        const onColorChange = jest.fn();
        render(
            <GlowingInput label="Line number:" glowColor="#ffcc00" shouldGlow={true}
                onColorChange={onColorChange} onInput={onInput} index={0} />
        );

        await user.type(screen.getByLabelText('Line number:'), '3a');

        expect(onInput).toHaveBeenLastCalledWith(0, -1);
        expect(onColorChange).toHaveBeenNthCalledWith(1, '#ffcc00', 2);
        expect(onColorChange).toHaveBeenLastCalledWith('#ffcc00', -1);
    });
});
