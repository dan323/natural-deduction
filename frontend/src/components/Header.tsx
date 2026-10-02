import { FC, ReactNode } from 'react';
import ThemeToggle from './ThemeToggle';

/** The sticky app bar: the title and, next to it, the toolbar given as children. */
const Header: FC<{ children?: ReactNode }> = ({ children }) => {
  return (
    <header className="app-bar">
      <h1 className="app-title">Natural Deduction Proof Assistant</h1>
      <div className="app-toolbar">
        {children}
        <ThemeToggle />
      </div>
    </header>
  );
};

export default Header;
