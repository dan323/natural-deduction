import React, { FC } from 'react';
import './Header.css';

const Header: FC<{}> = () => {
  return (
    <header className="app-header">
      <h1 className="app-title">Natural Deduction Proof Assistant</h1>
    </header>
  );
};

export default Header;
