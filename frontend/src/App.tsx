import { ReactNode, useCallback, useEffect, useRef, useState } from 'react';
import './App.css';
import Proof from './components/proof/ProofViewer';
import Header from './components/Header';
import Menu, { MenuHandle } from './components/menu/Menu';
import NewProofModal from './components/modal/NewProofModal';
import { StepDto, ProofDto } from './types';
import { LOGIC } from './constant';
import { loadProofFromText, replayProof, undoLastStep } from './service/actions';
import { loadedGoal, loadsBackWithSameGoal, proofToText } from './service/utils';
import { clearSavedProof, readSavedProof, writeSavedProof } from './service/savedProof';

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

// The proof "Try an example" starts: p -> q and p prove q in one Modus Ponens step.
const EXAMPLE_PREMISES = ['p -> q', 'p'];
const EXAMPLE_GOAL = 'q';

// What happens to a copied proof text in the New Proof dialog: `POST .../proof` only reads a finished proof, and takes
// its last line as the goal. `done` alone does not promise that the text loads back as the same proof: a top-level step
// other than the last may be the goal (e.g. a premise that already is the goal).
function copyLoadNote(proof: ProofDto): ReactNode {
  if (!proof.done) return 'It only loads back in the New Proof dialog once the proof is finished.';
  if (loadsBackWithSameGoal(proof)) return 'To load it again, paste it in the New Proof dialog.';
  return <>Its last line is not the goal, so it loads back in the New Proof dialog with the goal <code>{loadedGoal(proof)}</code> instead of <code>{proof.goal}</code>.</>;
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
  // Bumped whenever the user starts getting a new proof ("Try an example", or New Proof opening its dialog or its
  // confirmation), before that proof is shown. `proofIdRef` only changes once a proof is on screen, so the restore of
  // the saved proof reads this one too: its answer must not replace a proof the user already asked for.
  const userStartedRef = useRef(0);
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
  // Bumped whenever the New Proof dialog closes, so that a "Load from text" or Start Proof answer arriving after the
  // user cancelled the dialog is dropped instead of replacing the proof on screen.
  const dialogSessionRef = useRef(0);
  // "Try an example": whether its proof is being checked by the backend, and why it could not be started.
  const [isStartingExample, setIsStartingExample] = useState(false);
  const [exampleError, setExampleError] = useState('');
  // Whether the proof saved before a reload is being replayed by the backend, and why it could not be restored.
  const [isRestoring, setIsRestoring] = useState(false);
  const [restoreError, setRestoreError] = useState('');

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
    userStartedRef.current += 1;
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
    setRestoreError('');
    proofIdRef.current += 1;
    setProofId(proofIdRef.current);
  };

  // On mount, brings back the proof saved before a reload (see the effect below that saves it). It goes through the
  // backend's replay like any other proof, so that it is checked again and comes back with its `done` verdict; a proof
  // the backend rejects as invalid (a 400), or saved text that is not a proof, is forgotten and the empty state says so.
  // Any other failure (the backend unreachable or failing) says nothing about the proof, so it stays saved for the next
  // reload to try again. An answer arriving after the user started another proof meanwhile ("Try an example" and New
  // Proof stay usable) is dropped, and the saved proof is left as it is until that proof replaces it; so is one for an
  // unmounted App (React mounts twice in development).
  useEffect(() => {
    const saved = readSavedProof(LOGIC);
    if (saved.kind === 'none') return;
    if (saved.kind === 'corrupt') {
      clearSavedProof();
      setRestoreError('The proof saved before the page was reloaded could not be read, so it was discarded.');
      return;
    }
    let active = true;
    const requestedProofId = proofIdRef.current;
    const requestedStart = userStartedRef.current;
    setIsRestoring(true);
    replayProof(LOGIC, saved.proof, (result) => {
      if (!active) return;
      setIsRestoring(false);
      if (proofIdRef.current !== requestedProofId) return;
      if (userStartedRef.current !== requestedStart) {
        // The user asked for another proof while this one was being restored (e.g. opened the New Proof dialog, which
        // did not ask to discard anything since nothing was on screen yet). Say so in the empty state, in case they
        // cancel; a proof they start replaces the notice and the saved proof.
        if (result.proof) {
          setRestoreError('The proof saved before the page was reloaded was not restored, since a new proof was started meanwhile. It is still saved: reload the page to restore it.');
        }
        return;
      }
      if (result.proof) {
        showNewProof(result.proof);
      } else if (result.status === 400) {
        clearSavedProof();
        setRestoreError(`The proof saved before the page was reloaded could not be restored: ${result.message || 'the backend rejected it.'}`);
      } else {
        // Not a verdict on the proof (no connection, a server error, a busy server): keep it for the next reload.
        setRestoreError(`The proof saved before the page was reloaded could not be restored right now (${result.message}). It is still saved: reload the page to try again.`);
      }
    });
    return () => { active = false; };
    // Only on mount: `showNewProof` only calls state setters and reads refs.
  }, []);

  // Saves every change of the proof on screen (a new proof, a rule, an undo, a solve), for the effect above to restore
  // after a reload. The empty state is never saved, so that it cannot overwrite a saved proof that is still being
  // restored.
  useEffect(() => {
    if (proof.goal !== '' || proof.steps.length > 0) writeSavedProof(proof);
  }, [proof]);

  // Has the backend check a proof of only its premises and goal (see `replayProof`, the path Undo uses: it parses the
  // goal and every step), since `checkFormula` is only an instant check that can drift from the backend's parser.
  // Resolves to the proof as the backend returned it, or to the reason it was refused.
  const checkNewProof = (premises: string[], goal: string) => new Promise<{ proof: ProofDto } | { error: string }>((resolve) => {
    const steps: StepDto[] = premises.map((premise) => ({
      expression: premise.trim(),
      rule: 'Ass',
      assmsLevel: 0,
      extraParameters: {},
    }));
    replayProof(LOGIC, { steps: steps, logic: LOGIC, goal: goal }, (result) => resolve(result.proof
      ? { proof: result.proof }
      : { error: result.message || 'Could not start the proof.' }));
  });

  // Start Proof in the New Proof dialog: the proof is only shown once the backend accepted it, and a refusal keeps the
  // dialog open with the reason. An answer arriving after the dialog was closed is dropped.
  const handleNewProofSubmit = async (premises: string[], goal: string): Promise<string | null> => {
    const session = dialogSessionRef.current;
    const checked = await checkNewProof(premises, goal);
    if (dialogSessionRef.current !== session) return null;
    if ('error' in checked) return checked.error;
    showNewProof(checked.proof);
    return null;
  };

  // Starts the example exactly as if its premises and goal had been typed in the New Proof dialog, through the same
  // backend check. The button goes away with the empty state, so hand the focus to a control that stays on the page,
  // as `handleUndo` does. An answer arriving after another proof was started (the dialog can be opened meanwhile) is
  // dropped.
  const handleTryExample = async () => {
    if (isStartingExample) return;
    userStartedRef.current += 1;
    setExampleError('');
    setIsStartingExample(true);
    const requestedProofId = proofIdRef.current;
    const checked = await checkNewProof(EXAMPLE_PREMISES, EXAMPLE_GOAL);
    setIsStartingExample(false);
    if (proofIdRef.current !== requestedProofId) return;
    if ('error' in checked) {
      setExampleError(checked.error);
      return;
    }
    showNewProof(checked.proof);
    newProofButtonRef.current?.focus();
  };

  // "Load from text" in the New Proof dialog: the backend reads the text as a finished proof (see `loadProofFromText`),
  // which is only shown once it is accepted; a refusal keeps the dialog open with the reason. An answer arriving after
  // the dialog was closed is dropped.
  const handleLoadText = (text: string) => new Promise<string | null>((resolve) => {
    const session = dialogSessionRef.current;
    loadProofFromText(LOGIC, text, (result) => {
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

  // Copies the proof in the text layout the backend reads back (see `proofToText`), for "Load from text" or a file. The
  // backend only reads back a finished proof and takes its last line as the goal, which the notice says when this one
  // is not done yet, or when its last line is not the goal (see `copyLoadNote`).
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
            Proof copied to the clipboard as text. {copyLoadNote(currentCopy.proof)}
          </p>
        )}
        {currentCopy?.copied === false && (
          <div className="copy-status">
            <p role="status">
              The clipboard is not available here. Copy the proof text below by hand.{' '}
              {!loadsBackWithSameGoal(currentCopy.proof) && copyLoadNote(currentCopy.proof)}
            </p>
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
            <div className="empty-proof-state">
              <p role="status">
                {isRestoring
                  ? 'Restoring the proof from before the page was reloaded…'
                  : <>No proof loaded. Click <strong>New Proof</strong> to begin.</>}
              </p>
              {restoreError && <p className="restore-error" role="alert">{restoreError}</p>}
              <h2 className="how-it-works-title">How it works</h2>
              <ol className="how-it-works">
                <li>Enter the premises and the goal of the proof.</li>
                <li>Pick an inference rule.</li>
                <li>Enter the line numbers the rule uses, and apply it.</li>
              </ol>
              <button className="try-example-btn" onClick={handleTryExample} disabled={isStartingExample}>
                {isStartingExample ? 'Starting the example…' : 'Try an example'}
              </button>
              {exampleError && <p className="try-example-error" role="alert">{exampleError}</p>}
              <p className="try-example-desc">
                Premises <code>{EXAMPLE_PREMISES.join(', ')}</code>, goal <code>{EXAMPLE_GOAL}</code>.
              </p>
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
