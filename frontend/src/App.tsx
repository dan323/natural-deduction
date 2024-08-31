import React, { useState } from 'react';
import './App.css';
import Proof from './components/proof/ProofViewer';
import Header from './components/Header';
import Menu from './components/menu/Menu';
import NewProofModal from './components/NewProofModal';
import { StepDto, ProofDto } from './types';
import { LOGIC } from './constant';

function App() {
  const [coloringMap, setColor] = useState(new Map<number, string>());
  const [isModalOpen, setModalOpen] = useState(false);
  const [proof, setProof] = useState<ProofDto>({
    steps: [],
    logic: LOGIC,
    goal: '',
  });

  const onColorChange = (color: string, line: number) => {
    const newColoringMap = new Map(coloringMap);
    newColoringMap.forEach((value, key) => {
      if (value === color) {
        newColoringMap.delete(key);
      }
    });
    if (line >= 0) {
      newColoringMap.set(line, color);
    }
    setColor(newColoringMap);
  };

  const handleOpenModal = () => setModalOpen(true);
  const handleCloseModal = () => setModalOpen(false);

  const handleNewProofSubmit = (premises: string[], goal: string) => {
    const steps: StepDto[] = premises.map((premise) => ({
      expression: premise,
      rule: 'Ass',
      assmsLevel: 0,
      extraParameters: new Map(),
    }));

    setProof({
      steps: steps,
      logic: LOGIC,
      goal: goal,
    });
  };

  return (
    <div className="App">
      <Header />
      <button className="new-proof-btn" onClick={handleOpenModal}>
        New Proof
      </button>
      <Menu logic={LOGIC} proof={proof} setProof={setProof} onColorChange={onColorChange} />
      <Proof proof={proof} coloring={coloringMap} />
      
      <NewProofModal
        isOpen={isModalOpen}
        onClose={handleCloseModal}
        onSubmit={handleNewProofSubmit}
      />
    </div>
  );
}

export default App;
