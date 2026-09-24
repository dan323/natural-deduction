import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Menu from '../Menu';
import { fetchActions, applyAction } from '../../../service/actions';
import { ActionDescriptor, ProofDto } from '../../../types';

jest.mock('../../../service/actions', () => ({
    fetchActions: jest.fn(),
    applyAction: jest.fn(),
}));

const mockFetchActions = fetchActions as jest.Mock;
const mockApplyAction = applyAction as jest.Mock;

const REP: ActionDescriptor = { name: 'Rep', params: ['INT'] };
const ORI1: ActionDescriptor = { name: 'ORI1', params: ['INT', 'EXPRESSION'] };

// Eight lines, so that the line numbers used below are all in the proof.
const mockProof: ProofDto = {
    steps: Array.from({ length: 8 }, () => ({ expression: 'P', rule: 'Ass', assmsLevel: 0, extraParameters: {} })),
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

    const setupActions = (actions: ActionDescriptor[]) => {
        mockFetchActions.mockImplementation((logic, callback) => callback(actions));
        render(<Menu {...props} />);
    };

    const select = async (user: ReturnType<typeof userEvent.setup>, action: string) =>
        user.selectOptions(screen.getByLabelText(/Select Inference Rule:/i), action);

    const applyButton = () => screen.getByRole('button', { name: /Apply Rule/i });

    const apply = async (user: ReturnType<typeof userEvent.setup>) =>
        user.click(screen.getByRole('button', { name: /Apply Rule/i }));

    test('switching actions clears the typed inputs', async () => {
        const user = userEvent.setup();
        setupActions([{ name: 'ANDI', params: ['INT', 'INT'] }, { name: 'MP', params: ['INT', 'INT'] }]);

        await select(user, 'ANDI');
        const [first, second] = screen.getAllByLabelText(/Line number:/i);
        await user.type(first, '1');
        await user.type(second, '2');
        expect(first).toHaveValue('1');

        await select(user, 'MP');

        const inputs = screen.getAllByLabelText(/Line number:/i);
        expect(inputs[0]).toHaveValue('');
        expect(inputs[1]).toHaveValue('');
        expect(applyButton()).toBeDisabled();
    });

    test('a numeric expression is sent as the expression, not as a source', async () => {
        const user = userEvent.setup();
        setupActions([ORI1]);

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
        setupActions([{ name: 'MIXED', params: ['EXPRESSION', 'INT'] }]);

        await select(user, 'MIXED');
        await user.type(screen.getByLabelText(/Expression:/i), 'Q');
        await user.type(screen.getByLabelText(/Line number:/i), '4');
        await apply(user);

        expect(mockApplyAction).toHaveBeenCalledWith(
            props.logic, props.proof,
            { name: 'MIXED', sources: [4], extraParameters: { expression: 'Q' } },
            expect.any(Function)
        );
    });

    test('clearing a line number disables Apply, and -1 is never sent', async () => {
        const user = userEvent.setup();
        setupActions([REP]);

        await select(user, 'Rep');
        const input = screen.getByLabelText(/Line number:/i);
        await user.type(input, '3');
        expect(applyButton()).toBeEnabled();
        await user.clear(input);

        expect(applyButton()).toBeDisabled();
        await apply(user);
        expect(mockApplyAction).not.toHaveBeenCalled();
    });

    test('clearing an expression disables Apply', async () => {
        const user = userEvent.setup();
        setupActions([ORI1]);

        await select(user, 'ORI1');
        await user.type(screen.getByLabelText(/Line number:/i), '1');
        const input = screen.getByLabelText(/Expression:/i);
        await user.type(input, 'P');
        expect(applyButton()).toBeEnabled();
        await user.clear(input);

        expect(applyButton()).toBeDisabled();
    });

    test('each param becomes one input, in order, labelled by its kind', async () => {
        const user = userEvent.setup();
        setupActions([{ name: 'ALL', params: ['EXPRESSION', 'INT', 'STATE', 'INT'] }]);

        await select(user, 'ALL');

        const labels = Array.from(document.querySelectorAll('.input-label')).map(label => label.textContent);
        expect(labels).toEqual(['Expression:', 'Line number:', 'State:', 'Line number:']);
    });

    test('an action without params shows no inputs', async () => {
        const user = userEvent.setup();
        setupActions([{ name: 'DT', params: [] }]);

        await select(user, 'DT');
        await apply(user);

        expect(screen.getByText(/No additional inputs needed/i)).toBeInTheDocument();
        expect(screen.queryByRole('textbox')).not.toBeInTheDocument();
        expect(mockApplyAction).toHaveBeenCalledWith(
            props.logic, props.proof,
            { name: 'DT', sources: [], extraParameters: { expression: '' } },
            expect.any(Function)
        );
    });

    test('a state input is sent as the state, and only for actions that take one', async () => {
        const user = userEvent.setup();
        setupActions([{ name: 'Ass', params: ['EXPRESSION', 'STATE'] }, REP]);

        await select(user, 'Ass');
        await user.type(screen.getByLabelText(/Expression:/i), 'P');
        await user.type(screen.getByLabelText(/State:/i), 's0');
        await apply(user);
        expect(mockApplyAction).toHaveBeenLastCalledWith(
            props.logic, props.proof,
            { name: 'Ass', sources: [], extraParameters: { expression: 'P', state: 's0' } },
            expect.any(Function)
        );

        await select(user, 'Rep');
        await user.type(screen.getByLabelText(/Line number:/i), '2');
        await apply(user);
        expect(mockApplyAction).toHaveBeenLastCalledWith(
            props.logic, props.proof,
            { name: 'Rep', sources: [2], extraParameters: { expression: '' } },
            expect.any(Function)
        );
    });

    test('a state input takes a successor state such as s0+1 (modal-next-until) and sends it as typed', async () => {
        const user = userEvent.setup();
        setupActions([{ name: 'Ass', params: ['EXPRESSION', 'STATE'] }]);

        await select(user, 'Ass');
        await user.type(screen.getByLabelText(/Expression:/i), 'X p');
        await user.type(screen.getByLabelText(/State:/i), 's0+1');
        expect(screen.getByRole('button', { name: /Apply Rule/i })).toBeEnabled();
        await apply(user);

        expect(mockApplyAction).toHaveBeenLastCalledWith(
            props.logic, props.proof,
            { name: 'Ass', sources: [], extraParameters: { expression: 'X p', state: 's0+1' } },
            expect.any(Function)
        );
    });

    test('only the expression input gets the formula syntax help, not the state input', async () => {
        const user = userEvent.setup();
        setupActions([{ name: 'Ass', params: ['EXPRESSION', 'STATE'] }]);

        await select(user, 'Ass');
        const expression = screen.getByLabelText(/Expression:/i);
        const state = screen.getByLabelText(/State:/i);

        expect(expression).toHaveAttribute('placeholder', 'p -> q');
        expect(expression).toHaveAccessibleDescription(/Syntax:/);
        expect(state).not.toHaveAttribute('placeholder');
        expect(state).not.toHaveAttribute('aria-describedby');
        expect(screen.getAllByText(/^Syntax:/)).toHaveLength(1);
        expect(screen.getAllByRole('button', { name: 'Insert and (&)' })).toHaveLength(1);
    });

    test('line numbers skip over the other kinds of input', async () => {
        const user = userEvent.setup();
        setupActions([{ name: 'MIX', params: ['INT', 'EXPRESSION', 'INT'] }]);

        await select(user, 'MIX');
        const [first, second] = screen.getAllByLabelText(/Line number:/i);
        await user.type(second, '7');
        await user.type(first, '3');
        await user.type(screen.getByLabelText(/Expression:/i), 'Q');
        await apply(user);

        expect(mockApplyAction).toHaveBeenCalledWith(
            props.logic, props.proof,
            { name: 'MIX', sources: [3, 7], extraParameters: { expression: 'Q' } },
            expect.any(Function)
        );
    });

    test('does not refetch the actions when the props change', () => {
        mockFetchActions.mockImplementation((logic, callback) => callback([REP]));
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
        setupActions([REP]);

        await select(user, 'Rep');
        await user.type(screen.getByLabelText(/Line number:/i), '3');
        await apply(user);

        expect(screen.getByRole('alert')).toHaveTextContent('Line 3 does not exist');
        expect(props.setProof).not.toHaveBeenCalled();
    });

    describe('validation', () => {
        const sentSources = () => mockApplyAction.mock.calls.flatMap(([, , action]) => action.sources);

        test('Apply is disabled without a rule, with a hint that says why', () => {
            setupActions([REP]);

            expect(applyButton()).toBeDisabled();
            expect(applyButton()).toHaveAccessibleDescription('Choose a rule to apply.');
        });

        test('Apply stays disabled until every input is filled in, then the hint goes away', async () => {
            const user = userEvent.setup();
            setupActions([ORI1]);

            await select(user, 'ORI1');
            expect(applyButton()).toBeDisabled();
            expect(applyButton()).toHaveAccessibleDescription(/Fill in every input/);

            await user.type(screen.getByLabelText(/Line number:/i), '1');
            expect(applyButton()).toBeDisabled();
            await user.type(screen.getByLabelText(/Expression:/i), 'P');

            expect(applyButton()).toBeEnabled();
            expect(screen.queryByText(/Fill in every input/)).not.toBeInTheDocument();
        });

        test('a blank expression or state does not count as filled in', async () => {
            const user = userEvent.setup();
            setupActions([{ name: 'Ass', params: ['EXPRESSION', 'STATE'] }]);

            await select(user, 'Ass');
            await user.type(screen.getByLabelText(/Expression:/i), '   ');
            await user.type(screen.getByLabelText(/State:/i), 's0');
            expect(applyButton()).toBeDisabled();

            await user.type(screen.getByLabelText(/Expression:/i), 'P');
            expect(applyButton()).toBeEnabled();
            await user.clear(screen.getByLabelText(/State:/i));
            await user.type(screen.getByLabelText(/State:/i), ' ');
            expect(applyButton()).toBeDisabled();
        });

        test.each(['a', '1.5', '-2', '3a'])('the line number %j is flagged and never sent', async (text) => {
            const user = userEvent.setup();
            setupActions([REP]);

            await select(user, 'Rep');
            const input = screen.getByLabelText(/Line number:/i);
            await user.type(input, text);

            expect(input).toHaveAttribute('aria-invalid', 'true');
            expect(input).toHaveAccessibleDescription(/Enter a whole number/);
            expect(applyButton()).toBeDisabled();
            await apply(user);
            expect(mockApplyAction).not.toHaveBeenCalled();
            expect(screen.queryByText(/does not exist/)).not.toBeInTheDocument();
        });

        test.each(['0', '00', '9', '100'])('the line number %s is outside the eight lines of the proof', async (text) => {
            const user = userEvent.setup();
            setupActions([REP]);

            await select(user, 'Rep');
            const input = screen.getByLabelText(/Line number:/i);
            await user.type(input, text);

            expect(input).toHaveAttribute('aria-invalid', 'true');
            expect(input).toHaveAccessibleDescription('The proof has lines 1 to 8.');
            expect(applyButton()).toBeDisabled();
            await apply(user);
            expect(mockApplyAction).not.toHaveBeenCalled();
        });

        test.each(['1', '8', '08'])('the line number %s is inside the proof', async (text) => {
            const user = userEvent.setup();
            setupActions([REP]);

            await select(user, 'Rep');
            const input = screen.getByLabelText(/Line number:/i);
            await user.type(input, text);

            expect(input).not.toHaveAttribute('aria-invalid');
            expect(applyButton()).toBeEnabled();
        });

        test('a proof without lines has no line to refer to', async () => {
            const user = userEvent.setup();
            mockFetchActions.mockImplementation((logic, callback) => callback([REP]));
            render(<Menu {...props} proof={{ ...mockProof, steps: [] }} />);

            await select(user, 'Rep');
            await user.type(screen.getByLabelText(/Line number:/i), '1');

            expect(screen.getByLabelText(/Line number:/i)).toHaveAccessibleDescription('The proof has no lines yet.');
            expect(applyButton()).toBeDisabled();
        });

        test('one bad line number is enough to disable Apply', async () => {
            const user = userEvent.setup();
            setupActions([{ name: 'ANDI', params: ['INT', 'INT'] }]);

            await select(user, 'ANDI');
            const [first, second] = screen.getAllByLabelText(/Line number:/i);
            await user.type(first, '1');
            await user.type(second, 'x');
            expect(applyButton()).toBeDisabled();

            await user.clear(second);
            await user.type(second, '2');
            expect(applyButton()).toBeEnabled();
            await apply(user);

            expect(sentSources()).toEqual([1, 2]);
        });

        test('an action without inputs can be applied at once', async () => {
            const user = userEvent.setup();
            setupActions([{ name: 'DT', params: [] }]);

            await select(user, 'DT');

            expect(applyButton()).toBeEnabled();
            expect(screen.queryByText(/Fill in every input/)).not.toBeInTheDocument();
        });
    });

    test('shows nothing to apply a rule to before a proof exists', () => {
        mockFetchActions.mockImplementation((logic, callback) => callback([REP]));
        render(<Menu {...props} proof={{ steps: [], logic: 'mock-logic', goal: '' }} />);

        expect(screen.queryByLabelText(/Select Inference Rule:/i)).not.toBeInTheDocument();
        expect(screen.queryByText(/New Proof/)).not.toBeInTheDocument();
    });
});
