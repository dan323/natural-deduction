import { createRef } from 'react';
import { render, fireEvent, screen, waitFor, act } from '@testing-library/react';
import Menu, { MenuHandle } from '../Menu';
import { fetchActions, applyAction } from '../../../service/actions';
import { ProofDto, StepDto } from '../../../types';

// Mock the service functions
jest.mock('../../../service/actions', () => ({
    fetchActions: jest.fn(),
    applyAction: jest.fn(),
}));

const mockFetchActions = fetchActions as jest.Mock;
const mockApplyAction = applyAction as jest.Mock;

const steps: Array<StepDto> = [
    {
        expression: "P",
        rule: "Ass",
        assmsLevel: 0,
        extraParameters: {}
    },
    {
        expression: "Q",
        rule: "Ass",
        assmsLevel: 1,
        extraParameters: {}
    },
    {
        expression: "P",
        rule: "Rep [1]",
        assmsLevel: 1,
        extraParameters: {}
    },
]

const mockProof: ProofDto = {
    steps: steps,
    logic: "mock-logic",
    goal: "Q -> P"
};

describe('Menu Component', () => {
    const defaultProps = {
        logic: 'mock-logic',
        onColorChange: jest.fn(),
        setProof: jest.fn(),
        proof: mockProof,
    };

    beforeEach(() => {
        jest.clearAllMocks();
    });
    
    test('renders Menu component and fetches actions', async () => {
        mockFetchActions.mockImplementation((logic, callback) => {
            callback([{ name: 'Action1', params: ['INT', 'INT'] }, { name: 'Action2', params: ['EXPRESSION'] }]);
        });

        render(<Menu {...defaultProps} />);

        expect(mockFetchActions).toHaveBeenCalledWith(defaultProps.logic, expect.any(Function), expect.any(Function));

        await waitFor(() => {
            expect(screen.getByLabelText(/Select Inference Rule:/i)).toBeInTheDocument();
            expect(screen.getByText('-- Choose a rule --')).toBeInTheDocument();
            expect(screen.getByText('Action1')).toBeInTheDocument();
            expect(screen.getByText('Action2')).toBeInTheDocument();
        });
    });

    test('renders inputs when an action is selected', async () => {
        mockFetchActions.mockImplementation((logic, callback) => {
            callback([{ name: 'Action1', params: ['INT', 'INT'] }, { name: 'Action2', params: ['EXPRESSION'] }]);
        });

        render(<Menu {...defaultProps} />);

        // Simulate selecting an action that requires inputs
        fireEvent.change(screen.getByLabelText(/Select Inference Rule:/i), { target: { value: 'Action1' } });

        // Wait for the input fields to appear
        await waitFor(() => {
            const inputFields = screen.getAllByLabelText(/Line number:/i);
            expect(inputFields).toHaveLength(2); // Expecting 2 input fields for Action1
        });
    });

    test.each([
        ['modal', true],
        ['classical', false],
    ])('the expression input of a %s proof has modal buttons: %p', (logic, modal) => {
        mockFetchActions.mockImplementation((_logic, callback) => {
            callback([{ name: 'Ass', params: ['EXPRESSION', 'STATE'] }]);
        });

        render(<Menu {...defaultProps} logic={logic} proof={{ ...mockProof, logic }} />);
        fireEvent.change(screen.getByLabelText(/Select Inference Rule:/i), { target: { value: 'Ass' } });

        expect(screen.queryAllByRole('button', { name: /^Insert (necessarily|possibly)/ })).toHaveLength(modal ? 2 : 0);
        expect(screen.getByLabelText(/Expression:/i)).toHaveAccessibleDescription(
            modal ? expect.stringContaining('s0 <= s1') : 'Syntax: -> implies, & and, | or, - not'
        );
    });

    test.each([
        ['modal-next-until', true],
        ['modal', false],
        ['classical', false],
    ])('the expression input of a %s proof has the X and U buttons: %p', (logic, nextUntil) => {
        mockFetchActions.mockImplementation((_logic, callback) => {
            callback([{ name: 'Ass', params: ['EXPRESSION', 'STATE'] }]);
        });

        render(<Menu {...defaultProps} logic={logic} proof={{ ...mockProof, logic }} />);
        fireEvent.change(screen.getByLabelText(/Select Inference Rule:/i), { target: { value: 'Ass' } });

        expect(screen.queryAllByRole('button', { name: /^Insert (next|until) / })).toHaveLength(nextUntil ? 2 : 0);
        expect(screen.queryAllByRole('button', { name: /^Insert (necessarily|possibly)/ })).toHaveLength(logic === 'classical' ? 0 : 2);
    });

    test('button is enabled when inputs are valid', async () => {
        mockFetchActions.mockImplementation((logic, callback) => {
            callback([{ name: 'Action1', params: ['INT', 'INT'] }, { name: 'Action2', params: ['EXPRESSION'] }]);
        });

        render(<Menu {...defaultProps} />);

        fireEvent.change(screen.getByLabelText(/Select Inference Rule:/i), { target: { value: 'Action1' } });
        expect(screen.getByRole('button', { name: /Apply Rule/i })).toBeDisabled();

        const inputs = screen.getAllByLabelText(/Line number:/i);
        fireEvent.change(inputs[0], { target: { value: '1' } });
        fireEvent.change(inputs[1], { target: { value: '2' } });

        expect(screen.getByRole('button', { name: /Apply Rule/i })).not.toBeDisabled();
    });

    test('calls applyAction with correct parameters and updates proof', async () => {
        mockFetchActions.mockImplementation((logic, callback) => {
            callback([{ name: 'Action1', params: ['INT', 'INT'] }, { name: 'Action2', params: ['EXPRESSION'] }]);
        });

        const mockResponse = {
            success: true,
            proof: mockProof,
        };

        mockApplyAction.mockImplementation((logic, proof, actionDto, callback) => {
            callback(mockResponse);
        });

        render(<Menu {...defaultProps} />);
        fireEvent.change(screen.getByLabelText(/Select Inference Rule:/i), { target: { value: 'Action1' } });
        // Wait for the component to process the state update
        await waitFor(() => {
            const inputFields = screen.getAllByLabelText(/Line number:/i);
            expect(inputFields).toHaveLength(2);
        });

        const inputs = screen.getAllByLabelText(/Line number:/i);
        fireEvent.change(inputs[0], { target: { value: '1' } });
        await waitFor(() => {
            expect((inputs[0] as HTMLInputElement).value).toBe('1');
        });
        fireEvent.change(inputs[1], { target: { value: '2' } });
        await waitFor(() => {
            expect((inputs[1] as HTMLInputElement).value).toBe('2');
        });
        const performButton = screen.getByRole('button', { name: /Apply Rule/i });
        fireEvent.click(performButton);

        await waitFor(() => {
            expect(mockApplyAction).toHaveBeenCalledWith(
                defaultProps.logic,
                defaultProps.proof,
                {
                    name: 'Action1',
                    sources: [1, 2],
                    extraParameters: { expression: '' },
                },
                expect.any(Function)
            );

            expect(defaultProps.setProof).toHaveBeenCalledWith(mockResponse.proof);
        });
    });

    test('displays error message when action fails', async () => {
        mockFetchActions.mockImplementation((logic, callback) => {
            callback([{ name: 'Action1', params: ['INT', 'INT'] }, { name: 'Action2', params: ['EXPRESSION'] }]);
        });

        const mockResponse = {
            success: false,
            message: 'An error occurred',
        };

        mockApplyAction.mockImplementation((logic, proof, actionDto, callback) => {
            callback(mockResponse);
        });

        render(<Menu {...defaultProps} />);

        fireEvent.change(screen.getByLabelText(/Select Inference Rule:/i), { target: { value: 'Action1' } });

        const inputs = await screen.findAllByLabelText(/Line number:/i);
        fireEvent.change(inputs[0], { target: { value: '1' } });
        fireEvent.change(inputs[1], { target: { value: '2' } });

        const performButton = screen.getByRole('button', { name: /Apply Rule/i });
        fireEvent.click(performButton);

        await waitFor(() => {
            expect(mockApplyAction).toHaveBeenCalledWith(
                defaultProps.logic,
                defaultProps.proof,
                {
                    name: 'Action1',
                    sources: [1, 2],
                    extraParameters: { expression: '' },
                },
                expect.any(Function)
            );
            expect(defaultProps.setProof).not.toHaveBeenCalled();
            expect(screen.getByRole('alert')).toHaveTextContent('An error occurred');
        });
    });

    describe('after applying', () => {
        const setup = (response: { success: boolean, proof?: ProofDto, message?: string }) => {
            mockFetchActions.mockImplementation((logic, callback) => {
                callback([{ name: 'ORI1', params: ['INT', 'EXPRESSION'] }, { name: 'Ass', params: ['EXPRESSION', 'STATE'] }]);
            });
            mockApplyAction.mockImplementation((logic, proof, actionDto, callback) => callback(response));
            render(<Menu {...defaultProps} />);
        };

        const applyButton = () => screen.getByRole('button', { name: /Apply Rule/i });

        const fillOri1 = () => {
            fireEvent.change(screen.getByLabelText(/Select Inference Rule:/i), { target: { value: 'ORI1' } });
            fireEvent.change(screen.getByLabelText(/Line number:/i), { target: { value: '2' } });
            fireEvent.change(screen.getByLabelText(/Expression:/i), { target: { value: 'Q' } });
        };

        test('a successful action empties the inputs and keeps the selected rule', () => {
            setup({ success: true, proof: mockProof });
            fillOri1();

            fireEvent.click(applyButton());

            expect(defaultProps.setProof).toHaveBeenCalledWith(mockProof);
            expect(screen.getByLabelText(/Select Inference Rule:/i)).toHaveValue('ORI1');
            expect(screen.getByLabelText(/Line number:/i)).toHaveValue('');
            expect(screen.getByLabelText(/Expression:/i)).toHaveValue('');
            expect(applyButton()).toBeDisabled();
        });

        test('the next application starts from empty inputs, so nothing is sent twice', () => {
            setup({ success: true, proof: mockProof });
            fillOri1();
            fireEvent.click(applyButton());
            mockApplyAction.mockClear();

            fireEvent.click(applyButton());
            expect(mockApplyAction).not.toHaveBeenCalled();

            fireEvent.change(screen.getByLabelText(/Line number:/i), { target: { value: '1' } });
            fireEvent.change(screen.getByLabelText(/Expression:/i), { target: { value: 'R' } });
            fireEvent.click(applyButton());
            expect(mockApplyAction).toHaveBeenCalledWith(
                defaultProps.logic, defaultProps.proof,
                { name: 'ORI1', sources: [1], extraParameters: { expression: 'R' } },
                expect.any(Function)
            );
        });

        test('a successful action also empties the state input', () => {
            setup({ success: true, proof: mockProof });
            fireEvent.change(screen.getByLabelText(/Select Inference Rule:/i), { target: { value: 'Ass' } });
            fireEvent.change(screen.getByLabelText(/Expression:/i), { target: { value: 'P' } });
            fireEvent.change(screen.getByLabelText(/State:/i), { target: { value: 's0' } });

            fireEvent.click(applyButton());

            expect(screen.getByLabelText(/Expression:/i)).toHaveValue('');
            expect(screen.getByLabelText(/State:/i)).toHaveValue('');
            expect(applyButton()).toBeDisabled();
        });

        test('a failed action keeps the rule and everything typed', () => {
            setup({ success: false, message: 'An error occurred' });
            fillOri1();

            fireEvent.click(applyButton());

            expect(screen.getByRole('alert')).toHaveTextContent('An error occurred');
            expect(screen.getByLabelText(/Select Inference Rule:/i)).toHaveValue('ORI1');
            expect(screen.getByLabelText(/Line number:/i)).toHaveValue('2');
            expect(screen.getByLabelText(/Expression:/i)).toHaveValue('Q');
            expect(applyButton()).toBeEnabled();
        });
    });

    describe('when the proof is complete', () => {
        const doneProof: ProofDto = { ...mockProof, done: true };

        beforeEach(() => {
            mockFetchActions.mockImplementation((logic, callback) => {
                callback([{ name: 'Rep', params: ['INT'] }]);
            });
        });

        test('announces it in a status, whichever way the proof was completed', () => {
            render(<Menu {...defaultProps} proof={doneProof} />);

            expect(screen.getByRole('status')).toHaveTextContent('Proof complete.');
        });

        test('says nothing while the proof is not complete', () => {
            render(<Menu {...defaultProps} />);

            expect(screen.queryByRole('status')).not.toBeInTheDocument();
        });

        test('announces the proof that a rule has just completed', () => {
            mockApplyAction.mockImplementation((logic, proof, actionDto, callback) =>
                callback({ success: true, proof: doneProof, message: '' }));
            const { rerender } = render(<Menu {...defaultProps} />);
            fireEvent.change(screen.getByLabelText(/Select Inference Rule:/i), { target: { value: 'Rep' } });
            fireEvent.change(screen.getByLabelText(/Line number:/i), { target: { value: '1' } });

            fireEvent.click(screen.getByRole('button', { name: /Apply Rule/i }));
            rerender(<Menu {...defaultProps} proof={doneProof} />);

            expect(screen.getByRole('status')).toHaveTextContent('Proof complete.');
        });

        test('disables every rule control, without asking for more input', () => {
            const { rerender } = render(<Menu {...defaultProps} />);
            fireEvent.change(screen.getByLabelText(/Select Inference Rule:/i), { target: { value: 'Rep' } });
            fireEvent.change(screen.getByLabelText(/Line number:/i), { target: { value: '1' } });
            expect(screen.getByRole('button', { name: /Apply Rule/i })).toBeEnabled();

            rerender(<Menu {...defaultProps} proof={doneProof} />);

            expect(screen.getByLabelText(/Select Inference Rule:/i)).toBeDisabled();
            expect(screen.getByLabelText(/Line number:/i)).toBeDisabled();
            expect(screen.getByRole('button', { name: /Apply Rule/i })).toBeDisabled();
            expect(screen.getByRole('button', { name: 'Solve' })).toBeDisabled();
            expect(screen.queryByText(/Fill in every input/)).not.toBeInTheDocument();
            expect(screen.getByRole('button', { name: /Apply Rule/i })).not.toHaveAttribute('aria-describedby');
        });

        test('does not pick a line for the rule any more', () => {
            const ref = createRef<MenuHandle>();
            const { rerender } = render(<Menu {...defaultProps} ref={ref} />);
            fireEvent.change(screen.getByLabelText(/Select Inference Rule:/i), { target: { value: 'Rep' } });
            rerender(<Menu {...defaultProps} ref={ref} proof={doneProof} />);

            act(() => ref.current?.selectLine(2));

            expect(screen.getByLabelText(/Line number:/i)).toHaveValue('');
            expect(defaultProps.onColorChange).not.toHaveBeenCalledWith(expect.any(String), 1);
        });

        test('offers a new proof', () => {
            const onNewProof = jest.fn();
            render(<Menu {...defaultProps} proof={doneProof} onNewProof={onNewProof} />);

            fireEvent.click(screen.getByRole('button', { name: 'New Proof' }));

            expect(onNewProof).toHaveBeenCalledTimes(1);
            // outside of the status, so that a screen reader reads out the message alone
            expect(screen.getByRole('status')).not.toContainElement(screen.getByRole('button', { name: 'New Proof' }));
        });

        test('does not offer a new proof while the proof is open', () => {
            render(<Menu {...defaultProps} onNewProof={jest.fn()} />);

            expect(screen.queryByRole('button', { name: 'New Proof' })).not.toBeInTheDocument();
        });
    });
});
