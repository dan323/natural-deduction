import React, { FC, useState } from 'react';
import '../Expressions.css';
import { ProofDto } from '../../types';
import Goal from '../goal/Goal';
import StepViewer from './StepViewer';

type RangeDto = {
  start: number,
  end: number
}

type ProofViewerProps = { 
  proof: ProofDto, 
  coloring: Map<number, string>
}

export const ProofViewer: FC<ProofViewerProps> = ({ proof, coloring }) => {
  const [hoveredRange, setHoveredRange] = useState<RangeDto[] | null>(null);

  const handleMouseEnter = (rule: string) => {
    const range = parseRangeFromRule(rule);
    setHoveredRange(range);
  };

  const handleMouseLeave = () => {
    setHoveredRange(null);
  };

  const parseRangeFromRule = (rule: string): RangeDto[] | null => {
    let match = /\[(\d+(-\d+)?(, \d+(-\d+)?)*?)\]/.exec(rule);
    if (match) {
      const parts = match[1].split(',');
      let ranges: RangeDto[] = [];

      parts.forEach(part => {
        const rangeMatch = /(\d+)-(\d+)/.exec(part);
        if (rangeMatch) {
          ranges.push({ start: parseInt(rangeMatch[1]), end: parseInt(rangeMatch[2]) });
        } else {
          const number = parseInt(part);
          ranges.push({ start: number, end: number });
        }
      });

      return ranges;
    }
    return null;
  };

  const isHighlighted = (index: number): boolean => {
    if (!hoveredRange) return false;
    return hoveredRange.some(range =>
      index + 1 >= range.start && index + 1 <= range.end
    );
  };

  return (
    <div className="proof-viewer-wrapper">
      <hr className="proof-divider" />
      <div className="proof-viewer">
        <table aria-label="Proof steps">
          <thead>
            <tr>
              <th scope="col">Line</th>
              <th scope="col">Step</th>
              <th scope="col">Rule</th>
            </tr>
          </thead>
          <tbody>
            {proof.steps.map((step, index) => {
              return (
                <StepViewer
                  key={step.expression+index}
                  step={step}
                  stepIndex={index}
                  className={isHighlighted(index) ? 'highlighted' : ''}
                  color={coloring.get(index)}
                  onMouseEnter={() => handleMouseEnter(step.rule)}
                  onMouseLeave={handleMouseLeave}
                />
              );
            })}
          </tbody>
        </table>
      </div>
      <hr className="proof-divider" />
      <Goal
        expression={proof.goal}
        success={proof && proof.steps && proof.steps.length > 0 &&
          proof.steps[proof.steps.length - 1].assmsLevel === 0 &&
          proof.steps[proof.steps.length - 1].expression === proof.goal
        }
      />
    </div>
  );
};

export default ProofViewer;
