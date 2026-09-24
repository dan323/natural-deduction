import { FC, Fragment, useState, useEffect, useLayoutEffect, useRef } from 'react';
import { checkFormula } from '../../service/utils';
import ConnectiveButtons from '../input/ConnectiveButtons';
import { syntaxHint } from '../input/connectives';
import { DEFAULT_LOGIC, LOGICS, logicInfo } from '../../constant';
import './NewProofModal.css';

type NewProofModalProps = {
  isOpen: boolean;
  // Who gets the focus back when the dialog closes. Whoever opens the dialog should read it before the page behind goes
  // inert; when it is left out the element that has the focus as the dialog opens is used.
  opener?: HTMLElement | null;
  onClose: () => void;
  // The logic the dialog's selector starts at, each time it opens: the one of the proof on screen.
  logic?: string;
  // Starts a proof of the chosen logic from the premises and the goal, once they pass `checkFormula`. Resolves to null once the proof is
  // shown, or to the reason it could not be started (the backend has the last word on what is a formula), which the
  // dialog shows while it stays open.
  onSubmit: (premises: string[], goal: string, logic: string) => Promise<string | null>;
  // Loads a finished proof of the chosen logic from its text (the layout "Copy proof as text" writes; the last line is its goal). Resolves
  // to null once the proof is loaded, or to the reason it could not be (the backend rejects an unfinished or invalid
  // proof). The dialog only offers loading from text when this is given.
  onLoadText?: (text: string, logic: string) => Promise<string | null>;
};

// A premise row. The id is what identifies the row for React, so that its error follows it when another row is removed.
// `error` is what checkFormula found wrong; it is only set when Start Proof is pressed, and cleared as soon as the text
// is edited.
type Premise = { id: number; text: string; error: string | null };

const SYNTAX_HINT_ID = 'new-proof-syntax-hint';

// The ids that describe a formula field: its error first, when it has one, then the syntax hint.
const describedBy = (errorId: string | false) => (errorId ? `${errorId} ${SYNTAX_HINT_ID}` : SYNTAX_HINT_ID);

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

