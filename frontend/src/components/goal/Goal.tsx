import React, { FC } from 'react';
import './goal.css';
import { renderLogic } from '../../service/utils';

type GoalProps = {
  expression: string;
  success: boolean;  // Use the lowercase `boolean` for TypeScript's type
};

const Goal: FC<GoalProps> = ({ expression, success }) => {
  return (
    <div className="goal" aria-live="polite">
      <span className="goal-label">GOAL:</span>
      <span className={success ? 'goal-success' : 'goal-failure'}>
        {renderLogic(expression)}
      </span>
    </div>
  );
};

export default Goal;
