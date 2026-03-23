import React, { useState } from 'react';
import './App.css';
import Proof from './components/proof/ProofViewer';
import Header from './components/Header';
import Menu from './components/menu/Menu';
import NewProofModal from './components/modal/NewProofModal';
import { StepDto, ProofDto } from './types';
import { LOGIC } from './constant';

function App() {
  const [colorMapping, setColorMapping] = useState(new Map<number, string>());
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [proof, setProof] = useState<ProofDto>({
    steps: [],
    logic: LOGIC,
    goal: '',
  });

  const onColorChange = (color: string, line: number) => {
    setColorMapping(colorMapping => {
        const newColoringMap = new Map<number,string>(colorMapping);
        newColoringMap.forEach((value, key) => {
          if (value === color) {
            newColoringMap.delete(key);
          }
        });
        if (line >= 0) {
          newColoringMap.set(line, color);
        }
        return newColoringMap
    });
  };

  const handleOpenModal = () => setIsModalOpen(true);
  const handleCloseModal = () => setIsModalOpen(false);

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
    setColorMapping(new Map<number, string>())
  };

  const hasProof = proof.goal !== '' || proof.steps.length > 0;

  return (
    <div className="App">
      <Header />
      <div className="app-toolbar">
        <button
          className="new-proof-btn"
          onClick={handleOpenModal}
          aria-label="Start a new proof"
        >
          New Proof
        </button>
      </div>
      <main className="app-main">
        <Menu logic={LOGIC} proof={proof} setProof={setProof} onColorChange={onColorChange} />
        {hasProof ? (
          <Proof proof={proof} coloring={colorMapping} />
        ) : (
          <div className="empty-proof-state" role="status">
            <p>No proof loaded. Click <strong>New Proof</strong> to begin.</p>
          </div>
        )}
      </main>

      <NewProofModal
        isOpen={isModalOpen}
        onClose={handleCloseModal}
        onSubmit={handleNewProofSubmit}
      />
    </div>
  );
}

export default App;
