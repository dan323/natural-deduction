import React, { FC, useState, useEffect, useRef } from 'react';
import './NewProofModal.css';

type NewProofModalProps = {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (premises: string[], goal: string) => void;
};

const NewProofModal: FC<NewProofModalProps> = ({ isOpen, onClose, onSubmit }) => {
  const [premises, setPremises] = useState<string[]>(['']);
  const [goal, setGoal] = useState('');
  const firstInputRef = useRef<HTMLInputElement>(null);

  // Close on Escape key
  useEffect(() => {
    if (!isOpen) return;
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  // Focus first input when modal opens
  useEffect(() => {
    if (isOpen) {
      setTimeout(() => firstInputRef.current?.focus(), 50);
    }
  }, [isOpen]);

  const handleAddPremise = () => setPremises([...premises, '']);
  const handleRemovePremise = (index: number) => {
    if (premises.length === 1) return;
    setPremises(premises.filter((_, i) => i !== index));
  };
  const handlePremiseChange = (index: number, value: string) => {
    const newPremises = [...premises];
    newPremises[index] = value;
    setPremises(newPremises);
  };
  const handleGoalChange = (value: string) => setGoal(value);

  const handleSubmit = () => {
    onSubmit(premises.filter((premise: string) => premise.trim() !== ''), goal.trim());
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
          <label className="modal-section-label">Premises:</label>
          {premises.map((premise, index) => (
            <div key={index} className="premise-row">
              <input
                ref={index === 0 ? firstInputRef : undefined}
                id={`premise-${index}`}
                type="text"
                value={premise}
                onChange={(e) => handlePremiseChange(index, e.target.value)}
                placeholder={`Premise ${index + 1}`}
                aria-label={`Premise ${index + 1}`}
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
          ))}
          <button className="add-premise-btn" onClick={handleAddPremise}>
            + Add Premise
          </button>

          <label htmlFor="modal-goal" className="modal-section-label">Goal:</label>
          <input
            id="modal-goal"
            type="text"
            value={goal}
            onChange={(e) => handleGoalChange(e.target.value)}
            placeholder="Enter the goal expression"
          />
        </div>
        <div className="modal-footer">
          <button
            className="submit-btn"
            onClick={handleSubmit}
            disabled={goal.trim() === ''}
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
