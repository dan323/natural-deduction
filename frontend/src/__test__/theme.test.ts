import { THEME_KEY, applyTheme, readTheme, writeTheme } from '../service/theme';

describe('theme', () => {
  beforeEach(() => {
    window.localStorage.clear();
    document.documentElement.removeAttribute('data-theme');
  });
  afterEach(() => jest.restoreAllMocks());

  it('is system when nothing is saved', () => {
    expect(readTheme()).toBe('system');
  });

  it.each(['system', 'light', 'dark'] as const)('reads back a saved %s', (theme) => {
    writeTheme(theme);
    expect(window.localStorage.getItem(THEME_KEY)).toBe(theme);
    expect(readTheme()).toBe(theme);
  });

  it('treats an unknown saved value as system', () => {
    window.localStorage.setItem(THEME_KEY, 'purple');
    expect(readTheme()).toBe('system');
  });

  it('ignores storage that cannot be read', () => {
    jest.spyOn(Storage.prototype, 'getItem').mockImplementation(() => {
      throw new Error('denied');
    });
    expect(readTheme()).toBe('system');
  });

  it('ignores storage that cannot be written', () => {
    jest.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
      throw new Error('full');
    });
    expect(() => writeTheme('dark')).not.toThrow();
  });

  it('sets data-theme on the root element', () => {
    applyTheme('dark');
    expect(document.documentElement.getAttribute('data-theme')).toBe('dark');
    applyTheme('system');
    expect(document.documentElement.getAttribute('data-theme')).toBe('system');
  });
});
