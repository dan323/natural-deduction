import { render, screen, within } from '@testing-library/react';
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

const MP: ActionDescriptor = {
    name: 'MP', params: ['INT', 'INT'], label: 'Modus ponens', symbol: '→E', category: 'ELIMINATION',
    description: 'From A → B and A, derive B', paramLabels: ['Implication (A → B)', 'Antecedent (A)'],
};
const ANDI: ActionDescriptor = {
    name: 'ANDI', params: ['INT', 'INT'], label: 'And introduction', symbol: '∧I', category: 'INTRODUCTION',
    description: 'From A and B, derive A ∧ B', paramLabels: ['Line with A', 'Line with B'],
};
const ASSUME: ActionDescriptor = {
    name: 'ASSUME', params: ['EXPRESSION'], label: 'Assumption', symbol: 'Ass', category: 'OTHER',
    description: 'Assume A, opening a new subproof', paramLabels: ['Assumption (A)'],
};
const DT: ActionDescriptor = { name: 'DT', params: [], label: 'Deduction theorem', symbol: '→I', category: 'INTRODUCTION', description: 'Close the last assumption' };
const PLAIN: ActionDescriptor = { name: 'Rep', params: ['INT'] };

const mockProof: ProofDto = {
    steps: Array.from({ length: 4 }, () => ({ expression: 'P', rule: 'Ass', assmsLevel: 0, extraParameters: {} })),
    logic: 'mock-logic',
    goal: 'P',
};

