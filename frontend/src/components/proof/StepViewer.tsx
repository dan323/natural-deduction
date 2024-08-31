import React, { FC, MouseEventHandler, useEffect, useState } from 'react';
import '../Expressions.css';
import { StepDto } from '../../types';
import { renderLogic, getIndentation, isValidCSSColor } from '../../service/utils';
import clsx from 'clsx';

type StepProps = {
  step: StepDto,
  stepIndex: number,
  className?: string,
  onMouseEnter: MouseEventHandler<HTMLTableRowElement>,
  onMouseLeave: MouseEventHandler<HTMLTableRowElement>,
  color?: string,  // Color prop that will be validated
}

export const StepViewer: FC<StepProps> = ({
  step, stepIndex, className, onMouseEnter, onMouseLeave, color
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
    ? { '--glow-color': validColor } as React.CSSProperties
    : {};

  return (
    <tr className={clsx('step-viewer', className, { 'glow': validColor })}
      onMouseEnter={onMouseEnter}
      onMouseLeave={onMouseLeave}
      style={glowStyle}  // Apply custom glow style
    >
      <td>{stepIndex + 1}</td>
      <td>
        <pre>{getIndentation(step.assmsLevel)}{renderLogic(step.expression)}</pre>
      </td>
      <td className='rule'>{renderLogic(step.rule)}</td>
    </tr>
  );
};

export default StepViewer;
