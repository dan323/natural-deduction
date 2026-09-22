import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import StepViewer from '../StepViewer';
import { StepDto } from '../../../types';

const mockStep: StepDto = {
  expression: 'A → B',
  rule: '->I',
  assmsLevel: 0,
  extraParameters: {}
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
    expect(screen.getByText('→I')).toBeInTheDocument();
    expect(screen.getByRole('rowheader')).toHaveTextContent('cited by current input');
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
    expect(screen.getByRole('rowheader')).not.toHaveTextContent('cited by current input');
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
    expect(screen.getByRole('rowheader')).not.toHaveTextContent('cited by current input');
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

  describe('subproof structure', () => {
    const renderStep = (assmsLevel: number, openLevels?: number) => render(<table><tbody>
      <StepViewer
        step={{ ...mockStep, assmsLevel }}
        stepIndex={2}
        onMouseEnter={mockOnMouseEnter}
        onMouseLeave={mockOnMouseLeave}
        openLevels={openLevels}
      /></tbody></table>
    );

    test('the line number is the header of its row and says the assumption level', () => {
      renderStep(2);

      const header = screen.getByRole('rowheader');
      expect(header).toHaveTextContent('3, assumption level 2');
      expect(header).toHaveAttribute('scope', 'row');
    });

    test('a step outside every subproof is at assumption level 0', () => {
      const { container } = renderStep(0);

      expect(screen.getByRole('rowheader')).toHaveTextContent('3, assumption level 0');
      expect(container.querySelectorAll('.subproof-rule')).toHaveLength(0);
    });

    test('is indented with one rule per assumption level, and not with tabs', () => {
      const { container } = renderStep(3);

      const rules = container.querySelectorAll('.subproof-rule');
      expect(rules).toHaveLength(3);
      rules.forEach(rule => {
        expect(rule).not.toHaveClass('closed');
        expect(rule).toHaveAttribute('aria-hidden', 'true');
      });
      expect(container.querySelector('pre')?.textContent).toBe('A → B');
    });

    test('the rules of the levels that were closed are marked, and the step is discharged', () => {
      const { container } = renderStep(3, 1);

      const rules = Array.from(container.querySelectorAll('.subproof-rule'));
      expect(rules.map(rule => rule.classList.contains('closed'))).toEqual([false, true, true]);
      expect(screen.getByRole('row')).toHaveClass('discharged');
      expect(screen.getByRole('rowheader')).toHaveTextContent('3, assumption level 3, discharged');
    });

    test('a step whose levels are all open is not discharged', () => {
      renderStep(2, 2);

      expect(screen.getByRole('row')).not.toHaveClass('discharged');
      expect(screen.getByRole('rowheader')).not.toHaveTextContent('discharged');
    });
  });
});
