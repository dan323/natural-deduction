import { FC, useState, useEffect, ChangeEventHandler, useMemo } from 'react';
import '../Expressions.css';
import { fetchActions, applyAction, solveProof } from '../../service/actions';
import './Menu.css';
import GlowingInput from '../input/GlowingInput';
import { ProofDto, ActionDto, ActionDescriptor, ApplyActionResponse, ParamKind } from '../../types';

type MenuProps = {
    logic: string;
    onColorChange: (color: string, line: number) => void;
    setProof: (proof: ProofDto) => void;
    proof: ProofDto;
};

const glowingColors: string[] = ['#ffcc00', '#00f2ff', '#ff69b4'];

const inputLabels: Record<ParamKind, string> = {
    INT: 'Line number:',
    EXPRESSION: 'Expression:',
    STATE: 'State:',
};

const Menu: FC<MenuProps> = ({ logic, onColorChange, setProof, proof }) => {
    const [actions, setActions] = useState<ActionDescriptor[]>([]);
    const [selectedAction, setSelectedAction] = useState<string>('');
    const [sources, setSources] = useState<number[]>([]);
    const [expression, setExpression] = useState<string>("");
    const [state, setState] = useState<string>("");
    const [errorMessage, setErrorMessage] = useState<string>('');
    const [notice, setNotice] = useState<string>('');
    const [isLoading, setIsLoading] = useState(false);
    const [isSolving, setIsSolving] = useState(false);

    const selectedDescriptor = useMemo(
        () => actions.find(action => action.name === selectedAction),
        [selectedAction, actions]
    );

    const handleActionChange: ChangeEventHandler<HTMLSelectElement> = (event) => {
        const newAction = event.target.value;
        setSelectedAction(newAction);
        setErrorMessage('');
        setNotice('');

        glowingColors.forEach((color) => onColorChange(color,-1));

        const descriptor = actions.find(action => action.name === newAction);
        setSources(new Array(descriptor?.params.filter(kind => kind === 'INT').length ?? 0).fill(-1));
        setExpression('');
        setState('');
    };

    // `index` is the position among all inputs of the selected action; `sources` only holds the INT inputs.
    const onInput = (index: number, input: number | string) => {
        setErrorMessage('');
        setNotice('');
        const params = selectedDescriptor?.params ?? [];
        if (typeof input === 'number') {
            const sourceIndex = params.slice(0, index).filter(kind => kind === 'INT').length;
            setSources(prevSources => {
                const newSources = [...prevSources];
                newSources[sourceIndex] = input;
                return newSources;
            });
        } else if (params[index] === 'STATE') {
            setState(input);
        } else {
            setExpression(input);
        }
    };

    // Only the logic matters: refetching on every proof or colour change rescans the backend.
    useEffect(() => {
        fetchActions(
            logic,
            fetchedActions => {
                setActions(fetchedActions);
                setErrorMessage('');
            },
            message => setErrorMessage(message)
        );
    }, [logic]);

    const processAction = () => {
        if (selectedAction === '') return;
        setErrorMessage('');
        setNotice('');
        setIsLoading(true);

        const actionDto: ActionDto = {
            name: selectedAction,
            sources: sources,
            extraParameters: selectedDescriptor?.params.includes('STATE') ? { expression, state } : { expression }
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

    const solve = () => {
        setErrorMessage('');
        setNotice('');
        setIsSolving(true);

        solveProof(logic, proof, (response: ApplyActionResponse) => {
            setIsSolving(false);
            if (response.success && response.proof) {
                setProof(response.proof);
                glowingColors.forEach((color) => onColorChange(color, -1));
                setNotice(response.proof.done
                    ? 'The proof is complete.'
                    : 'The solver could not finish the proof. Continue from where it stopped.');
            } else {
                setErrorMessage(response.message || 'The proof could not be solved.');
            }
        });
    };

    const busy = isLoading || isSolving;
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
                    {selectedDescriptor && (
                        selectedDescriptor.params.length > 0 ? (
                            <div className="input-container">
                                {selectedDescriptor.params.map((kind, index) => (
                                    <GlowingInput
                                        key={`${selectedAction}-${index}`}
                                        index={index}
                                        label={inputLabels[kind]}
                                        glowColor={glowingColors[index]}
                                        onColorChange={onColorChange}
                                        onInput={onInput}
                                        shouldGlow={kind === 'INT'}
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
                    {notice && (
                        <output className="menu-notice">
                            {notice}
                        </output>
                    )}
                    <div className="menu-buttons">
                        <button
                            className="menu-button"
                            onClick={processAction}
                            disabled={selectedAction === '' || busy}
                            aria-disabled={selectedAction === '' || busy}
                        >
                            {isLoading ? 'Applying…' : 'Apply Rule'}
                        </button>
                        <button
                            className="menu-button menu-button-secondary"
                            onClick={solve}
                            disabled={busy}
                            aria-disabled={busy}
                            title="Let the solver try to finish the whole proof"
                        >
                            {isSolving ? 'Solving…' : 'Solve'}
                        </button>
                    </div>
                </>
            )}
        </div>
    );
};

export default Menu;