describe('Menu rule labels', () => {
    const setupActions = (actions: ActionDescriptor[]) => {
        mockFetchActions.mockImplementation((logic, callback) => callback(actions));
        render(<Menu logic="mock-logic" onColorChange={jest.fn()} setProof={jest.fn()} proof={mockProof} />);
    };

    const ruleSelect = () => screen.getByLabelText(/Select Inference Rule:/i);

    const select = async (user: ReturnType<typeof userEvent.setup>, action: string) =>
        user.selectOptions(ruleSelect(), action);

    beforeEach(() => {
        jest.clearAllMocks();
    });

    test('groups the rules by category and shows the label with the symbol', () => {
        setupActions([MP, ASSUME, ANDI]);

        const groups = screen.getAllByRole('group');
        expect(groups.map(group => group.getAttribute('label'))).toEqual(['Introduction rules', 'Elimination rules', 'Other rules']);
        expect(within(groups[0]).getByRole('option', { name: 'And introduction (∧I)' })).toHaveValue('ANDI');
        expect(within(groups[1]).getByRole('option', { name: 'Modus ponens (→E)' })).toHaveValue('MP');
        expect(within(groups[2]).getByRole('option', { name: 'Assumption (Ass)' })).toHaveValue('ASSUME');
    });

    test('omits the groups that have no rule', () => {
        setupActions([MP]);

        expect(screen.getAllByRole('group').map(group => group.getAttribute('label'))).toEqual(['Elimination rules']);
    });

    test('shows the description of the selected rule under the select', async () => {
        const user = userEvent.setup();
        setupActions([MP, PLAIN]);
        expect(screen.queryByText('From A → B and A, derive B')).not.toBeInTheDocument();
        expect(ruleSelect()).not.toHaveAttribute('aria-describedby');

        await select(user, 'MP');
        const description = screen.getByText('From A → B and A, derive B');
        expect(ruleSelect()).toHaveAttribute('aria-describedby', description.id);
        expect(ruleSelect()).toHaveAccessibleDescription('From A → B and A, derive B');

        await select(user, 'Rep');
        expect(screen.queryByText('From A → B and A, derive B')).not.toBeInTheDocument();
        expect(ruleSelect()).not.toHaveAttribute('aria-describedby');
    });

    test('labels each input with the label of its param, in order', async () => {
        const user = userEvent.setup();
        setupActions([MP, ASSUME]);

        await select(user, 'MP');
        const inputs = screen.getAllByRole('textbox');
        expect(inputs[0]).toHaveAccessibleName('Implication (A → B)');
        expect(inputs[1]).toHaveAccessibleName('Antecedent (A)');

        await select(user, 'ASSUME');
        expect(screen.getByLabelText('Assumption (A)')).toBeInTheDocument();
    });

    test('an action with a description but without params shows no inputs', async () => {
        const user = userEvent.setup();
        setupActions([DT]);

        await select(user, 'DT');

        expect(screen.getByText('Close the last assumption')).toBeInTheDocument();
        expect(screen.getByText(/No additional inputs needed/i)).toBeInTheDocument();
    });

    test('falls back to the name and the generic labels when the backend sends nothing', async () => {
        const user = userEvent.setup();
        setupActions([PLAIN, { name: 'Ass', params: ['EXPRESSION', 'STATE'], label: null, symbol: null, category: null, description: null, paramLabels: [] }]);

        expect(screen.queryAllByRole('group')).toHaveLength(0);
        expect(screen.getByRole('option', { name: 'Rep' })).toHaveValue('Rep');
        await select(user, 'Rep');
        expect(screen.getByLabelText('Line number:')).toBeInTheDocument();
        expect(ruleSelect()).not.toHaveAttribute('aria-describedby');

        await select(user, 'Ass');
        expect(screen.getByLabelText('Expression:')).toBeInTheDocument();
        expect(screen.getByLabelText('State:')).toBeInTheDocument();
    });

    test('shows a label without symbol, and mixes grouped and ungrouped rules', () => {
        setupActions([
            { name: 'Special', params: [], label: 'Special rule' },
            { name: 'Odd', params: [], label: 'Odd rule', symbol: 'Ø', category: 'FUTURE' as never },
            MP,
        ]);

        expect(screen.getByRole('option', { name: 'Special rule' })).toHaveValue('Special');
        expect(screen.getByRole('option', { name: 'Odd rule (Ø)' })).toHaveValue('Odd');
        const [group] = screen.getAllByRole('group');
        expect(within(group).getAllByRole('option')).toHaveLength(1);
        expect(within(ruleSelect()).getAllByRole('option').map(option => option.textContent))
            .toEqual(['-- Choose a rule --', 'Special rule', 'Odd rule (Ø)', 'Modus ponens (→E)']);
    });

    describe('when a rule is rejected', () => {
        const rejectWith = (response: object) => mockApplyAction.mockImplementation((logic, proof, action, callback) => callback(response));

        const applyMp = async (user: ReturnType<typeof userEvent.setup>) => {
            await select(user, 'MP');
            await user.type(screen.getByLabelText('Implication (A → B)'), '2');
            await user.type(screen.getByLabelText('Antecedent (A)'), '1');
            await user.click(screen.getByRole('button', { name: /Apply Rule/i }));
        };

        test('appends what the rule does to the reason of a 202', async () => {
            const user = userEvent.setup();
            setupActions([MP]);
            rejectWith({ proof: mockProof, success: false, message: 'Rule MP cannot be applied to lines [2, 1]' });

            await applyMp(user);

            expect(screen.getByRole('alert')).toHaveTextContent('Rule MP cannot be applied to lines [2, 1]. From A → B and A, derive B.');
        });

        test('does not repeat a full stop that is already there', async () => {
            const user = userEvent.setup();
            setupActions([MP]);
            rejectWith({ proof: mockProof, success: false, message: 'Not applicable.' });

            await applyMp(user);

            expect(screen.getByRole('alert')).toHaveTextContent(/^Not applicable\. From A → B and A, derive B\.$/);
        });

        test('keeps the message of a failure that is not about the rule', async () => {
            const user = userEvent.setup();
            setupActions([MP]);
            rejectWith({ success: false, message: 'Request failed with status 500.' });

            await applyMp(user);

            expect(screen.getByRole('alert')).toHaveTextContent(/^Request failed with status 500\.$/);
        });

        test('keeps the message of a rule without description', async () => {
            const user = userEvent.setup();
            setupActions([PLAIN]);
            rejectWith({ proof: mockProof, success: false, message: '' });

            await select(user, 'Rep');
            await user.type(screen.getByLabelText('Line number:'), '1');
            await user.click(screen.getByRole('button', { name: /Apply Rule/i }));

            expect(screen.getByRole('alert')).toHaveTextContent(/^Action could not be applied\.$/);
        });
    });
});
