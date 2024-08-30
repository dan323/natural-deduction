
export function renderLogic(rule: string): string {
    return rule
      .replace(/->/g, "→") // Logical implication
      .replace(/(?<!\d)-(?!\d)/g, "¬")  // Logical negation
      .replace(/&/g, "∧")  // Logical and
      .replace(/\|/g, "∨") // Logical or
      .replace(/\[\]/g, "□") // Modal logical always
      .replace(/<>/g, "◇")
      .replace(/I/g, "Intro")
      .replace(/E/g, "Elim");
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