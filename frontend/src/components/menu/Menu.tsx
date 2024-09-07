import React, { FC, useState, useEffect, ChangeEventHandler, useMemo, useCallback } from 'react';
import '../Expressions.css';
import { fetchActions, applyAction } from '../../service/actions';
import './Menu.css';
import GlowingInput from '../input/GlowingInput';
import { ProofDto, ActionDto, ApplyActionResponse } from '../../types';

type MenuProps = {
    logic: string;
    onColorChange: (color: string, line: number) => void;
    setProof: (proof: ProofDto) => void;
    proof: ProofDto;
};

type ActionParsed = {
    name: string;
    inputs: JSX.Element | null;
    intInputs: number;
};

const glowingColors: string[] = ['#ffcc00', '#00f2ff', '#ff69b4'];

function parseAction(
    action: string,
    onColorChange: (color: string, line: number) => void,
    onInput: (index: number, input: number | string) => void
): ActionParsed {
    const name = action.split('(')[0];
    const inputString = (/\[\s*(.*?)\s*\]/).exec(action)?.[1];
    const inputs = inputString ? inputString.split(',').map(input => input.trim()) : [];

    return {
        name,
        intInputs: inputs.filter(input => input === 'int').length,
        inputs: inputs.length > 0 ? (
            <div className="input-container">
                {inputs.map((input, index) => (
                    <GlowingInput
                        key={index+input}
                        index={index}
                        label={input === 'int' ? 'Enter line number:' : 'Enter expression:'}
                        glowColor={glowingColors[index]}
                        onColorChange={onColorChange}
                        onInput={onInput}
                        shouldGlow={input === 'int'}
                    />
                ))}
            </div>
        ) : <p className="no-inputs">No inputs needed</p>
    };
}

const Menu: FC<MenuProps> = ({ logic, onColorChange, setProof, proof }) => {
    const [actions, setActions] = useState<ActionParsed[]>([]);
    const [selectedAction, setSelectedAction] = useState<string>('');
    const [sources, setSources] = useState<number[]>([]);
    const [expression, setExpression] = useState<string>("");

    const selectedInputs = useMemo(() => {
        return actions.find(action => action.name === selectedAction)?.inputs || null;
    }, [selectedAction, actions]);

    const handleActionChange: ChangeEventHandler<HTMLSelectElement> = (event) => {
        const newAction = event.target.value;
        setSelectedAction(newAction);

        glowingColors.forEach((color) => onColorChange(color,-1));

        const selectedActionObj = actions.find(action => action.name === newAction);
        if (selectedActionObj) {
            const initialSources = Array(selectedActionObj.intInputs).fill(-1);
            setSources(initialSources);
        } else {
            setSources([]);
        }
        setExpression('');  // Reset expression
    };

    const onInput = useCallback(
        (index: number, input: number | string) => {
            if (typeof input === 'number') {
                setSources(prevSources => {
                    const newSources = [...prevSources];
                    newSources[index] = input;
                    return newSources;
                });
            } else {
                setExpression(input);
            }
        }, [setSources]);

    useEffect(() => {
        fetchActions(logic, fetchedActions => {
            const parsedActions = fetchedActions.map(action =>
                parseAction(action, onColorChange, onInput)
            );
            setActions(parsedActions);
        });
    }, [logic, onColorChange, onInput]);

    const processAction = () => {
        if (selectedAction === '') return;

        const actionDto: ActionDto = {
            name: selectedAction,
            sources: sources,
            extraParameters: { expression }
        };

        applyAction(logic, proof, actionDto, (response: ApplyActionResponse) => {
            if (response.success) {
                setProof(response.proof);
                glowingColors.forEach((color) => onColorChange(color,-1));
            } else {
                console.error('Action failed:', response.message);
                alert(`Action failed: ${response.message}`);
            }
        });
    };

    return (
        <div className="menu">
            <label htmlFor="action-select" className="menu-label">Select Inference Rule:</label>
            <select id="action-select" className="menu-select" value={selectedAction} onChange={handleActionChange}>
                <option value="">-- Choose an action --</option>
                {actions.map((action, index) => (
                    <option key={action.name} value={action.name}>
                        {action.name}
                    </option>
                ))}
            </select>
            {selectedInputs}
            <button className="menu-button" onClick={processAction} disabled={selectedAction === ''}>
                Perform Action
            </button>
        </div>
    );
};

export default Menu;
