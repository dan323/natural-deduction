import React, { FC } from 'react';
import './Expressions.css';

import { renderLogic } from '../service/utils';

type GoalProps = {
  expression: string,
  success: Boolean,
}

const Goal: FC<GoalProps> = ({ expression, success }) => {
  return (
    <div className="goal">
      <div>GOAL: </div><div style={success ? { color: 'green' } : { color: 'red' }}> {renderLogic(expression)}</div>
    </div>
  );
};

export default Goal;