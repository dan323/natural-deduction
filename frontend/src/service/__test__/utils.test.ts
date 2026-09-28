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

  test('renders the first-order quantifiers as the backend prints them, forall x. A, as ∀x. A', () => {
    expect(renderExpression('forall x. P(x)')).toBe('∀x. P(x)');
    expect(renderExpression('exists y. m(y, e) = y')).toBe('∃y. m(y, e) = y');
    expect(renderExpression('(forall x. P(x)) -> -(exists x2. -P(x2))')).toBe('(∀x. P(x)) → ¬(∃x2. ¬P(x2))');
    expect(renderExpression('forall x. forall y. forall z. m(m(x, y), z) = m(x, m(y, z))'))
      .toBe('∀x. ∀y. ∀z. m(m(x, y), z) = m(x, m(y, z))');
  });

  test('leaves names that only contain forall or exists alone', () => {
    expect(renderExpression('forall & exists')).toBe('forall ∧ exists');
    expect(renderExpression('P(forallx) & xexists')).toBe('P(forallx) ∧ xexists');
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
    '[]p -> p',
    '<>(p & q)',
    '[]<>p',
    '-[]-p',
    's0 <= s1',
    's0 = s1',
  ])('accepts %j', (formula) => {
    expect(checkFormula(formula)).toBeNull();
  });

  test.each(['', '   '])('rejects a blank formula %j', (formula) => {
    expect(checkFormula(formula)).toMatch(/blank/);
  });

  test.each(['p ->', 'q &&', 'p |', 'p &', 'p -> q ->', '-', 'p = '])('rejects the dangling operator in %j', (formula) => {
    expect(checkFormula(formula)).not.toBeNull();
  });

  test.each(['[]', '<>', 'p -> []', '[] <>', '([])', '<>(p & q) & []'])('rejects the dangling modal operator in %j', (formula) => {
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

describe('checkFormula in modal-next-until', () => {
  const NU = 'modal-next-until';

  test.each([
    'X p', 'p U q', 'X (p U q)', '(X p) U q', '[] (p -> (X p))', 'X - p', '[] X p', '- p U X q -> r', 'q | (p & (X (p U q)))',
    'Xp', 'pUq', 'TRUE', 'U1', 'X Xp', 'X(p)', 'p U(q)',
    's0 <= s0+1', 's0+1 <= s1', 's0 + 1 + 1 = s0+2', 'X p & s1+2 <= s3',
  ])('accepts %j', (formula) => {
    expect(checkFormula(formula, NU)).toBeNull();
  });

  test.each(['X', 'p U', 'U q', 'p X q', 'X U p', 'p U U q', '(X)'])('rejects the misplaced X or U in %j', (formula) => {
    expect(checkFormula(formula, NU)).not.toBeNull();
  });

  test.each(['s0+', 's0 + ', 's0+1a <= s1', 's0-1 <= s1', 'X+1', '+1'])('rejects the malformed successor state in %j', (formula) => {
    expect(checkFormula(formula, NU)).not.toBeNull();
  });

  test.each(['X p', 'p U q', 's0 <= s0+1'])('the other logics read X and U as names and have no successor states: %j is refused', (formula) => {
    expect(checkFormula(formula)).not.toBeNull();
    expect(checkFormula(formula, 'modal')).not.toBeNull();
  });

  test.each(['Xp', 'pUq', 'X', 'U'])('the other logics accept %j as a name', (formula) => {
    expect(checkFormula(formula, 'modal')).toBeNull();
  });
});

describe('checkFormula in first-order', () => {
  const FO = 'first-order';

  // The group axioms of the backend's `FirstOrderTheories`, as it prints them.
  test.each([
    'forall x. forall y. forall z. m(m(x, y), z) = m(x, m(y, z))',
    'forall x. m(e, x) = x & m(x, e) = x',
    'forall x. m(i(x), x) = e & m(x, i(x)) = e',
  ])('accepts the group axiom %j', (formula) => {
    expect(checkFormula(formula, FO)).toBeNull();
  });

  test.each([
    'forall x. P(x)', 'exists x. P(x)', 'forall x. x = x', 'a = b', 'P(a)', 'p -> q', 'p', 'TRUE', '-FALSE',
    '(forall x. P(x)) & (forall x. Q(x))', 'forall x. forall y. x = y -> y = x', 'forall x. forall y. R(x, y)',
    'forall y. (forall x. m(y, x) = x) -> y = e', 'forall x. i(i(x)) = x', '-(exists x. -P(x))', 'exists x.P(x)',
    '(x) = (f(y))', '(x = y)', 'f(x)', 'P(f(g(x, y)), z)', 'x1_a = x_2', '  forall x.   P(x)  ',
  ])('accepts %j', (formula) => {
    expect(checkFormula(formula, FO)).toBeNull();
  });

  test.each([
    ['forall x P(x)', /must be followed by a "\."/],
    ['forall . P(x)', /must be followed by a variable/],
    ['exists (x). P(x)', /must be followed by a variable/],
    ['exists P(x)', /must start with a lowercase letter/],
    ['forall', /must be followed by a variable/],
    ['forall X. P(X)', /must start with a lowercase letter/],
    ['forall f(x). P(x)', /must be followed by a "\."/],
    ['forall x.', /ends with an operator/],
    ['forall x. forall y.', /ends with an operator/],
    ['exists x. (P(x)', /Unbalanced/],
    ['∀x. P(x)', /Type ∀ as "forall"/],
    ['∃x. P(x)', /Type ∃ as "exists"/],
  ])('rejects the malformed quantifier in %j', (formula, message) => {
    expect(checkFormula(formula, FO)).toMatch(message);
  });

  test.each([
    ['x = ', /ends with an operator/],
    ['= x', /missing its left operand/],
    ['P = Q', /must be terms/],
    ['x = P', /is not a term/],
    ['P(x) = y', /must be terms/],
    ['P(', /ends with an operator/],
    ['P(x', /Unbalanced/],
    ['P(x y)', /Expected "," or "\)"/],
    ['P()', /parenthesis is closed right after/],
    ['P(x,)', /parenthesis is closed right after/],
    ['P(x) Q(x)', /Missing operator before "Q"/],
    ['P(x))', /Unbalanced/],
    ['p & & q', /Missing an operand between "&" and "&"/],
    ['x, y', /separates the arguments/],
    ['P(x) . Q', /only follows the variable/],
    ['m(x, y) * z = e', /Unexpected symbol "\*"/],
    ['x * y = e', /Unexpected symbol "\*"/],
    ['s0 <= s1', /Unexpected symbol "<"/],
    ['[]p', /Unexpected symbol "\["/],
  ])('rejects the malformed formula %j', (formula, message) => {
    expect(checkFormula(formula, FO)).toMatch(message);
  });

  test('a formula nested too deeply gets a message instead of overflowing the stack', () => {
    expect(checkFormula('-'.repeat(5000) + 'p', FO)).toMatch(/nested more than 500 levels deep/);
    expect(checkFormula('('.repeat(5000) + 'p' + ')'.repeat(5000), FO)).toMatch(/nested more than 500 levels deep/);
    expect(checkFormula('P(' + 'f('.repeat(5000) + 'x' + ')'.repeat(5001), FO)).toMatch(/nested more than 500 levels deep/);
    expect(checkFormula('-'.repeat(400) + 'p', FO)).toBeNull();
  });

  test.each(['forall x. P(x)', 'exists x. P(x)', 'P(x)', 'forall x. x = x'])('the other logics refuse %j', (formula) => {
    expect(checkFormula(formula)).not.toBeNull();
    expect(checkFormula(formula, 'classical')).not.toBeNull();
    expect(checkFormula(formula, 'modal')).not.toBeNull();
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

  test('a first-order proof is written in the plain layout, as the FirstOrderProofParser of the backend reads it', () => {
    const text = proofToText({
      steps: [
        { expression: 'exists x. forall y. R(x, y)', rule: 'Ass', assmsLevel: 0, extraParameters: {} },
        { expression: 'forall y. R(a, y)', rule: 'Ass', assmsLevel: 1, extraParameters: {} },
        { expression: 'R(a, b)', rule: '∀E [2]', assmsLevel: 1, extraParameters: {} },
        { expression: 'exists x. R(x, b)', rule: '∃I [3]', assmsLevel: 1, extraParameters: {} },
        { expression: 'exists x. R(x, b)', rule: '∃E [1, 2-4]', assmsLevel: 0, extraParameters: {} },
        { expression: 'forall y. exists x. R(x, y)', rule: '∀I [5]', assmsLevel: 0, extraParameters: {} },
        { expression: 'b = b', rule: '=I', assmsLevel: 0, extraParameters: {} },
      ],
      logic: 'first-order',
      goal: 'forall y. exists x. R(x, y)',
    });

    // The reference solution of the backend's `exists-forall-swap` exercise, and an `=I` line, which has no lines.
    expect(text.split('\n')).toEqual([
      'exists x. forall y. R(x, y)           Ass',
      '   forall y. R(a, y)           Ass',
      '   R(a, b)           ∀E [2]',
      '   exists x. R(x, b)           ∃I [3]',
      'exists x. R(x, b)           ∃E [1, 2-4]',
      'forall y. exists x. R(x, y)           ∀I [5]',
      'b = b           =I',
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

  test('a modal-next-until step in a successor state starts with that state, as ModalNextUntilProofParser reads it', () => {
    const text = proofToText({
      steps: [
        { expression: '[] p', rule: 'Ass', assmsLevel: 0, extraParameters: { state: 's0' } },
        { expression: 's0 <= s0+1', rule: 'Succ [1]', assmsLevel: 0, extraParameters: {} },
        { expression: 's0+1 <= s1', rule: 'Ass', assmsLevel: 1, extraParameters: {} },
        { expression: 's0 <= s1', rule: 'Trans [2, 3]', assmsLevel: 1, extraParameters: {} },
        { expression: 'p', rule: '[]E [1, 4]', assmsLevel: 1, extraParameters: { state: 's1' } },
        { expression: '[] p', rule: '[]I [3-5]', assmsLevel: 0, extraParameters: { state: 's0+1' } },
        { expression: 'X ([] p)', rule: 'XI [6]', assmsLevel: 0, extraParameters: { state: 's0' } },
      ],
      logic: 'modal-next-until',
      goal: 'X ([] p)',
    });

    // The reference solution of the backend's `always-always-next` exercise.
    expect(text.split('\n')).toEqual([
      's0: [] p           Ass',
      's0 <= s0+1           Succ [1]',
      '   s0+1 <= s1           Ass',
      '   s0 <= s1           Trans [2, 3]',
      's1:    p           []E [1, 4]',
      's0+1: [] p           []I [3-5]',
      's0: X ([] p)           XI [6]',
    ]);
  });
});

describe('isRelationFormula in modal-next-until', () => {
  const NU = 'modal-next-until';

  test('a relation between successor states is one', () => {
    expect(isRelationFormula('s0+1 <= s1', NU)).toBe(true);
    expect(isRelationFormula('s0 <= s0+1', NU)).toBe(true);
    expect(isRelationFormula('(s0 + 1 = s1+2)', NU)).toBe(true);
  });

  test('X and U as words of their own are connectives, but not inside a name', () => {
    expect(isRelationFormula('X s0 <= s1', NU)).toBe(false);
    expect(isRelationFormula('p U s0 <= s1', NU)).toBe(false);
    expect(isRelationFormula('X(p)', NU)).toBe(false);
    expect(isRelationFormula('Xs <= sU', NU)).toBe(true);
  });

  test('in modal logic a state may be called X', () => {
    expect(isRelationFormula('X <= s1', 'modal')).toBe(true);
  });
});

describe('isRelationFormula in first-order', () => {
  test('an equation is a formula, since first-order logic has no states', () => {
    expect(isRelationFormula('x = e', 'first-order')).toBe(false);
    expect(isRelationFormula('(m(x, e) = x)', 'first-order')).toBe(false);
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
