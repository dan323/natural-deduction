import { readdirSync, readFileSync } from 'fs';
import { join } from 'path';

const SRC = join(__dirname, '..');

function stylesheets(dir: string): string[] {
  return readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
    const path = join(dir, entry.name);
    if (entry.isDirectory()) return stylesheets(path);
    return entry.name.endsWith('.css') ? [path] : [];
  });
}

const read = (relative: string) => readFileSync(join(SRC, relative), 'utf-8');

// jsdom does not apply the stylesheets, so these guard the accessibility rules that live in them.
describe('stylesheets', () => {
  test.each(stylesheets(SRC).map((path) => [path.slice(SRC.length + 1), path]))(
    '%s never removes the focus outline',
    (_name, path) => {
      expect(readFileSync(path, 'utf-8')).not.toMatch(/outline\s*:\s*(none|0)\b/);
    });

  test('one shared :focus-visible ring is defined', () => {
    expect(read('index.css')).toMatch(/:focus-visible\s*\{[^}]*outline:\s*3px solid #0b5ed7;[^}]*outline-offset:\s*2px/);
  });

  test('the confetti only animates when the user has not asked for reduced motion', () => {
    const css = read('components/goal/goal.css');
    const outsideTheMediaQuery = css.replace(/@media[^{]*\{[^@]*\}\s*\}/, '');

    expect(css).toMatch(/@media \(prefers-reduced-motion: no-preference\)\s*\{[^@]*animation:\s*explode/);
    expect(outsideTheMediaQuery).not.toMatch(/animation:\s*explode/);
  });
});
