import { ProofDto, StepDto } from "../types";
import { NEXT_UNTIL_LOGIC } from "../components/input/connectives";

// Renders logical operators in a formula (only symbols, never letters, so a variable named E or I is untouched).
export function renderExpression(expression: string): string {
  return expression
    .replace(/->/g, "→")          // Logical implication
    .replace(/(?<!\d)-(?!\d)/g, "¬")  // Logical negation
    .replace(/&/g, "∧")           // Logical AND
    .replace(/\|/g, "∨")          // Logical OR
    .replace(/\[\]/g, "□")        // Modal always
    .replace(/<>/g, "◇");         // Modal possibly
}

// Renders the name of an inference rule, e.g. "->I [1-2]" becomes "→I [1-2]" and "FE [3]" becomes "⊥E [3]". The
// `symbol` of an action descriptor is written the same way, so a rule has one name in the list of rules and in a proof.
export function renderRule(rule: string): string {
  return renderExpression(rule)
    .replace(/^F(?=[EI])/, "⊥");  // Falsum introduction and elimination
}

// Function to check if a color string is a valid CSS color
export function isValidCSSColor(color: string): boolean {
  const option = new Option();
  option.style.color = color;
  return option.style.color !== '';
}

// Symbols of the formula language: `&`, `|`, `->`, `-` (negation) and, for the modal logic, `[]`, `<>`, `<=`, `=`.
const FORMULA_TOKEN = /^(?:->|<>|<=|\[\]|[&|=\-()]|\w+)/;
const BINARY_OPERATORS = new Set(['&', '|', '->', '<=', '=']);
const UNARY_OPERATORS = new Set(['-', '[]', '<>']);

// `modal-next-until` (`ModalNextUntilLogicParser`) adds Next, `X A`, and Until, `A U B`, as words of their own: `\w+`
// takes a whole word, so `X` and `U` are operators exactly when no letter, digit or `_` touches them, and `Xp`, `pUq`,
// `TRUE` stay names. A state there can be a successor term, `s0+1` or `s0 + 1 + 1`, which is one operand.
const NEXT = 'X';
const UNTIL = 'U';
const SUCCESSOR_STEPS = /^(?:\s*\+\s*\d+)+(?!\w)/;

type FormulaState = {
  depth: number;
  // true at the start and after an operator or "(", false after an operand or ")"
  expectOperand: boolean;
};

// Updates the state with one token of a formula and returns the problem it causes, if any.
function checkToken(token: string, state: FormulaState, nextUntil: boolean): string | null {
  if (token === ')') {
    if (state.depth === 0) return 'Unbalanced parentheses: ")" has no matching "(".';
    if (state.expectOperand) return 'A parenthesis is closed right after an operator or "(".';
    state.depth--;
    return null;
  }
  if (BINARY_OPERATORS.has(token) || (nextUntil && token === UNTIL)) {
    if (state.expectOperand) return `The operator "${token}" is missing its left operand.`;
    state.expectOperand = true;
    return null;
  }
  // "(", a negation-like operator or an operand: all of them need an operator (or nothing) before them.
  if (!state.expectOperand) return `Missing operator before "${token}".`;
  if (token === '(') state.depth++;
  else if (!UNARY_OPERATORS.has(token) && !(nextUntil && token === NEXT)) state.expectOperand = false;
  return null;
}

// A light syntax check of a formula typed by the user, so that obvious mistakes are reported before they become a
// proof line. Returns a message describing the first problem, or null when nothing is wrong. It is deliberately
// conservative: the backend parser stays the source of truth. `logic` is the logic of the proof the formula is for: only
// `modal-next-until` reads `X`, `U` and successor states such as `s0+1`.
export function checkFormula(formula: string, logic?: string): string | null {
  const text = formula.trim();
  if (text === '') return 'This field must not be blank.';

  const nextUntil = logic === NEXT_UNTIL_LOGIC;
  const state: FormulaState = { depth: 0, expectOperand: true };
  let rest = text;
  while (rest !== '') {
    let token = FORMULA_TOKEN.exec(rest)?.[0];
    if (token === undefined) return `Unexpected symbol "${rest[0]}".`;
    if (nextUntil && /^\w/.test(token) && token !== NEXT && token !== UNTIL) {
      token += SUCCESSOR_STEPS.exec(rest.slice(token.length))?.[0] ?? '';
    }
    rest = rest.slice(token.length).trimStart();
    const problem = checkToken(token, state, nextUntil);
    if (problem !== null) return problem;
  }

  if (state.expectOperand) return 'The formula ends with an operator that has nothing after it.';
  if (state.depth > 0) return 'Unbalanced parentheses: "(" is never closed.';
  return null;
}

