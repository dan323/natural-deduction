import { checkFormula, renderExpression, renderRule } from '../utils';

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

describe('checkFormula', () => {
  test.each([
    'p',
    'P -> Q',
    '-p',
    '--p',
    'p -> -q',
    '(p & q) | r',
    '((p))',
    '  p&q  ',
    'p -> (q -> p)',
    '[]p -> <>q',
    'x <= y',
    'x = y1',
    'p_1 & q_2',
  ])('accepts %j', (formula) => {
    expect(checkFormula(formula)).toBeNull();
  });

  test.each(['', '   '])('rejects a blank formula %j', (formula) => {
    expect(checkFormula(formula)).toMatch(/blank/);
  });

  test.each(['p ->', 'q &&', 'p |', 'p &', 'p -> q ->', '-', 'p = '])('rejects the dangling operator in %j', (formula) => {
    expect(checkFormula(formula)).not.toBeNull();
  });

  test.each(['-> p', '& q', '| p', '(& p)'])('rejects the operator without a left operand in %j', (formula) => {
    expect(checkFormula(formula)).toMatch(/missing its left operand/);
  });

  test.each(['(p', '((p & q)', 'p)', ')p(', '(p))'])('rejects the unbalanced parentheses in %j', (formula) => {
    expect(checkFormula(formula)).toMatch(/Unbalanced/);
  });

  test.each(['()', '(p &)', '( )'])('rejects the empty or unfinished group in %j', (formula) => {
    expect(checkFormula(formula)).not.toBeNull();
  });

  test.each(['p q', 'p (q)', '(p) q', 'p -q'])('rejects the missing operator in %j', (formula) => {
    expect(checkFormula(formula)).toMatch(/Missing operator/);
  });

  test.each(['p $ q', 'p ; q', 'p > q', 'p <-> q', 'p ∧ q', 'p.q'])('rejects the unexpected symbol in %j', (formula) => {
    expect(checkFormula(formula)).toMatch(/Unexpected symbol/);
  });
});
