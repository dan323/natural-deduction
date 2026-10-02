import { FC, useEffect, useState } from 'react';
import { THEMES, Theme, applyTheme, readTheme, writeTheme } from '../service/theme';

const LABELS: Record<Theme, string> = { system: 'System', light: 'Light', dark: 'Dark' };

/** The light/dark/system choice of the app bar. */
const ThemeToggle: FC = () => {
  const [theme, setTheme] = useState<Theme>(readTheme);

  useEffect(() => {
    applyTheme(theme);
  }, [theme]);

  return (
    <label className="theme-toggle">
      <span className="visually-hidden">Colour theme</span>
      <select
        className="theme-select"
        value={theme}
        onChange={(event) => {
          const value = event.target.value as Theme;
          writeTheme(value);
          setTheme(value);
        }}
      >
        {THEMES.map((value) => (
          <option key={value} value={value}>
            {LABELS[value]}
          </option>
        ))}
      </select>
    </label>
  );
};

export default ThemeToggle;
