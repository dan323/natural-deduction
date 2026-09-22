import { CSSProperties, FC, FocusEventHandler, KeyboardEventHandler, MouseEventHandler, useEffect, useState } from 'react';
import '../Expressions.css';
import { StepDto } from '../../types';
import { renderExpression, renderRule, isValidCSSColor } from '../../service/utils';
import clsx from 'clsx';

type StepProps = {
  step: StepDto,
  stepIndex: number,
  className?: string,
  onMouseEnter: MouseEventHandler<HTMLTableRowElement>,
  onMouseLeave: MouseEventHandler<HTMLTableRowElement>,
  // Focus does what the mouse does, so that the lines a step cites also light up from the keyboard.
  onFocus?: FocusEventHandler<HTMLTableRowElement>,
  onBlur?: FocusEventHandler<HTMLTableRowElement>,
  // Called with the 1-based line number when the row is clicked, or activated with Enter or Space.
  onSelect?: (line: number) => void,
  color?: string,  // Color prop that will be validated
  // How many of the step's assumption levels are still open. A level closes when a later step drops below it, and
  // the step is then discharged. The default is every level open, which is right for the last steps of a proof.
  openLevels?: number,
}

export const StepViewer: FC<StepProps> = ({
  step, stepIndex, className, onMouseEnter, onMouseLeave, onFocus, onBlur, onSelect, color, openLevels
}) => {
  const [validColor, setValidColor] = useState<string | null>(null);

  useEffect(() => {
    if (color && isValidCSSColor(color)) {
      setValidColor(color);
    } else {
      setValidColor(null);  // Reset if color is invalid
    }
  }, [color]);

  // Cast to CSSProperties to ensure custom properties are accepted
  const glowStyle = validColor
    ? { '--glow-color': validColor } as CSSProperties
    : {};

  const handleKeyDown: KeyboardEventHandler<HTMLTableRowElement> = (event) => {
    // Only the row itself: a key pressed in something inside it keeps its own meaning.
    if (event.target !== event.currentTarget) return;
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault(); // Space would otherwise scroll the page
      onSelect?.(stepIndex + 1);
    }
  };

  const level = Math.max(step.assmsLevel, 0);
  const open = Math.min(openLevels ?? level, level);
  const discharged = open < level;

  return (
    <tr className={clsx('step-viewer', className, { 'glow': validColor, 'discharged': discharged })}
      onMouseEnter={onMouseEnter}
      onMouseLeave={onMouseLeave}
      onFocus={onFocus}
      onBlur={onBlur}
      onClick={() => onSelect?.(stepIndex + 1)}
      onKeyDown={handleKeyDown}
      tabIndex={0}
      style={glowStyle}  // Apply custom glow style
    >
      <th scope="row" className="line-number">
        {stepIndex + 1}
        <span className="visually-hidden">
          {`, assumption level ${level}${discharged ? ', discharged' : ''}`}
          {validColor && ', cited by current input'}
        </span>
      </th>
      <td className="step-cell">
        {/* One rule per assumption level, drawn solid while its subproof is open and dashed once it is closed. The
            level is spoken in the row header, so the rules are only decoration. */}
        <div className="step-body">
          {Array.from({ length: level }, (_, depth) => (
            <span key={depth} aria-hidden="true"
              className={clsx('subproof-rule', { 'closed': depth >= open })} />
          ))}
          <pre>{renderExpression(step.expression)}</pre>
        </div>
      </td>
      <td className='rule'>{renderRule(step.rule)}</td>
    </tr>
  );
};

export default StepViewer;
