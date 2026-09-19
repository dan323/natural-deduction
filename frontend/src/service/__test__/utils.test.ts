import { renderExpression, renderRule } from '../utils';

describe('renderExpression', () => {
  test('renders logical operators', () => {
    expect(renderExpression('A -> B')).toBe('A → B');
    expect(renderExpression('-A & B | C')).toBe('¬A ∧ B ∨ C');
    expect(renderExpression('[]A -> <>B')).toBe('□A → ◇B');
  });

  test('does not touch variables named I or E', () => {
    expect(renderExpression('E -> I')).toBe('E → I');
    expect(renderExpression('-E & I')).toBe('¬E ∧ I');
  });
});

describe('renderRule', () => {
  test('renders introduction and elimination rules', () => {
    expect(renderRule('->I')).toBe('→Intro');
    expect(renderRule('&E1')).toBe('∧Elim1');
    expect(renderRule('->I [1-2]')).toBe('→Intro [1-2]');
  });

  test('keeps FALSE elimination rules distinguishable', () => {
    expect(renderRule('FALSE')).toBe('FALSE');
  });
});
