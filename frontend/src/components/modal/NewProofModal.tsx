import { FC, Fragment, useState, useEffect, useRef } from 'react';
import { checkFormula } from '../../service/utils';
import './NewProofModal.css';

type NewProofModalProps = {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (premises: string[], goal: string) => void;
};

// A premise row. The id is what identifies the row for React, so that its error follows it when another row is removed.
// `error` is what checkFormula found wrong; it is only set when Start Proof is pressed, and cleared as soon as the text
// is edited.
type Premise = { id: number; text: string; error: string | null };

const NewProofModal: FC<NewProofModalProps> = ({ isOpen, onClose, onSubmit }) => {
  const nextPremiseId = useRef(0);
  const newPremise = (): Premise => ({ id: nextPremiseId.current++, text: '', error: null });
  const [premises, setPremises] = useState<Premise[]>(() => [newPremise()]);
  const [goal, setGoal] = useState('');
  const [goalError, setGoalError] = useState<string | null>(null);
  const premiseRefs = useRef<Array<HTMLInputElement | null>>([]);
  const goalRef = useRef<HTMLInputElement>(null);
  // The premise that gets the focus after the next render, when a removal takes the focused one away.
  const pendingFocus = useRef<number | null>(null);

  // A dialog that was closed opens empty the next time.
  useEffect(() => {
    if (isOpen) return;
    setPremises([newPremise()]);
    setGoal('');
    setGoalError(null);
  }, [isOpen]);

  useEffect(() => {
    if (pendingFocus.current === null) return;
    premiseRefs.current[pendingFocus.current]?.focus();
    pendingFocus.current = null;
  }, [premises]);

  // Close on Escape key
  useEffect(() => {
    if (!isOpen) return;
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  // Move focus into the dialog when it opens and hand it back to whatever had it (the button that opened it) when it
  // closes.
  useEffect(() => {
    if (!isOpen) return;
    const previouslyFocused = document.activeElement instanceof HTMLElement ? document.activeElement : null;
    const timer = setTimeout(() => premiseRefs.current[0]?.focus(), 50);
    return () => {
      clearTimeout(timer);
      previouslyFocused?.focus();
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
    onSubmit(premises.map((premise) => premise.text).filter((text) => text.trim() !== ''), goal.trim());
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div
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