const NewProofModal: FC<NewProofModalProps> = ({ isOpen, opener: openerProp, onClose, logic: initialLogic = DEFAULT_LOGIC, onSubmit, onLoadText }) => {
  const nextPremiseId = useRef(0);
  const newPremise = (): Premise => ({ id: nextPremiseId.current++, text: '', error: null });
  const [premises, setPremises] = useState<Premise[]>(() => [newPremise()]);
  const [goal, setGoal] = useState('');
  const [goalError, setGoalError] = useState<string | null>(null);
  // The logic of the proof to start or load.
  const [logic, setLogic] = useState(initialLogic);
  const premiseRefs = useRef<Array<HTMLInputElement | null>>([]);
  const goalRef = useRef<HTMLInputElement>(null);
  const dialogRef = useRef<HTMLDivElement>(null);
  const opener = useRef<HTMLElement | null>(null);
  // The premise that gets the focus after the next render, when a removal takes the focused one away.
  const pendingFocus = useRef<number | null>(null);
  // "Load from text": the pasted proof, why the last attempt failed, and whether one is in flight. While one is in
  // flight the text is frozen (read-only, so that it keeps the focus), as the premises and the goal are for Start Proof:
  // App shows the loaded proof as soon as the backend accepts it, so an edit made meanwhile would be silently dropped.
  const [proofText, setProofText] = useState('');
  const [loadError, setLoadError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  // Start Proof: why the backend refused the premises and the goal, and whether a request is in flight.
  const [submitError, setSubmitError] = useState<string | null>(null);
  // While a request is in flight the premises and the goal are frozen: App shows the proof as soon as the check passes,
  // so an edit made meanwhile would be silently dropped (or a refusal would describe text no longer shown). The fields
  // are read-only rather than disabled, so that one keeps the focus.
  const [isSubmitting, setIsSubmitting] = useState(false);
  const submitRef = useRef<HTMLButtonElement>(null);
  const proofTextRef = useRef<HTMLTextAreaElement>(null);
  // Bumped by every load, every Start Proof and every close, so that the answer to a request the user walked away from
  // (by closing the dialog, maybe opening it again since) neither closes the dialog nor touches its state.
  const loadAttempt = useRef(0);

  // A dialog that was closed opens empty the next time, in the logic of the proof on screen.
  useEffect(() => {
    if (isOpen) {
      setLogic(initialLogic);
      return;
    }
    loadAttempt.current++;
    setPremises([newPremise()]);
    setGoal('');
    setGoalError(null);
    setProofText('');
    setLoadError(null);
    setIsLoading(false);
    setSubmitError(null);
    setIsSubmitting(false);
    // Only when the dialog opens or closes: a change of `initialLogic` while it is open must not undo the user's choice.
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

  // Move focus into the dialog when it opens and hand it back to the opener when it closes. The move is delayed, so it
  // is skipped when the focus is already inside the dialog: otherwise it would take the focus away from whatever the
  // user reached in the meantime (e.g. the premise a connective button just typed into).
  useEffect(() => {
    if (!isOpen) return;
    const timer = setTimeout(() => {
      if (dialogRef.current?.contains(document.activeElement)) return;
      premiseRefs.current[0]?.focus();
    }, 50);
    return () => {
      clearTimeout(timer);
      opener.current?.focus();
    };
  }, [isOpen]);

  // A refused Start Proof hands the focus to the button its error describes, once the button is enabled again (a
  // disabled button cannot take the focus).
  useEffect(() => {
    if (submitError !== null) submitRef.current?.focus();
  }, [submitError]);

  const handleAddPremise = () => setPremises([...premises, newPremise()]);
  const handleRemovePremise = (index: number) => {
    if (premises.length === 1) return;
    setPremises(premises.filter((_, i) => i !== index));
    // The premise that slides into this position, or the one before it when the last one was removed.
    pendingFocus.current = Math.min(index, premises.length - 2);
  };
  const handlePremiseChange = (index: number, text: string) => {
    setPremises(premises.map((premise, i) => (i === index ? { ...premise, text, error: null } : premise)));
    setSubmitError(null);
  };
  const handleGoalChange = (value: string) => {
    setGoal(value);
    setGoalError(null);
    setSubmitError(null);
  };

  // The backend's refusal was about the other logic, so it no longer applies.
  const handleLogicChange = (value: string) => {
    setLogic(value);
    setSubmitError(null);
    setLoadError(null);
  };

  const handleSubmit = async () => {
    if (isSubmitting || isLoading) return;
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
    // `checkFormula` is only the instant check; the backend parses every formula again and may still refuse one.
    const attempt = ++loadAttempt.current;
    setIsSubmitting(true);
    setSubmitError(null);
    const error = await onSubmit(premises.map((premise) => premise.text.trim()).filter((text) => text !== ''), goal.trim(), logic);
    if (loadAttempt.current !== attempt) return;
    setIsSubmitting(false);
    if (error === null) {
      onClose();
    } else {
      setSubmitError(error);
    }
  };

  // Start Proof and Load proof share `loadAttempt`, so only one of them runs at a time.
  const canLoad = proofText.trim() !== '' && !isLoading && !isSubmitting;
  const handleLoad = async () => {
    if (!onLoadText || !canLoad) return;
    const attempt = ++loadAttempt.current;
    setIsLoading(true);
    setLoadError(null);
    const error = await onLoadText(proofText, logic);
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
          <label htmlFor="modal-logic" className="modal-section-label">Logic:</label>
          <select
            id="modal-logic"
            className="modal-logic-select"
            value={logic}
            onChange={(e) => handleLogicChange(e.target.value)}
            disabled={isSubmitting || isLoading}
            aria-describedby="modal-logic-desc"
          >
            {LOGICS.map((info) => <option key={info.id} value={info.id}>{info.name}</option>)}
          </select>
          <p id="modal-logic-desc" className="modal-logic-desc">{logicInfo(logic)?.description}</p>
          <p id={SYNTAX_HINT_ID} className="syntax-hint modal-syntax-hint">Syntax: {syntaxHint(logic)}</p>
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
                    readOnly={isSubmitting}
                    placeholder={`Premise ${index + 1}`}
                    aria-invalid={premise.error ? true : undefined}
                    aria-describedby={describedBy(!!premise.error && `premise-${index}-error`)}
                  />
                  {premises.length > 1 && (
                    <button
                      className="remove-premise-btn"
                      onClick={() => handleRemovePremise(index)}
                      disabled={isSubmitting}
                      aria-label={`Remove premise ${index + 1}`}
                      title="Remove"
                    >
                      ×
                    </button>
                  )}
                </div>
                <ConnectiveButtons
                  getInput={() => premiseRefs.current[index]}
                  onInsert={(text) => handlePremiseChange(index, text)}
                  target={`Premise ${index + 1}`}
                  disabled={isSubmitting}
                  logic={logic}
                />
                {premise.error && (
                  <p id={`premise-${index}-error`} className="modal-error" role="alert">{premise.error}</p>
                )}
              </Fragment>
            ))}
            <button className="add-premise-btn" onClick={handleAddPremise} disabled={isSubmitting}>
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
            readOnly={isSubmitting}
            placeholder="Enter the goal expression"
            required
            aria-invalid={goalError ? true : undefined}
            aria-describedby={describedBy(!!goalError && 'modal-goal-error')}
          />
          <ConnectiveButtons getInput={() => goalRef.current} onInsert={handleGoalChange} target="Goal" disabled={isSubmitting} logic={logic} />
          {goalError && <p id="modal-goal-error" className="modal-error" role="alert">{goalError}</p>}

          {onLoadText && (
            <section className="load-text-section" aria-labelledby="load-text-title">
              <h3 id="load-text-title" className="modal-section-label">Or load a proof from text</h3>
              <label htmlFor="modal-proof-text" className="modal-field-label">
                Proof text (a finished proof of the logic chosen above, as written by Copy proof as text, one step per line;
                its last line is the goal):
              </label>
              <textarea
                id="modal-proof-text"
                ref={proofTextRef}
                value={proofText}
                onChange={(e) => { setProofText(e.target.value); setLoadError(null); }}
                readOnly={isLoading}
                rows={5}
                wrap="off"
                spellCheck={false}
                aria-invalid={loadError ? true : undefined}
                aria-describedby={loadError ? 'modal-proof-text-error' : undefined}
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
        {submitError && (
          <p id="new-proof-submit-error" className="modal-error" role="alert">{submitError}</p>
        )}
        <div className="modal-footer">
          <button
            ref={submitRef}
            className="submit-btn"
            onClick={handleSubmit}
            disabled={goal.trim() === '' || isSubmitting || isLoading}
            aria-describedby={goal.trim() === '' ? 'new-proof-submit-hint' : submitError ? 'new-proof-submit-error' : undefined}
          >
            {isSubmitting ? 'Starting…' : 'Start Proof'}
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
