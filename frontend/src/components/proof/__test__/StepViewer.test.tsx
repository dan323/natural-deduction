import React from 'react';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import StepViewer from '../StepViewer';
import { StepDto } from '../../../types';

const mockStep: StepDto = {
  expression: 'A → B',
  rule: '->I',
  assmsLevel: 0,
  extraParameters: new Map()
};

const mockOnMouseEnter = jest.fn();
const mockOnMouseLeave = jest.fn();

describe('StepViewer Component', () => {
  test('renders correctly with valid color', () => {
    render(<table><tbody>
      <StepViewer
        step={mockStep}
        stepIndex={0}
        onMouseEnter={mockOnMouseEnter}
        onMouseLeave={mockOnMouseLeave}
        color="blue"
      /></tbody></table>
    );

    const row = screen.getByRole('row');
    expect(row).toHaveClass('glow');
    expect(row).toHaveStyle('--glow-color: blue');
    expect(screen.getByText('1')).toBeInTheDocument();
    expect(screen.getByText('A → B')).toBeInTheDocument();
    expect(screen.getByText('→Intro')).toBeInTheDocument();
  });

  test('does not apply glow when color is invalid', () => {
    render(<table><tbody>
      <StepViewer
        step={mockStep}
        stepIndex={0}
        onMouseEnter={mockOnMouseEnter}
        onMouseLeave={mockOnMouseLeave}
        color="invalid-color"
      /></tbody></table>
    );

    const row = screen.getByRole('row');
    expect(row).not.toHaveClass('glow');
    expect(row).not.toHaveStyle('--glow-color: invalid-color');
  });

  test('does not apply glow when color is not provided', () => {
    render(<table><tbody>
      <StepViewer
        step={mockStep}
        stepIndex={0}
        onMouseEnter={mockOnMouseEnter}
        onMouseLeave={mockOnMouseLeave}
      /></tbody></table>
    );

    const row = screen.getByRole('row');
    expect(row).not.toHaveClass('glow');
    expect(row).not.toHaveStyle('--glow-color: blue');
  });

  test('calls onMouseEnter and onMouseLeave handlers', async () => {
    render(<table><tbody>
      <StepViewer
        step={mockStep}
        stepIndex={0}
        onMouseEnter={mockOnMouseEnter}
        onMouseLeave={mockOnMouseLeave}
        color="blue"
      /></tbody></table>
    );

    const row = screen.getByRole('row');

    await userEvent.hover(row);
    expect(mockOnMouseEnter).toHaveBeenCalledTimes(1);

    await userEvent.unhover(row);
    expect(mockOnMouseLeave).toHaveBeenCalledTimes(1);
  });
});
