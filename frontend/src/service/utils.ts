export function renderLogic(rule: string): string {
  return rule
    .replace(/->/g, "→")          // Logical implication
    .replace(/(?<!\d)-(?!\d)/g, "¬")  // Logical negation
    .replace(/&/g, "∧")           // Logical AND
    .replace(/\|/g, "∨")          // Logical OR
    .replace(/\[\]/g, "□")        // Modal always
    .replace(/<>/g, "◇")          // Modal possibly
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