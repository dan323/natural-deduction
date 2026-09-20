import React from 'react';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Menu from '../Menu';
import { fetchActions, applyAction, solveProof } from '../../../service/actions';
import { ProofDto } from '../../../types';

jest.mock('../../../service/actions', () => ({
    fetchActions: jest.fn(),
    applyAction: jest.fn(),
    solveProof: jest.fn(),
}));

const mockFetchActions = fetchActions as jest.Mock;
const mockApplyAction = applyAction as jest.Mock;
const mockSolveProof = solveProof as jest.Mock;

const proof: ProofDto = {
    steps: [{ expression: 'P', rule: 'Ass', assmsLevel: 0, extraParameters: {} }],
    logic: 'mock-logic',
    goal: 'P -> P',
};

const solved: ProofDto = {
    ...proof,
    steps: [
        { expression: 'P', rule: 'Ass', assmsLevel: 1, extraParameters: {} },
        { expression: 'P -> P', rule: '->I [1-1]', assmsLevel: 0, extraParameters: {} },
    ],
};

describe('Menu solve button', () => {
    const props = {
        logic: 'mock-logic',
        onColorChange: jest.fn(),
        setProof: jest.fn(),
        proof,
    };

    beforeEach(() => {
        jest.clearAllMocks();
        mockFetchActions.mockImplementation((logic, callback) => callback([{ name: 'Rep', params: ['INT'] }]));
    });

    const clickSolve = async (user: ReturnType<typeof userEvent.setup>) =>
        user.click(screen.getByRole('button', { name: /Solve/i }));

    test('sends the proof to the solver and shows the returned proof', async () => {
        const user = userEvent.setup();
        mockSolveProof.mockImplementation((logic, sent, callback) => callback({ success: true, proof: solved, message: '' }));
        render(<Menu {...props} />);

        await clickSolve(user);

        expect(mockSolveProof).toHaveBeenCalledWith('mock-logic', proof, expect.any(Function));
        expect(props.setProof).toHaveBeenCalledWith(solved);
        expect(screen.getByRole('status')).toHaveTextContent('The proof is complete.');
        expect(screen.queryByRole('alert')).not.toBeInTheDocument();
        // the highlighted lines belong to the old proof
        expect(props.onColorChange).toHaveBeenCalledWith(expect.any(String), -1);
    });

    test('says so when the solver could not finish, and still shows how far it got', async () => {
        const user = userEvent.setup();
        const partial: ProofDto = { ...proof, goal: 'Q' };
        mockSolveProof.mockImplementation((logic, sent, callback) => callback({ success: true, proof: partial, message: '' }));
        render(<Menu {...props} />);

        await clickSolve(user);

        expect(props.setProof).toHaveBeenCalledWith(partial);
        expect(screen.getByRole('status')).toHaveTextContent('The solver could not finish the proof');
    });

    test('shows the error and leaves the proof alone when the solve fails', async () => {
        const user = userEvent.setup();
        mockSolveProof.mockImplementation((logic, sent, callback) => callback({
            success: false,
            message: 'The solver did not finish within 10 seconds, try solving part of the proof by hand first',
        }));
        render(<Menu {...props} />);

        await clickSolve(user);

        expect(screen.getByRole('alert')).toHaveTextContent('The solver did not finish within 10 seconds');
        expect(props.setProof).not.toHaveBeenCalled();
        expect(screen.queryByRole('status')).not.toBeInTheDocument();
    });

    test('a failure without a message still shows an error', async () => {
        const user = userEvent.setup();
        mockSolveProof.mockImplementation((logic, sent, callback) => callback({ success: false, message: '' }));
        render(<Menu {...props} />);

        await clickSolve(user);

        expect(screen.getByRole('alert')).toHaveTextContent('The proof could not be solved.');
    });

    test('is disabled while solving, together with Apply Rule', async () => {
        const user = userEvent.setup();
        let finish: (response: unknown) => void = () => undefined;
        mockSolveProof.mockImplementation((logic, sent, callback) => { finish = callback; });
        render(<Menu {...props} />);

        await clickSolve(user);

        expect(screen.getByRole('button', { name: /Solving/i })).toBeDisabled();
        expect(screen.getByRole('button', { name: /Apply Rule/i })).toBeDisabled();

        await React.act(async () => finish({ success: true, proof: solved, message: '' }));

        expect(screen.getByRole('button', { name: 'Solve' })).toBeEnabled();
    });

    test('is not offered before a proof is started', () => {
        render(<Menu {...props} proof={{ steps: [], logic: 'mock-logic', goal: '' }} />);

        expect(screen.queryByRole('button', { name: /Solve/i })).not.toBeInTheDocument();
    });

    test('a new message replaces the solver notice', async () => {
        const user = userEvent.setup();
        mockSolveProof.mockImplementation((logic, sent, callback) => callback({ success: true, proof: solved, message: '' }));
        mockApplyAction.mockImplementation((logic, sent, action, callback) => callback({ success: false, message: 'Line 9 does not exist' }));
        render(<Menu {...props} />);

        await clickSolve(user);
        expect(screen.getByRole('status')).toBeInTheDocument();
        await user.selectOptions(screen.getByLabelText(/Select Inference Rule:/i), 'Rep');
        await user.click(screen.getByRole('button', { name: /Apply Rule/i }));

        expect(screen.queryByRole('status')).not.toBeInTheDocument();
        expect(screen.getByRole('alert')).toHaveTextContent('Line 9 does not exist');
    });
});
