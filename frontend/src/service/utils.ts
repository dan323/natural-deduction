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
