import React from 'react';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ProofViewer from '../components/proof/ProofViewer';  // Adjust the import path as necessary
import { ProofDto, StepDto } from '../types';

const mockSteps: StepDto[] = [
  { expression: 'A -> B', rule: '->I [1-2]', assmsLevel: 0, extraParameters: new Map()},
  { expression: 'A', rule: 'Assumption', assmsLevel: 1, extraParameters: new Map() },
  { expression: 'B', rule: '->E [2, 3]', assmsLevel: 1, extraParameters: new Map() },
  { expression: 'A | B', rule: 'Rep [1]', assmsLevel: 0, extraParameters: new Map() },
];

const expressionsShown: string[] = ['A → B','A','B','A ∨ B']
const rulesShown: string[] = ['→Intro [1-2]','Assumption','→Elim [2, 3]', 'Rep [1]'];

const mockProof: ProofDto = {
  steps: mockSteps,
  goal: 'A | B',
  logic: 'classic'
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

  test('goal success is correctly determined', () => {
    render(<ProofViewer proof={mockProof} coloring={mockColoring} />);

    // Check if the goal is marked as achieved (based on success prop)
    const goalElement = screen.getByText(goalExpression, {selector: 'span'});
    expect(goalElement).toHaveClass('goal-success');
  });
});
