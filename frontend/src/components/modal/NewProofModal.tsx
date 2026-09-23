import { FC, Fragment, useState, useEffect, useLayoutEffect, useRef } from 'react';
import { checkFormula } from '../../service/utils';
import './NewProofModal.css';

type NewProofModalProps = {
  isOpen: boolean;
  // Who gets the focus back when the dialog closes. Whoever opens the dialog should read it before the page behind goes
  // inert; when it is left out the element that has the focus as the dialog opens is used.
  opener?: HTMLElement | null;
  onClose: () => void;
  onSubmit: (premises: string[], goal: string) => void;
  // Loads a proof from its text (the layout "Copy proof as text" writes) and its goal. Resolves to null once the
  // proof is loaded, or to the reason it could not be. The dialog only offers loading from text when this is given.
  onLoadText?: (text: string, goal: string) => Promise<string | null>;
};

// A premise row. The id is what identifies the row for React, so that its error follows it when another row is removed.
// `error` is what checkFormula found wrong; it is only set when Start Proof is pressed, and cleared as soon as the text
// is edited.
type Premise = { id: number; text: string; error: string | null };

const FOCUSABLE = 'button, input, select, textarea, a[href], [tabindex]:not([tabindex="-1"])';

// Keeps Tab and Shift+Tab inside the dialog: from the last control Tab goes to the first one, from the first one
// Shift+Tab goes to the last one, and a focus that somehow is outside the dialog is pulled back in. A native <dialog>
// would do this by itself, but jsdom has no showModal(), so the trap is done by hand (and the rest of the page is made
// inert by App).
function trapTab(event: KeyboardEvent, dialog: HTMLElement) {
  const focusable = Array.from(dialog.querySelectorAll<HTMLElement>(FOCUSABLE)).filter((element) => !element.hasAttribute('disabled'));
  if (focusable.length === 0) return;
  const edge = event.shiftKey ? focusable[0] : focusable[focusable.length - 1];
  const target = event.shiftKey ? focusable[focusable.length - 1] : focusable[0];
  if (document.activeElement === edge || !dialog.contains(document.activeElement)) {
    event.preventDefault();
    target.focus();
  }
}