// The text layout of a proof step, as the backend's `ProofStep.toString()` prints it and its `ProofParser.ProofLine`
// reads it back: 3 spaces of indent per assumption level, the expression, an 11-space gap and the rule.
const INDENT = ' '.repeat(3);
const RULE_GAP = ' '.repeat(11);

// Renders a proof as the text `POST /logic/{logic}/proof` accepts, one step per line. The expressions and rules are
// written as the backend sent them (ASCII `->`, `&`, ...), not as the proof table renders them, so that the text parses.
// They are trimmed: until the backend first answers, the premises are as the user typed them, and a leading space would
// read back as an indent. A step in a state (modal logic, see `hasStates`) starts with that state, before the indent, as
// `ProofStepModal.toString()` prints it and `ModalProofParser.parseLine` reads it: `s1:    p           []E [1, 2]`. A
// relation between states (`s0 <= s1`) is in no state and has no prefix.
export function proofToText(proof: ProofDto): string {
  return proof.steps
    .map((step) => statePrefix(step) + INDENT.repeat(step.assmsLevel) + step.expression.trim() + RULE_GAP + step.rule.trim())
    .join('\n');
}

function statePrefix(step: StepDto): string {
  const state = step.extraParameters?.state?.trim();
  return state ? `${state}: ` : '';
}

// Whether a formula of a logic with states is a relation between states (`s0 <= s1`, `s0 = s1`) rather than a formula
// that holds in a state. Only the top-level operator counts: the backend's `ModalLogicParser` also accepts a relation
// inside a connective (`p & s0 <= s1`), and such a formula holds in a state. The relations bind tighter than every
// connective there, so the formula is a relation exactly when, outside parentheses, it has a relation and no connective.
// In `modal-next-until` (`logic`) the sides may be successor terms (`s0+1 <= s1`), and `X` and `U` as words of their own
// are connectives too.
export function isRelationFormula(formula: string, logic?: string): boolean {
  const text = withoutEnclosingParentheses(formula.trim());
  const nextUntil = logic === NEXT_UNTIL_LOGIC;
  let depth = 0;
  let relation = false;
  for (let i = 0; i < text.length; i++) {
    const char = text[i];
    if (char === '(') depth++;
    else if (char === ')') depth--;
    else if (depth > 0) continue;
    else if (text.startsWith('<=', i)) {
      relation = true;
      i++;
    } else if (char === '=') relation = true;
    else if ('&|-[]<>'.includes(char)) return false;
    else if (nextUntil && (char === NEXT || char === UNTIL) && !/\w/.test(text[i - 1] ?? '') && !/\w/.test(text[i + 1] ?? '')) {
      return false;
    }
  }
  return relation;
}

// The formula without the parentheses that enclose all of it, as often as they do: `((p))` is `p`, `(p) & (q)` stays.
function withoutEnclosingParentheses(formula: string): string {
  let text = formula;
  while (text.startsWith('(') && closingParenthesis(text) === text.length - 1) {
    text = text.slice(1, -1).trim();
  }
  return text;
}

// The index of the parenthesis that closes the one the text starts with, or -1 if it is never closed.
function closingParenthesis(text: string): number {
  let depth = 0;
  for (let i = 0; i < text.length; i++) {
    if (text[i] === '(') depth++;
    else if (text[i] === ')' && --depth === 0) return i;
  }
  return -1;
}

// The goal a proof text reads back with: `POST .../proof` takes the last line as the goal. Null for an empty proof.
export function loadedGoal(proof: ProofDto): string | null {
  const last = proof.steps[proof.steps.length - 1] as StepDto | undefined;
  return last === undefined ? null : last.expression.trim();
}

// Whether the text of this proof (see `proofToText`) loads back as the same proof, with the same goal. `done` only says
// that some top-level step is the goal (the domain's `Proof.isDone()`), while the backend reads the last line as the
// goal, so the last line must also be a top-level step equal to the goal. Spaces are ignored in the comparison, since
// the goal may still be as the user typed it; any other difference in writing counts as a different goal.
export function loadsBackWithSameGoal(proof: ProofDto): boolean {
  const last = proof.steps[proof.steps.length - 1] as StepDto | undefined;
  const withoutSpaces = (formula: string) => formula.replace(/\s+/g, '');
  return proof.done === true
    && last !== undefined
    && last.assmsLevel === 0
    && withoutSpaces(last.expression) === withoutSpaces(proof.goal);
}
