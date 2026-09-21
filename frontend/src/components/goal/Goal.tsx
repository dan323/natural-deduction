import { CSSProperties, FC, useEffect, useState } from 'react';
import './goal.css';
import { renderExpression } from '../../service/utils';

type GoalProps = {
  expression: string;
  success: boolean;
};

const emojis = ['🎉', '🎊', '🎈', '🥳', '✨']; // Array of emojis for celebration

type Confetti = { emoji: string; style: CSSProperties };

// Rolls the confetti once per celebration. It must not run during render, or every re-render would reshuffle it.
function rollConfetti(): Confetti[] {
  return Array.from({ length: 30 }).map(() => ({
    emoji: emojis[Math.floor(Math.random() * emojis.length)],
    style: {
      left: `${Math.random() * 100}vw`, // Random horizontal position
      bottom: `${Math.random() * 100}vh`, // Random vertical position
      animationDelay: `${Math.random() * 2}s`, // Random delay for staggering animation
      fontSize: `${Math.random() * 30 + 20}px`, // Random size for variety
    },
  }));
}

const Goal: FC<GoalProps> = ({ expression, success }) => {
  const [confetti, setConfetti] = useState<Confetti[] | null>(null);

  useEffect(() => {
    if (success) {
      setConfetti(rollConfetti());
      const timer = setTimeout(() => setConfetti(null), 2000); // Hide celebration after 2 seconds
      return () => clearTimeout(timer);
    }
  }, [success]);

  return (
    <div className="goal" aria-live="polite">
      <span className="goal-label">GOAL:</span>
      <span className={success ? 'goal-success' : 'goal-failure'}>
        {renderExpression(expression)}
      </span>
      {confetti && (
        <div className="celebration" aria-hidden="true">
          {confetti.map(({ emoji, style }, index) => (
            <span key={"emoji" + index} className="emoji" style={style}>
              {emoji}
            </span>
          ))}
        </div>
      )}
    </div>
  );
};

export default Goal;
