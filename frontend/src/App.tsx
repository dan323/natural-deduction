import { useCallback, useState } from 'react';
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
  // The control that had the focus when the dialog was opened, read before the page goes inert (a browser moves the
  // focus away from something that becomes inert), so that the dialog can hand the focus back to it.
  const [modalOpener, setModalOpener] = useState<HTMLElement | null>(null);
  // Bumped for every new proof; it is the key of the Menu, so that the selected rule, the typed inputs and the last
  // error of the previous proof do not carry over.
  const [proofId, setProofId] = useState(0);
  const [proof, setProof] = useState<ProofDto>({
    steps: [],
    logic: LOGIC,
    goal: '',
  });

  const onColorChange = useCallback((color: string, line: number) => {
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
  }, []);

  const handleOpenModal = () => {
    setModalOpener(document.activeElement instanceof HTMLElement ? document.activeElement : null);
    setIsModalOpen(true);
  };
  const handleCloseModal = () => setIsModalOpen(false);

  const handleNewProofSubmit = (premises: string[], goal: string) => {
    const steps: StepDto[] = premises.map((premise) => ({
      expression: premise,
      rule: 'Ass',
      assmsLevel: 0,
      extraParameters: {},
    }));

    setProof({
      steps: steps,
      logic: LOGIC,
      goal: goal,
    });
    setColorMapping(new Map<number, string>())
    setProofId(id => id + 1);
  };

  const hasProof = proof.goal !== '' || proof.steps.length > 0;

  return (
    <div className="App">
      {/* While the modal is open the page behind it can neither be tabbed to nor read by assistive technology. */}
      <div inert={isModalOpen}>
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
          <Menu key={proofId} logic={LOGIC} proof={proof} setProof={setProof} onColorChange={onColorChange} />
          {hasProof ? (
            <Proof proof={proof} coloring={colorMapping} />
          ) : (
            <div className="empty-proof-state" role="status">
              <p>No proof loaded. Click <strong>New Proof</strong> to begin.</p>
            </div>
          )}
        </main>
      </div>

      <NewProofModal
        isOpen={isModalOpen}
        opener={modalOpener}
        onClose={handleCloseModal}
        onSubmit={handleNewProofSubmit}
      />
    </div>
  );
}

export default App;
