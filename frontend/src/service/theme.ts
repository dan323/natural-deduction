// The colour theme: "system" follows the OS (`prefers-color-scheme`), the others override it. The choice is kept in
// `localStorage` under this key and shown as `data-theme` on <html>, which index.css reads.
export const THEME_KEY = 'natural-deduction.theme';

export type Theme = 'system' | 'light' | 'dark';

export const THEMES: readonly Theme[] = ['system', 'light', 'dark'];

const isTheme = (value: unknown): value is Theme => THEMES.includes(value as Theme);

// The saved theme. Storage that cannot be used, and anything unknown, count as "system".
export function readTheme(): Theme {
  try {
    const value = window.localStorage.getItem(THEME_KEY);
    return isTheme(value) ? value : 'system';
  } catch {
    return 'system';
  }
}

// Saves the theme; storage that cannot be used (or is full) is ignored, the theme then lasts until the page reloads.
export function writeTheme(theme: Theme): void {
  try {
    window.localStorage.setItem(THEME_KEY, theme);
  } catch {
    // ignored
  }
}

// Shows the theme on the page.
export function applyTheme(theme: Theme): void {
  document.documentElement.setAttribute('data-theme', theme);
}
