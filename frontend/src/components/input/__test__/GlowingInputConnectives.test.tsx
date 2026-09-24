import { render, screen, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import GlowingInput from '../GlowingInput';

const HINT = 'Syntax: -> implies, & and, | or, - not';

const renderInput = (shouldGlow: boolean, extra: { error?: string; disabled?: boolean; isExpression?: boolean } = {}) => {
    const onInput = jest.fn();
    render(
        <GlowingInput
            label="Formula:"
            glowColor="#ffcc00"
            shouldGlow={shouldGlow}
            isExpression={!shouldGlow}
            onColorChange={jest.fn()}
            onInput={onInput}
            index={0}
            {...extra}
        />
    );
    return { onInput, input: screen.getByLabelText('Formula:') as HTMLInputElement };
};

describe('GlowingInput expression syntax help', () => {
    test('an expression input has a placeholder and a syntax hint tied to it', () => {
        const { input } = renderInput(false);

        expect(input).toHaveAttribute('placeholder', 'p -> q');
        expect(input).toHaveAccessibleDescription(HINT);
    });

    test('the hint follows the error in the description when both apply', () => {
        const { input } = renderInput(false, { error: 'Not a formula.' });

        expect(input).toHaveAttribute('aria-describedby', 'glowing-input-0-error glowing-input-0-hint');
        expect(input).toHaveAccessibleDescription(`Not a formula. ${HINT}`);
    });

    test('a line-number input has no placeholder, hint or connective buttons', () => {
        const { input } = renderInput(true);

        expect(input).not.toHaveAttribute('placeholder');
        expect(input).not.toHaveAttribute('aria-describedby');
        expect(screen.queryByText(/Syntax:/)).not.toBeInTheDocument();
        expect(screen.queryByRole('button')).not.toBeInTheDocument();
    });

    test('a text input that is not a formula (e.g. a modal state) has no placeholder, hint or connective buttons', async () => {
        const user = userEvent.setup();
        const { input, onInput } = renderInput(false, { isExpression: false });

        expect(input).not.toHaveAttribute('placeholder');
        expect(input).not.toHaveAttribute('aria-describedby');
        expect(screen.queryByText(/Syntax:/)).not.toBeInTheDocument();
        expect(screen.queryByRole('button')).not.toBeInTheDocument();

        await user.type(input, 's1');
        expect(onInput).toHaveBeenLastCalledWith(0, 's1');
    });

    test('the connective buttons show the table symbols, have accessible names and never submit', () => {
        renderInput(false);

        const buttons = screen.getAllByRole('button');
        expect(buttons.map((button) => button.textContent)).toEqual(['→', '∧', '∨', '¬']);
        expect(buttons.map((button) => button.getAttribute('aria-label'))).toEqual([
            'Insert implies (->)', 'Insert and (&)', 'Insert or (|)', 'Insert not (-)',
        ]);
        buttons.forEach((button) => expect(button).toHaveAttribute('type', 'button'));
    });

    test('a connective goes in at the caret, is reported like typed text, and the caret ends up after it', async () => {
        const user = userEvent.setup();
        const { input, onInput } = renderInput(false);
        await user.type(input, 'pq');
        input.setSelectionRange(1, 1);

        await user.click(screen.getByRole('button', { name: 'Insert implies (->)' }));

        expect(input).toHaveValue('p->q');
        expect(onInput).toHaveBeenLastCalledWith(0, 'p->q');
        expect(input).toHaveFocus();
        expect(input.selectionStart).toBe(3);
        expect(input.selectionEnd).toBe(3);
    });

    test('a connective replaces the selected text', async () => {
        const user = userEvent.setup();
        const { input, onInput } = renderInput(false);
        fireEvent.change(input, { target: { value: 'p and q' } });
        input.setSelectionRange(2, 5);

        await user.click(screen.getByRole('button', { name: 'Insert and (&)' }));

        expect(input).toHaveValue('p & q');
        expect(onInput).toHaveBeenLastCalledWith(0, 'p & q');
        expect(input.selectionStart).toBe(3);
    });

    test('a connective is appended when the caret is at the end', async () => {
        const user = userEvent.setup();
        const { input, onInput } = renderInput(false);
        await user.type(input, 'p ');

        await user.click(screen.getByRole('button', { name: 'Insert or (|)' }));
        await user.type(input, ' q');

        expect(input).toHaveValue('p | q');
        expect(onInput).toHaveBeenLastCalledWith(0, 'p | q');
    });

    test('the buttons are disabled with the input', () => {
        renderInput(false, { disabled: true });

        screen.getAllByRole('button').forEach((button) => expect(button).toBeDisabled());
    });
});
