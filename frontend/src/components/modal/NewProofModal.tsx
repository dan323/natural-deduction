import React, { FC, useState } from 'react';
import './NewProofModal.css';

type NewProofModalProps = {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (premises: string[], goal: string) => void;
};

const NewProofModal: FC<NewProofModalProps> = ({ isOpen, onClose, onSubmit }) => {
  const [premises, setPremises] = useState<string[]>(['']);
  const [goal, setGoal] = useState('');

  const handleAddPremise = () => setPremises([...premises, '']);
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
    <div className="modal">
      <div className="modal-content">
        <h2>New Proof</h2>
        <div className="modal-body">
          <label>Premises:</label>
          {premises.map((premise, index) => (
            <input
              key={premise+index}
              type="text"
              value={premise}
              onChange={(e) => handlePremiseChange(index, e.target.value)}
              placeholder="Enter a premise"
            />
          ))}
          <button className="add-premise-btn" onClick={handleAddPremise}>
            + Add Premise
          </button>

          <label>Goal:</label>
          <input
            type="text"
            value={goal}
            onChange={(e) => handleGoalChange(e.target.value)}
            placeholder="Enter the goal"
          />
        </div>
        <div className="modal-footer">
          <button className="submit-btn" onClick={handleSubmit}>
            Submit
          </button>
          <button className="close-btn" onClick={onClose}>
            Close
          </button>
        </div>
      </div>
    </div>
  );
};

export default NewProofModal;
