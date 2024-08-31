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

  // Updated parseRangeFromRule to handle the general case with ranges and individual numbers
  const parseRangeFromRule = (rule: string): RangeDto[] | null => {
    // Match all sequences inside the brackets
    let match = rule.match(/\[(\d+(-\d+)?(, \d+(-\d+)?)*?)\]/);
    if (match) {
      const parts = match[1].split(',');
      let ranges: RangeDto[] = [];

      parts.forEach(part => {
        // Check if the part is a range (e.g., "1-2")
        const rangeMatch = part.match(/(\d+)-(\d+)/);
        if (rangeMatch) {
          ranges.push({ start: parseInt(rangeMatch[1]), end: parseInt(rangeMatch[2]) });
        } else {
          // If it's not a range, treat it as a single number
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
    <div>
      <hr
        style={{
          background: 'black',
          color: 'black',
          borderColor: 'black',
          height: '3px',
        }}
      />
      <div className="proof-viewer">
        <table>
          <thead>
            <tr>
              <th>Line</th>
              <th>Step</th>
              <th>Rule</th>
            </tr>
          </thead>
          <tbody>
            {proof.steps.map((step, index) => {
              return (
                <StepViewer
                  key={index}
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
      <hr
        style={{
          background: 'black',
          color: 'black',
          borderColor: 'black',
          height: '3px',
        }}
      />
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
