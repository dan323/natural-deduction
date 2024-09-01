import React from 'react';
import { render, fireEvent, screen, waitFor, act } from '@testing-library/react';
import Menu from '../components/menu/Menu';
import { fetchActions, applyAction } from '../service/actions';
import { ProofDto, StepDto } from '../types';

// Mock the service functions
jest.mock('../service/actions', () => ({
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
        extraParameters: new Map()
    },
    {
        expression: "Q",
        rule: "Ass",
        assmsLevel: 1,
        extraParameters: new Map()
    },
    {
        expression: "P",
        rule: "Rep [1]",
        assmsLevel: 1,
        extraParameters: new Map()
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
        global.alert = jest.fn();
    });
    
    test('renders Menu component and fetches actions', async () => {
        mockFetchActions.mockImplementation((logic, callback) => {
            callback(['Action1([int, int])', 'Action2([expression])']);
        });

        render(<Menu {...defaultProps} />);

        expect(mockFetchActions).toHaveBeenCalledWith(defaultProps.logic, expect.any(Function));

        await waitFor(() => {
            expect(screen.getByLabelText(/Select Inference Rule:/i)).toBeInTheDocument();
            expect(screen.getByText('-- Choose an action --')).toBeInTheDocument();
            expect(screen.getByText('Action1')).toBeInTheDocument();
            expect(screen.getByText('Action2')).toBeInTheDocument();
        });
    });

    test('renders inputs when an action is selected', async () => {
        mockFetchActions.mockImplementation((logic, callback) => {
            callback(['Action1([int, int])', 'Action2([expression])']);
        });

        render(<Menu {...defaultProps} />);

        // Simulate selecting an action that requires inputs
        fireEvent.change(screen.getByLabelText(/Select Inference Rule:/i), { target: { value: 'Action1' } });

        // Wait for the input fields to appear
        await waitFor(() => {
            const inputFields = screen.getAllByLabelText(/Enter line number:/i);
            expect(inputFields.length).toBe(2); // Expecting 2 input fields for Action1
        });
    });

    test('button is enabled when inputs are valid', async () => {
        mockFetchActions.mockImplementation((logic, callback) => {
            callback(['Action1([int, int])', 'Action2([expression])']);
        });

        render(<Menu {...defaultProps} />);

        fireEvent.change(screen.getByLabelText(/Select Inference Rule:/i), { target: { value: 'Action1' } });

        expect(screen.getByRole('button', { name: /Perform Action/i })).not.toBeDisabled();
    });

    test('calls applyAction with correct parameters and updates proof', async () => {
        mockFetchActions.mockImplementation((logic, callback) => {
            callback(['Action1([int, int])', 'Action2([expression])']);
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
                const inputFields = screen.getAllByLabelText(/Enter line number:/i);
                expect(inputFields.length).toBe(2);
            })
        });

        await act(async () => {
            const inputs = screen.getAllByLabelText(/Enter line number:/i);
            fireEvent.change(inputs[0], { target: { value: '1' } });

            // Wait for the component to process the state update
            await waitFor(() => {
                expect((inputs[0] as HTMLInputElement).value).toBe('1');
            })
        });
        await act(async () => {
            const inputs = screen.getAllByLabelText(/Enter line number:/i);
            fireEvent.change(inputs[1], { target: { value: '2' } });

            // Wait for the component to process the state update
            await waitFor(() => {
                expect((inputs[1] as HTMLInputElement).value).toBe('2');
            });
        });
        const performButton = screen.getByRole('button', { name: /Perform Action/i });
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
            callback(['Action1([int, int])', 'Action2([expression])']);
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
            const inputs = screen.getAllByLabelText(/Enter line number:/i);
            fireEvent.change(inputs[0], { target: { value: '1' } });
            fireEvent.change(inputs[1], { target: { value: '2' } });
        });

        const performButton = screen.getByRole('button', { name: /Perform Action/i });
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
            expect(window.alert).toHaveBeenCalledWith('Action failed: An error occurred');
        });
    });
});
