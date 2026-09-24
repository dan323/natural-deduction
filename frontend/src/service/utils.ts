import { ProofDto } from "../types";

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

type FormulaState = {
  depth: number;
  // true at the start and after an operator or "(", false after an operand or ")"
  expectOperand: boolean;
};

// Updates the state with one token of a formula and returns the problem it causes, if any.
function checkToken(token: string, state: FormulaState): string | null {
  if (token === ')') {
    if (state.depth === 0) return 'Unbalanced parentheses: ")" has no matching "(".';
    if (state.expectOperand) return 'A parenthesis is closed right after an operator or "(".';
    state.depth--;
    return null;
  }
  if (BINARY_OPERATORS.has(token)) {
    if (state.expectOperand) return `The operator "${token}" is missing its left operand.`;
    state.expectOperand = true;
    return null;
  }
  // "(", a negation-like operator or an operand: all of them need an operator (or nothing) before them.
  if (!state.expectOperand) return `Missing operator before "${token}".`;
  if (token === '(') state.depth++;
  else if (!UNARY_OPERATORS.has(token)) state.expectOperand = false;
  return null;
}

// A light syntax check of a formula typed by the user, so that obvious mistakes are reported before they become a
// proof line. Returns a message describing the first problem, or null when nothing is wrong. It is deliberately
// conservative: the backend parser stays the source of truth.
export function checkFormula(formula: string): string | null {
  const text = formula.trim();
  if (text === '') return 'This field must not be blank.';

  const state: FormulaState = { depth: 0, expectOperand: true };
  let rest = text;
  while (rest !== '') {
    const token = FORMULA_TOKEN.exec(rest)?.[0];
    if (token === undefined) return `Unexpected symbol "${rest[0]}".`;
    rest = rest.slice(token.length).trimStart();
    const problem = checkToken(token, state);
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
// read back as an indent.
export function proofToText(proof: ProofDto): string {
  return proof.steps
    .map((step) => INDENT.repeat(step.assmsLevel) + step.expression.trim() + RULE_GAP + step.rule.trim())
    .join('\n');
}
