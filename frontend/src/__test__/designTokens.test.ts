import { readdirSync, readFileSync, statSync } from 'fs';
import { join } from 'path';

const SRC = join(__dirname, '..');
const rootBlockPattern = /:root\s*\{[^}]*\}/;
const rootBlock = rootBlockPattern.exec(readFileSync(join(SRC, 'index.css'), 'utf8'))![0];
const css = readFileSync(join(SRC, 'index.css'), 'utf8');
const darkBlock = /:root\[data-theme='dark'\]\s*\{[^}]*\}/.exec(css)![0];
const mediaBlock = /@media \(prefers-color-scheme: dark\)\s*\{\s*:root:not\([^{]*\{[^}]*\}/.exec(css)![0];
const colourTokens = (block: string) =>
  new Map<string, string>(
    Array.from(block.matchAll(/--([\w-]+):\s*(#[0-9a-fA-F]{6})\s*;/g)).map((m) => [m[1], m[2]]),
  );
const declarations = (block: string) =>
  block
    .slice(block.indexOf('{', block.indexOf('{') + (block.startsWith('@') ? 1 : 0)) + 1)
    .split(';')
    .map((d) => d.replace(/[}\s]+/g, ' ').trim())
    .filter(Boolean);
const lightTokens = colourTokens(rootBlock);
// The dark palette overrides some of the light tokens; the rest stay as they are.
const darkTokens = new Map([...lightTokens, ...colourTokens(darkBlock)]);

const luminance = (hex: string) => {
  const [r, g, b] = [1, 3, 5].map((i) => {
    const c = parseInt(hex.slice(i, i + 2), 16) / 255;
    return c <= 0.03928 ? c / 12.92 : ((c + 0.055) / 1.055) ** 2.4;
  });
  return 0.2126 * r + 0.7152 * g + 0.0722 * b;
};

const contrast = (a: string, b: string) => {
  const [hi, lo] = [luminance(a), luminance(b)].sort((x, y) => y - x);
  return (hi + 0.05) / (lo + 0.05);
};

// [text token, background token]: every combination the stylesheets use.
const PAIRS: [string, string][] = [
  ['color-text', 'color-background'],
  ['color-text', 'color-surface'],
  ['color-text', 'color-surface-muted'],
  ['color-text', 'color-primary-soft'],
  ['color-text-heading', 'color-surface'],
  ['color-text-heading', 'color-background'],
  ['color-muted', 'color-background'],
  ['color-muted', 'color-surface'],
  ['color-muted', 'color-surface-muted'],
  ['color-on-primary', 'color-primary'],
  ['color-on-primary', 'color-primary-hover'],
  ['color-on-primary', 'color-danger'],
  ['color-on-primary', 'color-danger-hover'],
  ['color-primary', 'color-surface'],
  ['color-primary', 'color-primary-soft'],
  ['color-primary', 'color-background'],
  ['color-success', 'color-surface'],
  ['color-success', 'color-success-soft'],
  ['color-danger', 'color-surface'],
  ['color-danger', 'color-danger-soft'],
  ['color-warning', 'color-warning-soft'],
  ['color-info', 'color-info-soft'],
  ['color-on-highlight', 'color-highlight'],
];

describe.each([
  ['light', lightTokens],
  ['dark', darkTokens],
])('%s design tokens', (_name, tokens) => {
  it.each(PAIRS)('%s on %s has a contrast of at least 4.5:1', (text, background) => {
    expect(tokens.has(text)).toBe(true);
    expect(tokens.has(background)).toBe(true);
    expect(contrast(tokens.get(text)!, tokens.get(background)!)).toBeGreaterThanOrEqual(4.5);
  });

  it.each(['color-background', 'color-surface', 'color-surface-muted'])(
    'the focus ring has a contrast of at least 3:1 on %s',
    (background) => {
      expect(contrast(tokens.get('color-focus')!, tokens.get(background)!)).toBeGreaterThanOrEqual(3);
    },
  );

  it('keeps the focus ring, the highlight and the subproof rule distinguishable', () => {
    const colours = ['color-focus', 'color-highlight', 'color-text-heading', 'color-muted'].map((t) => tokens.get(t));
    expect(new Set(colours).size).toBe(colours.length);
    expect(contrast(tokens.get('color-highlight')!, tokens.get('color-surface')!)).toBeGreaterThanOrEqual(1.2);
  });
});

describe('design tokens', () => {
  it('has the same dark values for the OS preference and for data-theme="dark"', () => {
    expect(declarations(mediaBlock)).toEqual(declarations(darkBlock));
    expect(declarations(darkBlock).length).toBeGreaterThan(20);
  });

  it('has colour literals only in the :root token block', () => {
    const cssFiles: string[] = [];
    const walk = (dir: string) => {
      for (const name of readdirSync(dir)) {
        const path = join(dir, name);
        if (statSync(path).isDirectory()) walk(path);
        else if (path.endsWith('.css')) cssFiles.push(path);
      }
    };
    walk(SRC);
    const offenders = cssFiles.filter((file) => {
      const text = readFileSync(file, 'utf8')
        .replace(rootBlockPattern, '')
        .replace(darkBlock, '')
        .replace(mediaBlock, '');
      return /#[0-9a-fA-F]{3,8}\b/.test(text);
    });
    expect(offenders).toEqual([]);
  });
});
