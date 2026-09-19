import React, { FC, useState, useEffect, ChangeEventHandler, useMemo } from 'react';
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
    // One entry per input of the action: true for a line number, false for an expression.
    inputKinds: boolean[];
};

const glowingColors: string[] = ['#ffcc00', '#00f2ff', '#ff69b4'];

function parseAction(action: string): ActionParsed {
    const name = action.split('(')[0];
    const inputString = (/\[\s*(.*?)\s*\]/).exec(action)?.[1];
    const inputs = inputString ? inputString.split(',').map(input => input.trim()) : [];

    return { name, inputKinds: inputs.map(input => input === 'int') };
}

const Menu: FC<MenuProps> = ({ logic, onColorChange, setProof, proof }) => {
    const [actions, setActions] = useState<ActionParsed[]>([]);
    const [selectedAction, setSelectedAction] = useState<string>('');
    const [sources, setSources] = useState<number[]>([]);
    const [expression, setExpression] = useState<string>("");
    const [errorMessage, setErrorMessage] = useState<string>('');
    const [isLoading, setIsLoading] = useState(false);

    const selectedActionParsed = useMemo(
        () => actions.find(action => action.name === selectedAction),
        [selectedAction, actions]
    );

    const handleActionChange: ChangeEventHandler<HTMLSelectElement> = (event) => {
        const newAction = event.target.value;
        setSelectedAction(newAction);
        setErrorMessage('');

        glowingColors.forEach((color) => onColorChange(color,-1));

        const selectedActionObj = actions.find(action => action.name === newAction);
        if (selectedActionObj) {
            const intInputs = selectedActionObj.inputKinds.filter(Boolean).length;
            setSources(new Array(intInputs).fill(-1));
        } else {
            setSources([]);
        }
        setExpression('');
    };

    // `index` is the position among all inputs of the selected action; `sources` only holds the int inputs.
    const onInput = (index: number, input: number | string) => {
        setErrorMessage('');
        if (typeof input === 'number') {
            const sourceIndex = selectedActionParsed?.inputKinds.slice(0, index).filter(Boolean).length ?? index;
            setSources(prevSources => {
                const newSources = [...prevSources];
                newSources[sourceIndex] = input;
                return newSources;
            });
        } else {
            setExpression(input);
        }
    };

    // Only the logic matters: refetching on every proof or colour change rescans the backend.
    useEffect(() => {
        fetchActions(
            logic,
            fetchedActions => {
                setActions(fetchedActions.map(parseAction));
                setErrorMessage('');
            },
            message => setErrorMessage(message)
        );
    }, [logic]);

    const processAction = () => {
        if (selectedAction === '') return;
        setErrorMessage('');
        setIsLoading(true);

        const actionDto: ActionDto = {
            name: selectedAction,
            sources: sources,
            extraParameters: { expression }
        };

        applyAction(logic, proof, actionDto, (response: ApplyActionResponse) => {
            setIsLoading(false);
            if (response.success && response.proof) {
                setProof(response.proof);
                glowingColors.forEach((color) => onColorChange(color,-1));
            } else {
                setErrorMessage(response.message || 'Action could not be applied.');
            }
        });
    };

    const noProof = proof.steps.length === 0 && !proof.goal;

    return (
        <div className="menu">
            {noProof ? (
                <p className="empty-state">
                    Click <strong>New Proof</strong> to get started.
                </p>
            ) : (
                <>
                    <label htmlFor="action-select" className="menu-label">Select Inference Rule:</label>
                    <select
                        id="action-select"
                        className="menu-select"
                        value={selectedAction}
                        onChange={handleActionChange}
                        aria-label="Select inference rule"
                    >
                        <option value="">-- Choose a rule --</option>
                        {actions.map((action) => (
                            <option key={action.name} value={action.name}>
                                {action.name}
                            </option>
                        ))}
                    </select>
                    {selectedActionParsed && (
                        selectedActionParsed.inputKinds.length > 0 ? (
                            <div className="input-container">
                                {selectedActionParsed.inputKinds.map((isInt, index) => (
                                    <GlowingInput
                                        key={`${selectedAction}-${index}`}
                                        index={index}
                                        label={isInt ? 'Line number:' : 'Expression:'}
                                        glowColor={glowingColors[index]}
                                        onColorChange={onColorChange}
                                        onInput={onInput}
                                        shouldGlow={isInt}
                                    />
                                ))}
                            </div>
                        ) : <p className="no-inputs">No additional inputs needed</p>
                    )}
                    {errorMessage && (
                        <p className="menu-error" role="alert" aria-live="assertive">
                            {errorMessage}
                        </p>
                    )}
                    <button
                        className="menu-button"
                        onClick={processAction}
                        disabled={selectedAction === '' || isLoading}
                        aria-disabled={selectedAction === '' || isLoading}
                    >
                        {isLoading ? 'Applying…' : 'Apply Rule'}
                    </button>
                </>
            )}
        </div>
    );
};

export default Menu;
