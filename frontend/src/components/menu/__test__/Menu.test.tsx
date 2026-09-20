import { render, fireEvent, screen, waitFor, act } from '@testing-library/react';
import Menu from '../Menu';
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
            expect(inputFields.length).toBe(2); // Expecting 2 input fields for Action1
        });
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
        await act(async () => {
            fireEvent.change(screen.getByLabelText(/Select Inference Rule:/i), { target: { value: 'Action1' } });
            // Wait for the component to process the state update
            await waitFor(() => {
                const inputFields = screen.getAllByLabelText(/Line number:/i);
                expect(inputFields.length).toBe(2);
            })
        });

        await act(async () => {
            const inputs = screen.getAllByLabelText(/Line number:/i);
            fireEvent.change(inputs[0], { target: { value: '1' } });

            // Wait for the component to process the state update
            await waitFor(() => {
                expect((inputs[0] as HTMLInputElement).value).toBe('1');
            })
        });
        await act(async () => {
            const inputs = screen.getAllByLabelText(/Line number:/i);
            fireEvent.change(inputs[1], { target: { value: '2' } });

            // Wait for the component to process the state update
            await waitFor(() => {
                expect((inputs[1] as HTMLInputElement).value).toBe('2');
            });
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

        await waitFor(() => {
            const inputs = screen.getAllByLabelText(/Line number:/i);
            fireEvent.change(inputs[0], { target: { value: '1' } });
            fireEvent.change(inputs[1], { target: { value: '2' } });
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
});