const NewProofModal: FC<NewProofModalProps> = ({ isOpen, opener: openerProp, onClose, onSubmit, onLoadText }) => {
  const nextPremiseId = useRef(0);
  const newPremise = (): Premise => ({ id: nextPremiseId.current++, text: '', error: null });
  const [premises, setPremises] = useState<Premise[]>(() => [newPremise()]);
  const [goal, setGoal] = useState('');
  const [goalError, setGoalError] = useState<string | null>(null);
  const premiseRefs = useRef<Array<HTMLInputElement | null>>([]);
  const goalRef = useRef<HTMLInputElement>(null);
  const dialogRef = useRef<HTMLDivElement>(null);
  const opener = useRef<HTMLElement | null>(null);
  // The premise that gets the focus after the next render, when a removal takes the focused one away.
  const pendingFocus = useRef<number | null>(null);
  // "Load from text": the pasted proof, its goal, why the last attempt failed, and whether one is in flight.
  const [proofText, setProofText] = useState('');
  const [textGoal, setTextGoal] = useState('');
  const [loadError, setLoadError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const proofTextRef = useRef<HTMLTextAreaElement>(null);
  // Bumped by every load and every close, so that the answer to a load the user walked away from (by closing the
  // dialog, maybe opening it again since) neither closes the dialog nor touches its state.
  const loadAttempt = useRef(0);

  // A dialog that was closed opens empty the next time.
  useEffect(() => {
    if (isOpen) return;
    loadAttempt.current++;
    setPremises([newPremise()]);
    setGoal('');
    setGoalError(null);
    setProofText('');
    setTextGoal('');
    setLoadError(null);
    setIsLoading(false);
  }, [isOpen]);

  useEffect(() => {
    if (pendingFocus.current === null) return;
    premiseRefs.current[pendingFocus.current]?.focus();
    pendingFocus.current = null;
  }, [premises]);

  // Close on Escape key, and keep Tab inside the dialog
  useEffect(() => {
    if (!isOpen) return;
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
      else if (e.key === 'Tab' && dialogRef.current) trapTab(e, dialogRef.current);
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  // Note who gets the focus back on close. The rest of the page goes inert when the dialog opens, which in a browser
  // takes the focus away from the opener, so the opener passed by the caller is preferred to reading it here.
  useLayoutEffect(() => {
    if (isOpen) opener.current = openerProp ?? (document.activeElement instanceof HTMLElement ? document.activeElement : null);
  }, [isOpen, openerProp]);

  // Move focus into the dialog when it opens and hand it back to the opener when it closes.
  useEffect(() => {
    if (!isOpen) return;
    const timer = setTimeout(() => premiseRefs.current[0]?.focus(), 50);
    return () => {
      clearTimeout(timer);
      opener.current?.focus();
    };
  }, [isOpen]);

  const handleAddPremise = () => setPremises([...premises, newPremise()]);
  const handleRemovePremise = (index: number) => {
    if (premises.length === 1) return;
    setPremises(premises.filter((_, i) => i !== index));
    // The premise that slides into this position, or the one before it when the last one was removed.
    pendingFocus.current = Math.min(index, premises.length - 2);
  };
  const handlePremiseChange = (index: number, text: string) => {
    setPremises(premises.map((premise, i) => (i === index ? { ...premise, text, error: null } : premise)));
  };
  const handleGoalChange = (value: string) => {
    setGoal(value);
    setGoalError(null);
  };

  const handleSubmit = () => {
    // A blank premise is just an unused row and is left out; everything else has to look like a formula.
    const checked = premises.map((premise) => (
      { ...premise, error: premise.text.trim() === '' ? null : checkFormula(premise.text) }
    ));
    const newGoalError = checkFormula(goal);
    if (newGoalError !== null || checked.some((premise) => premise.error !== null)) {
      setPremises(checked);
      setGoalError(newGoalError);
      const firstInvalid = checked.findIndex((premise) => premise.error !== null);
      (firstInvalid >= 0 ? premiseRefs.current[firstInvalid] : goalRef.current)?.focus();
      return;
    }
    onSubmit(premises.map((premise) => premise.text.trim()).filter((text) => text !== ''), goal.trim());
    onClose();
  };

  const canLoad = proofText.trim() !== '' && textGoal.trim() !== '' && !isLoading;
  const handleLoad = async () => {
    if (!onLoadText || !canLoad) return;
    const attempt = ++loadAttempt.current;
    setIsLoading(true);
    setLoadError(null);
    const error = await onLoadText(proofText, textGoal.trim());
    if (loadAttempt.current !== attempt) return;
    setIsLoading(false);
    if (error === null) {
      onClose();
    } else {
      setLoadError(error);
      proofTextRef.current?.focus();
    }
  };

  if (!isOpen) return null;

  return (
    <div
      ref={dialogRef}
      className="modal"
      role="dialog"
      aria-modal="true"
      aria-labelledby="new-proof-modal-title"
      onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}
    >
      <div className="modal-content">
        <h2 id="new-proof-modal-title">New Proof</h2>
        <div className="modal-body">
          <div role="group" aria-labelledby="new-proof-premises-label">
            <span id="new-proof-premises-label" className="modal-section-label">Premises:</span>
            {premises.map((premise, index) => (
              <Fragment key={premise.id}>
                <div className="premise-row">
                  <label htmlFor={`premise-${index}`} className="visually-hidden">{`Premise ${index + 1}`}</label>
                  <input
                    ref={(element) => { premiseRefs.current[index] = element; }}
                    id={`premise-${index}`}
                    type="text"
                    value={premise.text}
                    onChange={(e) => handlePremiseChange(index, e.target.value)}
                    placeholder={`Premise ${index + 1}`}
                    aria-invalid={premise.error ? true : undefined}
                    aria-describedby={premise.error ? `premise-${index}-error` : undefined}
                  />
                  {premises.length > 1 && (
                    <button
                      className="remove-premise-btn"
                      onClick={() => handleRemovePremise(index)}
                      aria-label={`Remove premise ${index + 1}`}
                      title="Remove"
                    >
                      ×
                    </button>
                  )}
                </div>
                {premise.error && (
                  <p id={`premise-${index}-error`} className="modal-error" role="alert">{premise.error}</p>
                )}
              </Fragment>
            ))}
            <button className="add-premise-btn" onClick={handleAddPremise}>
              + Add Premise
            </button>
          </div>

          <label htmlFor="modal-goal" className="modal-section-label">Goal:</label>
          <input
            id="modal-goal"
            ref={goalRef}
            type="text"
            value={goal}
            onChange={(e) => handleGoalChange(e.target.value)}
            placeholder="Enter the goal expression"
            required
            aria-invalid={goalError ? true : undefined}
            aria-describedby={goalError ? 'modal-goal-error' : undefined}
          />
          {goalError && <p id="modal-goal-error" className="modal-error" role="alert">{goalError}</p>}

          {onLoadText && (
            <section className="load-text-section" aria-labelledby="load-text-title">
              <h3 id="load-text-title" className="modal-section-label">Or load a proof from text</h3>
              <label htmlFor="modal-proof-text" className="modal-field-label">
                Proof text (as written by Copy proof as text, one step per line):
              </label>
              <textarea
                id="modal-proof-text"
                ref={proofTextRef}
                value={proofText}
                onChange={(e) => { setProofText(e.target.value); setLoadError(null); }}
                rows={5}
                wrap="off"
                spellCheck={false}
                aria-invalid={loadError ? true : undefined}
                aria-describedby={loadError ? 'modal-proof-text-error' : undefined}
              />
              <label htmlFor="modal-proof-text-goal" className="modal-field-label">
                Goal of the loaded proof:
              </label>
              <input
                id="modal-proof-text-goal"
                required
                type="text"
                value={textGoal}
                onChange={(e) => { setTextGoal(e.target.value); setLoadError(null); }}
              />
              {loadError && <p id="modal-proof-text-error" className="modal-error" role="alert">{loadError}</p>}
              <button
                className="load-text-btn"
                onClick={handleLoad}
                disabled={!canLoad}
                aria-disabled={!canLoad}
              >
                {isLoading ? 'Loading…' : 'Load proof'}
              </button>
            </section>
          )}
        </div>
        {goal.trim() === '' && (
          <p id="new-proof-submit-hint" className="modal-hint">Enter the goal to start the proof.</p>
        )}
        <div className="modal-footer">
          <button
            className="submit-btn"
            onClick={handleSubmit}
            disabled={goal.trim() === ''}
            aria-describedby={goal.trim() === '' ? 'new-proof-submit-hint' : undefined}
          >
            Start Proof
          </button>
          <button className="close-btn" onClick={onClose}>
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
};

export default NewProofModal;
