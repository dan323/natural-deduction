import { act, render } from '@testing-library/react';
import Goal from '../Goal';
import { renderExpression } from '../../../service/utils';

// Mock the renderExpression function
jest.mock('../../../service/utils', () => ({
  renderExpression: jest.fn(),
}));

describe('Goal Component', () => {
  const expression = 'A → B';

  beforeEach(() => {
    (renderExpression as jest.Mock).mockReturnValue(expression);
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

  test('uses the renderExpression function to render the expression', () => {
    render(<Goal expression={expression} success={false} />);
    
    // Check if renderExpression was called wtesth the correct expression
    expect(renderExpression).toHaveBeenCalledWith(expression);
  });
});

describe('Goal marker', () => {
  beforeEach(() => {
    (renderExpression as jest.Mock).mockReturnValue('A → B');
  });

  test('says in text that the goal is proved, not only in colour', () => {
    const { getByText, queryByText } = render(<Goal expression="A → B" success={true} />);

    expect(getByText('✓ Proved')).toBeInTheDocument();
    expect(queryByText('Not proved yet')).toBeNull();
  });

  test('says in text that the goal is not proved yet', () => {
    const { getByText, queryByText } = render(<Goal expression="A → B" success={false} />);

    expect(getByText('Not proved yet')).toBeInTheDocument();
    expect(queryByText('✓ Proved')).toBeNull();
  });
});

describe('Goal celebration', () => {
  beforeEach(() => {
    (renderExpression as jest.Mock).mockReturnValue('A → B');
    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.useRealTimers();
    jest.restoreAllMocks();
  });

  test('rolls the confetti once, not on every render', () => {
    const random = jest.spyOn(Math, 'random');
    const { container, rerender } = render(<Goal expression="A → B" success={false} />);
    expect(container.querySelector('.celebration')).toBeNull();

    rerender(<Goal expression="A → B" success={true} />);
    expect(container.querySelectorAll('.emoji')).toHaveLength(30);
    const rolled = random.mock.calls.length;
    const before = Array.from(container.querySelectorAll('.emoji')).map(e => e.outerHTML);

    rerender(<Goal expression="A → B" success={true} />);
    expect(random.mock.calls.length).toBe(rolled);
    expect(Array.from(container.querySelectorAll('.emoji')).map(e => e.outerHTML)).toEqual(before);
  });

  test('keeps the confetti away from assistive technology', () => {
    const { container } = render(<Goal expression="A → B" success={true} />);

    expect(container.querySelector('.celebration')).toHaveAttribute('aria-hidden', 'true');
  });

  test('drops the confetti at once when the goal is no longer proved', () => {
    const { container, rerender } = render(<Goal expression="A → B" success={true} />);
    expect(container.querySelector('.celebration')).not.toBeNull();

    rerender(<Goal expression="A → B" success={false} />);

    expect(container.querySelector('.celebration')).toBeNull();
  });

  test('hides the confetti after two seconds', () => {
    const { container } = render(<Goal expression="A → B" success={true} />);
    expect(container.querySelector('.celebration')).not.toBeNull();

    act(() => { jest.advanceTimersByTime(2000); });

    expect(container.querySelector('.celebration')).toBeNull();
  });
});
