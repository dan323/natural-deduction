import React from 'react';
import { render, fireEvent, act } from '@testing-library/react';
import GlowingInput from './GlowingInput';

describe('Glowing input', () => {
    test('renders GlowingInput component', () => {
        const { getByLabelText } = render(
            <GlowingInput
                label="Enter line number:"
                glowColor="#ffcc00"
                shouldGlow={true}
                onColorChange={jest.fn()}
                onInput={jest.fn()}
                index={0}
            />
        );
        expect(getByLabelText(/Enter line number:/i)).toBeInTheDocument();
    });

    test('input glows when shouldGlow is true and value is present', () => {
        const { getByLabelText } = render(
            <GlowingInput
                label="Enter line number:"
                glowColor="#ffcc00"
                shouldGlow={true}
                onColorChange={jest.fn()}
                onInput={jest.fn()}
                index={0}
            />
        );

        const input = getByLabelText(/Enter line number:/i);
        act(() => {
            fireEvent.change(input, { target: { value: '1' } });
        });
        expect(input).toHaveStyle(`box-shadow: 0 0 10px #ffcc00, 0 0 40px #ffcc00, 0 0 80px #ffcc00`);
    });

    test('input does not glow when shouldGlow is false', () => {
        const { getByLabelText } = render(
            <GlowingInput
                label="Enter line number:"
                glowColor="#ffcc00"
                shouldGlow={false}
                onColorChange={jest.fn()}
                onInput={jest.fn()}
                index={0}
            />
        );

        const input = getByLabelText(/Enter line number:/i);
        act(() => {
            fireEvent.change(input, { target: { value: '1' } });
        });
        expect(input).not.toHaveStyle(`box-shadow: inset 0 0 10px #ffcc00`);
    });

    test('calls onInput with correct arguments when input changes', () => {
        const onInputMock = jest.fn();
        const { getByLabelText } = render(
            <GlowingInput
                label="Enter line number:"
                glowColor="#ffcc00"
                shouldGlow={true}
                onColorChange={jest.fn()}
                onInput={onInputMock}
                index={0}
            />
        );

        const input = getByLabelText(/Enter line number:/i);
        act(() => {
            fireEvent.change(input, { target: { value: '2' } });
        });
        expect(onInputMock).toHaveBeenCalledWith(0, 2);
    });

    test('calls onColorChange correctly when input value is valid', () => {
        const onColorChangeMock = jest.fn();
        const { getByLabelText } = render(
            <GlowingInput
                label="Enter line number:"
                glowColor="#ffcc00"
                shouldGlow={true}
                onColorChange={onColorChangeMock}
                onInput={jest.fn()}
                index={0}
            />
        );

        const input = getByLabelText(/Enter line number:/i);
        act(() => {
            fireEvent.change(input, { target: { value: '3' } });
        });
        expect(onColorChangeMock).toHaveBeenCalledWith("#ffcc00", 2);
    });

    test('does not apply glow or call onColorChange with non-numeric input', () => {
        const onColorChangeMock = jest.fn();
        const { getByLabelText } = render(
            <GlowingInput
                label="Enter line number:"
                glowColor="#ffcc00"
                shouldGlow={true}
                onColorChange={onColorChangeMock}
                onInput={jest.fn()}
                index={0}
            />
        );

        const input = getByLabelText(/Enter line number:/i);
        act(() => {
            fireEvent.change(input, { target: { value: 'abc' } });
        });
        expect(onColorChangeMock).not.toHaveBeenCalled();
        expect(input).not.toHaveStyle(`box-shadow: inset 0 0 10px #ffcc00`);
    });

    test('removes glow and calls onColorChange with -1 when input is cleared', () => {
        const onColorChangeMock = jest.fn();
        const { getByLabelText } = render(
            <GlowingInput
                label="Enter line number:"
                glowColor="#ffcc00"
                shouldGlow={true}
                onColorChange={onColorChangeMock}
                onInput={jest.fn()}
                index={0}
            />
        );

        const input = getByLabelText(/Enter line number:/i);
        act(() => {
            fireEvent.change(input, { target: { value: '5' } });
            fireEvent.change(input, { target: { value: '' } });
        })
        expect(onColorChangeMock).toHaveBeenCalledWith("#ffcc00", -1);
        expect(input).not.toHaveStyle(`box-shadow: inset 0 0 10px #ffcc00`);
    });

    test('matches snapshot when shouldGlow is true', () => {
        const { asFragment } = render(
            <GlowingInput
                label="Enter line number:"
                glowColor="#ffcc00"
                shouldGlow={true}
                onColorChange={jest.fn()}
                onInput={jest.fn()}
                index={0}
            />
        );
        expect(asFragment()).toMatchSnapshot();
    });

    test('matches snapshot when shouldGlow is false', () => {
        const { asFragment } = render(
            <GlowingInput
                label="Enter line number:"
                glowColor="#ffcc00"
                shouldGlow={false}
                onColorChange={jest.fn()}
                onInput={jest.fn()}
                index={0}
            />
        );
        expect(asFragment()).toMatchSnapshot();
    });
});