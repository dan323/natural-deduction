import { checkFormula, isRelationFormula, loadedGoal, loadsBackWithSameGoal, proofToText, renderExpression, renderRule } from '../utils';

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

  test('premises as typed, with spaces around them, are written without the spaces', () => {
    const text = proofToText({
      steps: [{ expression: '   P ', rule: 'Ass', assmsLevel: 0, extraParameters: {} }],
      logic: 'classical',
      goal: 'P',
    });

    expect(text).toBe('P           Ass');
  });

  test('a modal step starts with its state, before the indent, as ProofStepModal.toString() prints it; a relation has none', () => {
    const text = proofToText({
      steps: [
        { expression: '[] p', rule: 'Ass', assmsLevel: 0, extraParameters: { state: 's0' } },
        { expression: 's0 <= s1', rule: 'Ass', assmsLevel: 0, extraParameters: {} },
        { expression: 'q', rule: 'Ass', assmsLevel: 1, extraParameters: { state: 's2' } },
        { expression: 'p', rule: '[]E [1, 2]', assmsLevel: 0, extraParameters: { state: 's1' } },
      ],
      logic: 'modal',
      goal: 'p',
    });

    // The layout `ModalProofParser.parseLine` reads: the state, ": ", then the line of any other logic.
    expect(text.split('\n')).toEqual([
      's0: [] p           Ass',
      's0 <= s1           Ass',
      's2:    q           Ass',
      's1: p           []E [1, 2]',
    ]);
  });
});

describe('isRelationFormula', () => {
  test('a relation between states is one', () => {
    expect(isRelationFormula('s0 <= s1')).toBe(true);
    expect(isRelationFormula('s0 = s1')).toBe(true);
  });

  test('a formula that holds in a state is not', () => {
    expect(isRelationFormula('[]p -> <>q')).toBe(false);
    expect(isRelationFormula('-(p & q) | r')).toBe(false);
  });

  test('a relation in parentheses is one', () => {
    expect(isRelationFormula('(s0 <= s1)')).toBe(true);
    expect(isRelationFormula(' ((s0 = s1)) ')).toBe(true);
  });

  test('a formula with a relation under a connective is not', () => {
    expect(isRelationFormula('p & s0 <= s1')).toBe(false);
    expect(isRelationFormula('(s0 = s1) -> p')).toBe(false);
    expect(isRelationFormula('-(s0 <= s1)')).toBe(false);
    expect(isRelationFormula('(s0 <= s1) & (s1 <= s2)')).toBe(false);
  });
});

describe('loadsBackWithSameGoal', () => {
  const step = (expression: string, assmsLevel = 0) => ({ expression, rule: 'Ass', assmsLevel, extraParameters: {} });

  test('a done proof whose last line is a top-level step equal to the goal loads back with the same goal', () => {
    expect(loadsBackWithSameGoal({ steps: [step('P', 1), step('P -> P')], logic: 'classical', goal: 'P->P', done: true })).toBe(true);
  });

  test('a done proof whose goal is an earlier step, not the last line, does not', () => {
    const proof = { steps: [step('P'), step('Q')], logic: 'classical', goal: 'P', done: true };
    expect(loadsBackWithSameGoal(proof)).toBe(false);
    expect(loadedGoal(proof)).toBe('Q');
  });

  test('a last line equal to the goal inside a subproof does not', () => {
    expect(loadsBackWithSameGoal({ steps: [step('P', 1)], logic: 'classical', goal: 'P', done: true })).toBe(false);
  });

  test('an unfinished or empty proof does not', () => {
    expect(loadsBackWithSameGoal({ steps: [step('P')], logic: 'classical', goal: 'P', done: false })).toBe(false);
    expect(loadsBackWithSameGoal({ steps: [], logic: 'classical', goal: 'P', done: true })).toBe(false);
    expect(loadedGoal({ steps: [], logic: 'classical', goal: 'P' })).toBeNull();
  });
});
