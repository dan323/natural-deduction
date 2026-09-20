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

    test('clearing a line number input reports no line', async () => {
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

        expect(onInput).toHaveBeenLastCalledWith(0, null);
        expect(input).not.toHaveAttribute('aria-invalid');
    });

    test('a non numeric line number input reports no line instead of text and drops the glow', async () => {
        const user = userEvent.setup();
        const onInput = jest.fn();
        const onColorChange = jest.fn();
        render(
            <GlowingInput label="Line number:" glowColor="#ffcc00" shouldGlow={true}
                onColorChange={onColorChange} onInput={onInput} index={0} />
        );

        await user.type(screen.getByLabelText('Line number:'), '3a');

        expect(onInput).toHaveBeenLastCalledWith(0, null);
        expect(onColorChange).toHaveBeenNthCalledWith(1, '#ffcc00', 2);
        expect(onColorChange).toHaveBeenLastCalledWith('#ffcc00', -1);
    });

    test.each(['a', '1.5', '-1', '3a', ' 4', '1 2'])('a line number input rejects %j and never reports -1', async (text) => {
        const user = userEvent.setup();
        const onInput = jest.fn();
        render(
            <GlowingInput label="Line number:" glowColor="#ffcc00" shouldGlow={true}
                onColorChange={jest.fn()} onInput={onInput} index={2} />
        );
        const input = screen.getByLabelText('Line number:');

        await user.type(input, text);

        expect(input).toHaveAttribute('aria-invalid', 'true');
        const message = document.getElementById(input.getAttribute('aria-describedby') as string);
        expect(message).toHaveTextContent('Enter a whole number');
        expect(input).not.toHaveClass('glowing-input');
        expect(onInput).toHaveBeenLastCalledWith(2, null);
        expect(onInput.mock.calls.map(([, value]) => value)).not.toContain(-1);
    });

    test('a valid line number clears the message again', async () => {
        const user = userEvent.setup();
        render(
            <GlowingInput label="Line number:" glowColor="#ffcc00" shouldGlow={true}
                onColorChange={jest.fn()} onInput={jest.fn()} index={0} />
        );
        const input = screen.getByLabelText('Line number:');

        await user.type(input, 'x');
        expect(input).toHaveAttribute('aria-invalid', 'true');
        await user.clear(input);
        await user.type(input, '12');

        expect(input).not.toHaveAttribute('aria-invalid');
        expect(input).not.toHaveAttribute('aria-describedby');
        expect(screen.queryByText(/Enter a whole number/)).not.toBeInTheDocument();
    });

    test('shows the error of the parent for a well formed line number and drops the glow', async () => {
        const user = userEvent.setup();
        render(
            <GlowingInput label="Line number:" glowColor="#ffcc00" shouldGlow={true}
                onColorChange={jest.fn()} onInput={jest.fn()} index={0} error="The proof has lines 1 to 3." />
        );
        const input = screen.getByLabelText('Line number:');

        await user.type(input, '9');

        expect(input).toHaveAttribute('aria-invalid', 'true');
        expect(input).toHaveAccessibleDescription('The proof has lines 1 to 3.');
        expect(input).not.toHaveClass('glowing-input');
    });

    test('an expression input accepts any text without an error', async () => {
        const user = userEvent.setup();
        render(
            <GlowingInput label="Expression:" glowColor="#ffcc00" shouldGlow={false}
                onColorChange={jest.fn()} onInput={jest.fn()} index={0} />
        );
        const input = screen.getByLabelText('Expression:');

        await user.type(input, 'a -> 1.5');

        expect(input).not.toHaveAttribute('aria-invalid');
    });
});
