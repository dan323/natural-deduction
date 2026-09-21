import { CSSProperties, FC, FocusEventHandler, KeyboardEventHandler, MouseEventHandler, useEffect, useState } from 'react';
import '../Expressions.css';
import { StepDto } from '../../types';
import { renderExpression, renderRule, getIndentation, isValidCSSColor } from '../../service/utils';
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
}

export const StepViewer: FC<StepProps> = ({
  step, stepIndex, className, onMouseEnter, onMouseLeave, onFocus, onBlur, onSelect, color
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

  return (
    <tr className={clsx('step-viewer', className, { 'glow': validColor })}
      onMouseEnter={onMouseEnter}
      onMouseLeave={onMouseLeave}
      onFocus={onFocus}
      onBlur={onBlur}
      onClick={() => onSelect?.(stepIndex + 1)}
      onKeyDown={handleKeyDown}
      tabIndex={0}
      style={glowStyle}  // Apply custom glow style
    >
      <td>{stepIndex + 1}</td>
      <td>
        <pre>{getIndentation(step.assmsLevel)}{renderExpression(step.expression)}</pre>
      </td>
      <td className='rule'>{renderRule(step.rule)}</td>
    </tr>
  );
};

export default StepViewer;
