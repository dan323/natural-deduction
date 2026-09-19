import React from 'react';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Menu from '../Menu';
import { fetchActions, applyAction } from '../../../service/actions';
import { ProofDto } from '../../../types';

jest.mock('../../../service/actions', () => ({
    fetchActions: jest.fn(),
    applyAction: jest.fn(),
}));

const mockFetchActions = fetchActions as jest.Mock;
const mockApplyAction = applyAction as jest.Mock;

const mockProof: ProofDto = {
    steps: [{ expression: 'P', rule: 'Ass', assmsLevel: 0, extraParameters: {} }],
    logic: 'mock-logic',
    goal: 'P',
};

describe('Menu inputs', () => {
    const props = {
        logic: 'mock-logic',
        onColorChange: jest.fn(),
        setProof: jest.fn(),
        proof: mockProof,
    };

    beforeEach(() => {
        jest.clearAllMocks();
        mockApplyAction.mockImplementation((logic, proof, actionDto, callback) => {
            callback({ success: true, proof: mockProof, message: '' });
        });
    });

    const setupActions = (actions: string[]) => {
        mockFetchActions.mockImplementation((logic, callback) => callback(actions));
        render(<Menu {...props} />);
    };

    const select = async (user: ReturnType<typeof userEvent.setup>, action: string) =>
        user.selectOptions(screen.getByLabelText(/Select Inference Rule:/i), action);

    const apply = async (user: ReturnType<typeof userEvent.setup>) =>
        user.click(screen.getByRole('button', { name: /Apply Rule/i }));

    test('switching actions clears the typed inputs', async () => {
        const user = userEvent.setup();
        setupActions(['ANDI([int, int])', 'MP([int, int])']);

        await select(user, 'ANDI');
        const [first, second] = screen.getAllByLabelText(/Line number:/i);
        await user.type(first, '1');
        await user.type(second, '2');
        expect(first).toHaveValue('1');

        await select(user, 'MP');

        const inputs = screen.getAllByLabelText(/Line number:/i);
        expect(inputs[0]).toHaveValue('');
        expect(inputs[1]).toHaveValue('');
        await apply(user);
        expect(mockApplyAction).toHaveBeenCalledWith(
            props.logic, props.proof,
            { name: 'MP', sources: [-1, -1], extraParameters: { expression: '' } },
            expect.any(Function)
        );
    });

    test('a numeric expression is sent as the expression, not as a source', async () => {
        const user = userEvent.setup();
        setupActions(['ORI1([int, expression])']);

        await select(user, 'ORI1');
        await user.type(screen.getByLabelText(/Line number:/i), '1');
        await user.type(screen.getByLabelText(/Expression:/i), '5');
        await apply(user);

        expect(mockApplyAction).toHaveBeenCalledWith(
            props.logic, props.proof,
            { name: 'ORI1', sources: [1], extraParameters: { expression: '5' } },
            expect.any(Function)
        );
    });

    test('line numbers are indexed by position among the int inputs', async () => {
        const user = userEvent.setup();
        setupActions(['MIXED([expression, int])']);

        await select(user, 'MIXED');
        await user.type(screen.getByLabelText(/Line number:/i), '4');
        await apply(user);

        expect(mockApplyAction).toHaveBeenCalledWith(
            props.logic, props.proof,
            { name: 'MIXED', sources: [4], extraParameters: { expression: '' } },
            expect.any(Function)
        );
    });

    test('clearing a line number sends -1', async () => {
        const user = userEvent.setup();
        setupActions(['Rep([int])']);

        await select(user, 'Rep');
        const input = screen.getByLabelText(/Line number:/i);
        await user.type(input, '3');
        await user.clear(input);
        await apply(user);

        expect(mockApplyAction).toHaveBeenCalledWith(
            props.logic, props.proof,
            { name: 'Rep', sources: [-1], extraParameters: { expression: '' } },
            expect.any(Function)
        );
    });

    test('clearing an expression sends an empty expression', async () => {
        const user = userEvent.setup();
        setupActions(['ORI1([int, expression])']);

        await select(user, 'ORI1');
        const input = screen.getByLabelText(/Expression:/i);
        await user.type(input, 'P');
        await user.clear(input);
        await apply(user);

        expect(mockApplyAction).toHaveBeenCalledWith(
            props.logic, props.proof,
            { name: 'ORI1', sources: [-1], extraParameters: { expression: '' } },
            expect.any(Function)
        );
    });

    test('does not refetch the actions when the props change', () => {
        mockFetchActions.mockImplementation((logic, callback) => callback(['Rep([int])']));
        const { rerender } = render(<Menu {...props} />);

        rerender(<Menu {...props} onColorChange={jest.fn()} proof={{ ...mockProof, goal: 'Q' }} />);
        rerender(<Menu {...props} onColorChange={jest.fn()} />);

        expect(mockFetchActions).toHaveBeenCalledTimes(1);
    });

    test('shows a message when the actions cannot be loaded', async () => {
        mockFetchActions.mockImplementation((logic, callback, onError) => onError('Unknown logic: foo'));
        render(<Menu {...props} />);

        expect(await screen.findByRole('alert')).toHaveTextContent('Unknown logic: foo');
    });

    test('shows the reason when an action is not applied', async () => {
        const user = userEvent.setup();
        mockApplyAction.mockImplementation((logic, proof, actionDto, callback) => {
            callback({ proof: mockProof, success: false, message: 'Line 3 does not exist' });
        });
        setupActions(['Rep([int])']);

        await select(user, 'Rep');
        await apply(user);

        expect(screen.getByRole('alert')).toHaveTextContent('Line 3 does not exist');
        expect(props.setProof).not.toHaveBeenCalled();
    });
});
