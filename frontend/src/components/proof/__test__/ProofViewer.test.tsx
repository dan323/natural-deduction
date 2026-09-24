import { act, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ProofViewer from '../ProofViewer';  // Adjust the import path as necessary
import { ProofDto, StepDto } from '../../../types';

const mockSteps: StepDto[] = [
  { expression: 'A -> B', rule: '->I [1-2]', assmsLevel: 0, extraParameters: {}},
  { expression: 'A', rule: 'Assumption', assmsLevel: 1, extraParameters: {} },
  { expression: 'B', rule: '->E [2, 3]', assmsLevel: 1, extraParameters: {} },
  { expression: 'A | B', rule: 'Rep [1]', assmsLevel: 0, extraParameters: {} },
];

const expressionsShown: string[] = ['A → B','A','B','A ∨ B']
const rulesShown: string[] = ['→I [1-2]','Assumption','→E [2, 3]', 'Rep [1]'];

const mockProof: ProofDto = {
  steps: mockSteps,
  goal: 'A | B',
  logic: 'classic',
  done: true,
};

const goalExpression: string = 'A ∨ B';

const mockColoring = new Map<number, string>([
  [0, 'blue'],
  [1, 'red'],
  [2, 'green'],
]);

describe('ProofViewer Component', () => {
  test('renders all steps and goal correctly', () => {
    render(<ProofViewer proof={mockProof} coloring={mockColoring} />);

    // Check if all steps are rendered with correct content
    mockSteps.forEach((step, index) => {
      expect(screen.getByText(expressionsShown[index], { selector: 'pre' })).toBeInTheDocument();
      expect(screen.getByText(rulesShown[index])).toBeInTheDocument();
    });

    // Check if the goal is rendered
    expect(screen.getByText(goalExpression, { selector: 'span' })).toBeInTheDocument();
  });

  test('applies correct colors to steps based on coloring map', () => {
    render(<ProofViewer proof={mockProof} coloring={mockColoring} />);

    const rows = screen.getAllByRole('row');

    // Assert correct coloring
    expect(rows[1]).toHaveStyle('--glow-color: blue');
    expect(rows[2]).toHaveStyle('--glow-color: red');
    expect(rows[3]).toHaveStyle('--glow-color: green');
  });

  test('highlights steps based on hovered rule', async () => {
    render(<ProofViewer proof={mockProof} coloring={mockColoring} />);

    const rows = screen.getAllByRole('row');

    // Hover over the first step
    await userEvent.hover(rows[1]);
    
    // Check if the range [1-2] is highlighted (steps 1 and 2)
    expect(rows[1]).toHaveClass('highlighted');
    expect(rows[2]).toHaveClass('highlighted');
  });

  test('removes highlighting when mouse leaves step', async () => {
    render(<ProofViewer proof={mockProof} coloring={mockColoring} />);

    const rows = screen.getAllByRole('row');

    // Hover over the first step and then unhover
    await userEvent.hover(rows[1]);
    await userEvent.unhover(rows[1]);

    // Check if the highlighting is removed
    expect(rows[2]).not.toHaveClass('highlighted');
    expect(rows[3]).not.toHaveClass('highlighted');
  });

  test('goal success is what the backend reports in done', () => {
    render(<ProofViewer proof={mockProof} coloring={mockColoring} />);

    const goalElement = screen.getByText(goalExpression, {selector: 'span'});
    expect(goalElement).toHaveClass('goal-success');
  });

  test('the goal is not marked as achieved unless done, even when the last step is the goal', () => {
    render(<ProofViewer proof={{ ...mockProof, done: false }} coloring={mockColoring} />);

    expect(screen.getByText(goalExpression, {selector: 'span'})).toHaveClass('goal-failure');
  });

  test('the goal is achieved when done, even when the last step is not the goal', () => {
    const goalInTheMiddle = { ...mockProof, steps: [mockSteps[3], mockSteps[0]], done: true };
    render(<ProofViewer proof={goalInTheMiddle} coloring={mockColoring} />);

    expect(screen.getByText(goalExpression, {selector: 'span'})).toHaveClass('goal-success');
  });

  test('a proof that was not returned by the backend is not done', () => {
    const { done, ...local } = mockProof;
    render(<ProofViewer proof={local} coloring={mockColoring} />);

    expect(screen.getByText(goalExpression, {selector: 'span'})).toHaveClass('goal-failure');
  });

  test('highlights the cited steps when a row gets the focus, and stops when it loses it', () => {
    render(<ProofViewer proof={mockProof} coloring={mockColoring} />);
    const rows = screen.getAllByRole('row');

    act(() => rows[1].focus());
    expect(rows[1]).toHaveClass('highlighted');
    expect(rows[2]).toHaveClass('highlighted');
    expect(rows[3]).not.toHaveClass('highlighted');

    act(() => rows[1].blur());
    expect(rows[1]).not.toHaveClass('highlighted');
    expect(rows[2]).not.toHaveClass('highlighted');
  });

  test('every step can be reached with the Tab key', async () => {
    render(<ProofViewer proof={mockProof} coloring={mockColoring} />);
    const rows = screen.getAllByRole('row').slice(1);

    for (const row of rows) {
      await userEvent.tab();
      expect(row).toHaveFocus();
    }
  });

  test('reports the 1-based line of the row that is clicked, or activated with Enter or Space', async () => {
    const onSelectLine = jest.fn();
    render(<ProofViewer proof={mockProof} coloring={mockColoring} onSelectLine={onSelectLine} />);
    const rows = screen.getAllByRole('row');

    await userEvent.click(rows[3]);
    expect(onSelectLine).toHaveBeenLastCalledWith(3);

    act(() => rows[2].focus());
    await userEvent.keyboard('{Enter}');
    expect(onSelectLine).toHaveBeenLastCalledWith(2);

    await userEvent.keyboard(' ');
    expect(onSelectLine).toHaveBeenCalledTimes(3);
    expect(onSelectLine).toHaveBeenLastCalledWith(2);

    await userEvent.keyboard('a');
    expect(onSelectLine).toHaveBeenCalledTimes(3);
  });

  describe('subproof structure', () => {
    // 1 P (0) | 2 A (1) | 3 B (2) | 4 C (2) | 5 B->C (1) | 6 A->(B->C) (0) | 7 D (1)
    const step = (assmsLevel: number, expression: string): StepDto =>
      ({ expression, rule: 'Rep', assmsLevel, extraParameters: {} });
    const nested: ProofDto = {
      ...mockProof,
      steps: [step(0, 'P'), step(1, 'A'), step(2, 'B'), step(2, 'C'), step(1, 'B -> C'), step(0, 'A -> B -> C'), step(1, 'D')],
    };

    test('every line number is a row header that says its assumption level', () => {
      render(<ProofViewer proof={nested} coloring={mockColoring} />);

      // mockColoring colors lines 1-3, and StepViewer now appends a text equivalent of the
      // glow to a colored row's header, so those three rows say "cited by current input" too.
      const headers = screen.getAllByRole('rowheader').map(header => header.textContent);
      expect(headers).toEqual([
        '1, assumption level 0, cited by current input',
        '2, assumption level 1, discharged, cited by current input',
        '3, assumption level 2, discharged, cited by current input',
        '4, assumption level 2, discharged',
        '5, assumption level 1, discharged',
        '6, assumption level 0',
        '7, assumption level 1',
      ]);
    });

    test('a level is closed by a later step at a lower level, and only that level', () => {
      render(<ProofViewer proof={nested} coloring={mockColoring} />);
      const rows = screen.getAllByRole('row').slice(1);
      const closedRules = (row: HTMLElement) =>
        Array.from(row.querySelectorAll('.subproof-rule')).map(rule => rule.classList.contains('closed'));

      expect(closedRules(rows[0])).toEqual([]);
      expect(closedRules(rows[1])).toEqual([true]);
      expect(closedRules(rows[2])).toEqual([true, true]);
      expect(closedRules(rows[4])).toEqual([true]);
      expect(closedRules(rows[6])).toEqual([false]);
    });

    test('an inner subproof that was discharged leaves the outer one open', () => {
      const inner: ProofDto = { ...mockProof, steps: [step(1, 'A'), step(2, 'B'), step(2, 'C'), step(1, 'B -> C')] };
      render(<ProofViewer proof={inner} coloring={mockColoring} />);
      const rows = screen.getAllByRole('row').slice(1);
      const closedRules = (row: HTMLElement) =>
        Array.from(row.querySelectorAll('.subproof-rule')).map(rule => rule.classList.contains('closed'));

      expect(closedRules(rows[0])).toEqual([false]);
      expect(closedRules(rows[1])).toEqual([false, true]);
      expect(closedRules(rows[3])).toEqual([false]);
      expect(rows[0]).not.toHaveClass('discharged');
      expect(rows[1]).toHaveClass('discharged');
      expect(rows[3]).not.toHaveClass('discharged');
    });
  });

  test('a modal proof has a State column that says which state each step is in', () => {
    const modal: ProofDto = {
      steps: [
        { expression: '[] p', rule: 'Ass', assmsLevel: 0, extraParameters: { state: 's0' } },
        { expression: 's0 <= s1', rule: 'Ass', assmsLevel: 0, extraParameters: {} },
        { expression: 'p', rule: '[]E [1, 2]', assmsLevel: 0, extraParameters: { state: 's1' } },
      ],
      goal: 'p',
      logic: 'modal',
    };
    render(<ProofViewer proof={modal} coloring={new Map()} />);

    expect(screen.getAllByRole('columnheader').map((header) => header.textContent)).toEqual(['Line', 'State', 'Step', 'Rule']);
    const states = screen.getAllByRole('row').slice(1).map((row) => row.querySelector('td.state')?.textContent);
    expect(states).toEqual(['s0', '–none (a relation between states)', 's1']);
  });

  test('a classical proof has no State column', () => {
    render(<ProofViewer proof={mockProof} coloring={mockColoring} />);

    expect(screen.getAllByRole('columnheader').map((header) => header.textContent)).toEqual(['Line', 'Step', 'Rule']);
  });
});
