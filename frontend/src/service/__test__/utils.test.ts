import { checkFormula, proofToText, renderExpression, renderRule } from '../utils';

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
  test('renders the symbols of a rule and keeps its lines', () => {
    expect(renderRule('->I')).toBe('→I');
    expect(renderRule('&E1')).toBe('∧E1');
    expect(renderRule('->I [1-2]')).toBe('→I [1-2]');
    expect(renderRule('->E [1, 2]')).toBe('→E [1, 2]');
  });

  test('renders falsum introduction and elimination with ⊥', () => {
    expect(renderRule('FE [3]')).toBe('⊥E [3]');
    expect(renderRule('FI [1, 2]')).toBe('⊥I [1, 2]');
  });

  test('leaves other names alone', () => {
    expect(renderRule('FALSE')).toBe('FALSE');
    expect(renderRule('Rep [1]')).toBe('Rep [1]');
    expect(renderRule('Ass')).toBe('Ass');
  });

  // The backend writes these rule texts in StepDto.rule; the `symbol` of the classical action descriptors is the
  // rendered one (ClassicalUseTest holds the same table), so the list of rules and the Rule column name a rule alike.
  test.each([
    ['Ass', 'Ass'], ['|I', '∨I'], ['|E', '∨E'], ['&I', '∧I'], ['&E', '∧E'], ['Rep', 'Rep'], ['-E', '¬E'],
    ['-I', '¬I'], ['->I', '→I'], ['->E', '→E'], ['FE', '⊥E'], ['FI', '⊥I'],
  ])('renders the rule text %s as the symbol %s', (ruleText, symbol) => {
    expect(renderRule(ruleText)).toBe(symbol);
    expect(renderRule(`${ruleText} [1, 2]`)).toBe(`${symbol} [1, 2]`);
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

describe('proofToText', () => {
  test('writes each step as ProofStep.toString() does: 3 spaces per level, the expression, 11 spaces, the rule', () => {
    const text = proofToText({
      steps: [
        { expression: 'P', rule: 'Ass', assmsLevel: 0, extraParameters: {} },
        { expression: 'Q', rule: 'Ass', assmsLevel: 1, extraParameters: {} },
        { expression: 'P', rule: 'Rep [1]', assmsLevel: 1, extraParameters: {} },
        { expression: 'Q -> P', rule: '->I [2-3]', assmsLevel: 0, extraParameters: {} },
      ],
      logic: 'classical',
      goal: 'Q -> P',
    });

    expect(text.split('\n')).toEqual([
      'P           Ass',
      '   Q           Ass',
      '   P           Rep [1]',
      'Q -> P           ->I [2-3]',
    ]);
  });

  test('an empty proof is an empty text', () => {
    expect(proofToText({ steps: [], logic: 'classical', goal: 'P' })).toBe('');
  });
});
