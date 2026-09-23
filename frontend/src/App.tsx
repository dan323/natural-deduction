import { useCallback, useEffect, useRef, useState } from 'react';
import './App.css';
import Proof from './components/proof/ProofViewer';
import Header from './components/Header';
import Menu, { MenuHandle } from './components/menu/Menu';
import NewProofModal from './components/modal/NewProofModal';
import { StepDto, ProofDto } from './types';
import { LOGIC } from './constant';
import { loadProofFromText, undoLastStep } from './service/actions';
import { proofToText } from './service/utils';

// The steps counted as the proof's premises: a leading run of `Ass` steps at assumption level 0, the shape
// `handleNewProofSubmit` creates. The backend treats exactly this prefix as the premises when it replays a proof
// (see `ClassicalProofTransformer.replayProof` / `ProofParser.extractAssumptions`), so it is what "nothing beyond
// the premises" means for both Undo (nothing left to undo) and New Proof (nothing to lose by discarding it).
function premiseCount(steps: StepDto[]): number {
  let count = 0;
  for (const step of steps) {
    if (step.assmsLevel !== 0 || step.rule !== 'Ass') break;
    count++;
  }
  return count;
}

function App() {
  const [colorMapping, setColorMapping] = useState(new Map<number, string>());
  const [isModalOpen, setIsModalOpen] = useState(false);
  // The control that had the focus when the dialog was opened, read before the page goes inert (a browser moves the
  // focus away from something that becomes inert), so that the dialog can hand the focus back to it.
  const [modalOpener, setModalOpener] = useState<HTMLElement | null>(null);
  // Bumped for every new proof; it is the key of the Menu, so that the selected rule, the typed inputs and the last
  // error of the previous proof do not carry over.
  const [proofId, setProofId] = useState(0);
  const menuRef = useRef<MenuHandle>(null);
  const newProofButtonRef = useRef<HTMLButtonElement>(null);
  // Mirrors `proofId`, readable from inside an in-flight undo request's callback without that closure capturing a
  // stale value: an undo response that comes back after a newer proof was started (New Proof can be clicked while
  // an undo is still pending) must not overwrite that newer proof.
  const proofIdRef = useRef(0);
  const [proof, setProof] = useState<ProofDto>({
    steps: [],
    logic: LOGIC,
    goal: '',
  });

  // Shown next to the toolbar's New Proof button instead of opening the dialog right away, whenever the current
  // proof has more than its premises: an in-page confirmation, not `window.confirm`, so it can be tested and styled
  // like the rest of the UI, and so the page behind it never needs to go inert.
  const [confirmingNewProof, setConfirmingNewProof] = useState(false);
  const confirmCancelRef = useRef<HTMLButtonElement>(null);
  const [isUndoing, setIsUndoing] = useState(false);
  const [undoError, setUndoError] = useState('');
  // What "Copy proof as text" did, and for which proof: copied to the clipboard, or (when the clipboard cannot be used,
  // e.g. outside a secure context) the text, shown for the user to copy by hand. It is only shown while that proof is
  // the one on screen, since it is out of date as soon as the proof changes.
  const [copyResult, setCopyResult] = useState<{ proof: ProofDto, text: string, copied: boolean } | null>(null);
  const currentCopy = copyResult?.proof === proof ? copyResult : null;
  // Bumped whenever the New Proof dialog closes, so that a "Load from text" answer arriving after the user cancelled
  // the dialog is dropped instead of replacing the proof on screen.
  const dialogSessionRef = useRef(0);

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

  // A click (or Enter, or Space) on a row of the proof picks that line for the rule being filled in the menu.
  const handleSelectLine = useCallback((line: number) => menuRef.current?.selectLine(line), []);

  // Goes straight to the dialog when there is nothing to lose; otherwise asks first. `opener` is who gets the focus
  // back, from whichever control (the toolbar button, or the Menu's once a proof is done) asked for a new proof.
  const requestNewProof = (opener: HTMLElement | null) => {
    setModalOpener(opener);
    if (proof.steps.length > premiseCount(proof.steps)) {
      setConfirmingNewProof(true);
    } else {
      setIsModalOpen(true);
    }
  };
  const handleOpenModal = () => requestNewProof(document.activeElement instanceof HTMLElement ? document.activeElement : null);
  // The button of a finished proof goes away with the proof, so the toolbar button is the one that gets the focus back.
  const handleOpenModalFromMenu = () => requestNewProof(newProofButtonRef.current);
  const handleConfirmNewProof = () => {
    setConfirmingNewProof(false);
    setIsModalOpen(true);
  };
  const handleCancelNewProof = useCallback(() => {
    setConfirmingNewProof(false);
    modalOpener?.focus();
  }, [modalOpener]);
  const handleCloseModal = () => {
    dialogSessionRef.current += 1;
    setIsModalOpen(false);
  };

  // Escape cancels the inline confirmation, like the dialog does for itself.
  useEffect(() => {
    if (!confirmingNewProof) return;
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') handleCancelNewProof();
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [confirmingNewProof, handleCancelNewProof]);

  // Cancel is the safe default action, so it gets the focus as soon as the confirmation appears.
  useEffect(() => {
    if (confirmingNewProof) confirmCancelRef.current?.focus();
  }, [confirmingNewProof]);

  // Replaces the proof on screen with a new one, and forgets everything about the previous one.
  const showNewProof = (newProof: ProofDto) => {
    setProof(newProof);
    setColorMapping(new Map<number, string>())
    setUndoError('');
    proofIdRef.current += 1;
    setProofId(proofIdRef.current);
  };

  const handleNewProofSubmit = (premises: string[], goal: string) => {
    const steps: StepDto[] = premises.map((premise) => ({
      expression: premise,
      rule: 'Ass',
      assmsLevel: 0,
      extraParameters: {},
    }));
    showNewProof({ steps: steps, logic: LOGIC, goal: goal });
  };

  const handleLoadText = (text: string, goal: string) => new Promise<string | null>((resolve) => {
    const session = dialogSessionRef.current;
    loadProofFromText(LOGIC, text, goal, (result) => {
      if (dialogSessionRef.current !== session) {
        resolve(null);
      } else if (result.success && result.proof) {
        showNewProof(result.proof);
        resolve(null);
      } else {
        resolve(result.message || 'Could not load the proof.');
      }
    });
  });

  // Copies the proof in the text layout the backend reads back (see `proofToText`), for "Load from text" or a file.
  const handleCopyText = async () => {
    const text = proofToText(proof);
    try {
      await navigator.clipboard.writeText(text);
      setCopyResult({ proof, text, copied: true });
    } catch {
      setCopyResult({ proof, text, copied: false });
    }
  };

  // Nothing to undo once only the premises are left, or there are no steps at all.
  const canUndo = proof.steps.length > premiseCount(proof.steps);

  const handleUndo = () => {
    if (!canUndo || isUndoing) return;
    setUndoError('');
    setIsUndoing(true);
    const requestedProofId = proofIdRef.current;
    undoLastStep(LOGIC, proof, (result) => {
      setIsUndoing(false);
      // New Proof stays enabled while an undo is in flight, and it bumps `proofIdRef`. If that happened, this
      // response is about a proof that no longer exists on screen; applying it (or reporting its error) would
      // clobber the newer proof, so drop it.
      if (proofIdRef.current !== requestedProofId) return;
      if (result.proof) {
        const updated = result.proof;
        setProof(updated);
        setColorMapping(new Map<number, string>());
        // The button is about to become disabled; hand the focus somewhere that stays on the page.
        if (updated.steps.length <= premiseCount(updated.steps)) {
          newProofButtonRef.current?.focus();
        }
      } else {
        setUndoError(result.message || 'Could not undo the last step.');
      }
    });
  };

  const hasProof = proof.goal !== '' || proof.steps.length > 0;

  return (
    <div className="App">
      {/* While the modal is open the page behind it can neither be tabbed to nor read by assistive technology. */}
      <div inert={isModalOpen}>
        <Header />
        <div className="app-toolbar">
          <button
            className="undo-btn"
            onClick={handleUndo}
            disabled={!canUndo || isUndoing}
            aria-disabled={!canUndo || isUndoing}
          >
            {isUndoing ? 'Undoing…' : 'Undo last step'}
          </button>
          <button
            className="copy-text-btn"
            onClick={handleCopyText}
            disabled={proof.steps.length === 0}
            aria-disabled={proof.steps.length === 0}
          >
            Copy proof as text
          </button>
          <button
            ref={newProofButtonRef}
            className="new-proof-btn"
            onClick={handleOpenModal}
            aria-label="Start a new proof"
          >
            New Proof
          </button>
          {confirmingNewProof && (
            <div className="confirm-bar" role="alertdialog" aria-labelledby="confirm-new-proof-title" aria-describedby="confirm-new-proof-desc">
              <span id="confirm-new-proof-title" className="visually-hidden">Discard the current proof?</span>
              <span id="confirm-new-proof-desc">Starting a new proof discards the current one. Continue?</span>
              <button className="confirm-btn" onClick={handleConfirmNewProof}>Discard and start new</button>
              <button ref={confirmCancelRef} className="cancel-btn" onClick={handleCancelNewProof}>Cancel</button>
            </div>
          )}
        </div>
        {undoError && (
          <p className="undo-error" role="alert" aria-live="assertive">{undoError}</p>
        )}
        {currentCopy?.copied === true && (
          <p className="copy-status" role="status">
            Proof copied to the clipboard as text. To load it again, paste it in the New Proof dialog.
          </p>
        )}
        {currentCopy?.copied === false && (
          <div className="copy-status">
            <p role="status">The clipboard is not available here. Copy the proof text below by hand.</p>
            <label htmlFor="copied-proof-text" className="visually-hidden">Proof as text</label>
            <textarea
              id="copied-proof-text"
              className="copy-fallback"
              value={currentCopy.text}
              readOnly
              rows={Math.min(proof.steps.length, 10)}
              wrap="off"
              onFocus={(e) => e.target.select()}
            />
          </div>
        )}
        <main className="app-main">
          <Menu key={proofId} ref={menuRef} logic={LOGIC} proof={proof} setProof={setProof} onColorChange={onColorChange} onNewProof={handleOpenModalFromMenu} />
          {hasProof ? (
            <Proof proof={proof} coloring={colorMapping} onSelectLine={handleSelectLine} />
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
        onLoadText={handleLoadText}
      />
    </div>
  );
}

export default App;
