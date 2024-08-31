import React, { FC, useEffect, useState } from 'react';
import './goal.css';
import { renderLogic } from '../../service/utils';

type GoalProps = {
  expression: string;
  success: boolean;
};

const emojis = ['🎉', '🎊', '🎈', '🥳', '✨']; // Array of emojis for celebration

const Goal: FC<GoalProps> = ({ expression, success }) => {
  const [showCelebration, setShowCelebration] = useState(false);

  useEffect(() => {
    if (success) {
      setShowCelebration(true);
      const timer = setTimeout(() => setShowCelebration(false), 2000); // Hide celebration after 2 seconds
      return () => clearTimeout(timer);
    }
  }, [success]);

  return (
    <div className="goal" aria-live="polite">
      <span className="goal-label">GOAL:</span>
      <span className={success ? 'goal-success' : 'goal-failure'}>
        {renderLogic(expression)}
      </span>
      {showCelebration && (
        <div className="celebration">
          {Array.from({ length: 30 }).map((_, index) => {
            const emoji = emojis[Math.floor(Math.random() * emojis.length)];
            return (
              <span
                key={index}
                className="emoji"
                style={{
                  left: `${Math.random() * 100}vw`, // Random horizontal position
                  bottom: `${Math.random() * 100}vh`, // Random vertical position
                  animationDelay: `${Math.random() * 2}s`, // Random delay for staggering animation
                  fontSize: `${Math.random() * 30 + 20}px`, // Random size for variety
                }}
              >
                {emoji}
              </span>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default Goal;
