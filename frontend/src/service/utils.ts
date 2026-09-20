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

// Renders the name of an inference rule, e.g. "->I [1-2]" becomes "→Intro [1-2]".
export function renderRule(rule: string): string {
  return renderExpression(rule)
    .replace(/I/g, "Intro")       // Introduction
    .replace(/(?<!FALS)E/g, "Elim"); // Elimination, unless preceded by 'FALS'
}

// Helper function to generate indentation
export function getIndentation(assmsLevel: number): string {
  return Array(assmsLevel).fill('\t').join('');
}

// Function to check if a color string is a valid CSS color
export function isValidCSSColor(color: string): boolean {
  const option = new Option();
  option.style.color = color;
  return option.style.color !== '';
}

// Symbols of the formula language: `&`, `|`, `->`, `-` (negation) and, for the modal logic, `[]`, `<>`, `<=`, `=`.
const FORMULA_TOKEN = /^(?:->|<>|<=|\[\]|[&|=\-()]|[A-Za-z0-9_]+)/;
const BINARY_OPERATORS = new Set(['&', '|', '->', '<=', '=']);
const UNARY_OPERATORS = new Set(['-', '[]', '<>']);

// A light syntax check of a formula typed by the user, so that obvious mistakes are reported before they become a
// proof line. Returns a message describing the first problem, or null when nothing is wrong. It is deliberately
// conservative: the backend parser stays the source of truth.
export function checkFormula(formula: string): string | null {
  const text = formula.trim();
  if (text === '') return 'This field must not be blank.';

  let depth = 0;
  let expectOperand = true; // true at the start and after an operator or "(", false after an operand or ")"
  let rest = text;
  while (rest !== '') {
    const match = FORMULA_TOKEN.exec(rest);
    if (match === null) {
      return `Unexpected symbol "${rest[0]}".`;
    }
    const token = match[0];
    rest = rest.slice(token.length).trimStart();

    if (token === '(') {
      if (!expectOperand) return 'Missing operator before "(".';
      depth++;
    } else if (token === ')') {
      if (depth === 0) return 'Unbalanced parentheses: ")" has no matching "(".';
      if (expectOperand) return 'A parenthesis is closed right after an operator or "(".';
      depth--;
    } else if (BINARY_OPERATORS.has(token)) {
      if (expectOperand) return `The operator "${token}" is missing its left operand.`;
      expectOperand = true;
    } else if (UNARY_OPERATORS.has(token)) {
      if (!expectOperand) return `Missing operator before "${token}".`;
    } else {
      if (!expectOperand) return `Missing operator before "${token}".`;
      expectOperand = false;
    }
  }

  if (expectOperand) return 'The formula ends with an operator that has nothing after it.';
  if (depth > 0) return 'Unbalanced parentheses: "(" is never closed.';
  return null;
}
