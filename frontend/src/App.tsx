import React, { useState } from 'react';
import './App.css';
import Proof from './components/proof/ProofViewer';
import Header from './components/Header';
import Menu from './components/menu/Menu';
import { StepDto } from './types';
import { LOGIC } from './constant';
import { ProofDto } from './types';



function App() {
  const [coloringMap, setColor] = useState(new Map<number, string>());
  function onColorChange(color: string, line: number) {
    // Create a new Map based on the existing one to maintain immutability
    const newColoringMap = new Map(coloringMap);

    // Remove the color from any other lines that have this color
    newColoringMap.forEach((value, key) => {
      if (value === color) {
        newColoringMap.delete(key);
      }
    });

    // Set the new color for the specified line
    if (line >= 0) {
      newColoringMap.set(line, color);
    }

    // Update the state with the new Map
    setColor(newColoringMap);
  }
  const steps: Array<StepDto> = [
    {
      expression: "P",
      rule: "Ass",
      assmsLevel: 0,
      extraParameters: new Map()
    },
    {
      expression: "Q",
      rule: "Ass",
      assmsLevel: 1,
      extraParameters: new Map()
    },
    {
      expression: "P",
      rule: "Rep [1]",
      assmsLevel: 1,
      extraParameters: new Map()
    },
  ]
  const [proof, setProof] = useState<ProofDto>({
    steps: steps,
    logic: LOGIC,
    goal: "Q -> P"
  });

  return (
    <div className="App">
      <Header />
      <Menu logic={LOGIC} proof={proof} setProof={setProof} onColorChange={onColorChange} />
      <Proof proof={proof} coloring={coloringMap} />
    </div>
  );
}

export default App;
