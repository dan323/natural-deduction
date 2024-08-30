import React from 'react';
import { render } from '@testing-library/react';
import '@testing-library/jest-dom/extend-expect';
import Goal from './Goal';
import { renderLogic } from '../../service/utils';

// Mock the renderLogic function
jest.mock('../../service/utils', () => ({
  renderLogic: jest.fn(),
}));

describe('Goal Component', () => {
  const expression = 'A → B';

  beforeEach(() => {
    (renderLogic as jest.Mock).mockReturnValue(expression);
  });

  test('renders the goal expression correctly', () => {
    const { getByText } = render(<Goal expression={expression} success={false} />);
    
    // Check if the expression is rendered correctly
    expect(getByText(expression)).toBeInTheDocument();
  });

  test('applies the correct class and style when success is true', () => {
    const { getByText } = render(<Goal expression={expression} success={true} />);
    
    // Check if the success class is applied
    const expressionElement = getByText(expression);
    expect(expressionElement).toHaveClass('goal-success');
    expect(expressionElement).not.toHaveClass('goal-failure');
  });

  test('applies the correct class and style when success is false', () => {
    const { getByText } = render(<Goal expression={expression} success={false} />);
    
    // Check if the failure class is applied
    const expressionElement = getByText(expression);
    expect(expressionElement).toHaveClass('goal-failure');
    expect(expressionElement).not.toHaveClass('goal-success');
  });

  test('renders "GOAL:" label correctly', () => {
    const { getByText } = render(<Goal expression={expression} success={false} />);
    
    // Check if the GOAL label is rendered correctly
    expect(getByText('GOAL:')).toBeInTheDocument();
  });

  test('uses the renderLogic function to render the expression', () => {
    render(<Goal expression={expression} success={false} />);
    
    // Check if renderLogic was called wtesth the correct expression
    expect(renderLogic).toHaveBeenCalledWith(expression);
  });
});
