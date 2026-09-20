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

// `index` is the position among all inputs of an action; `sources` only holds the INT inputs.
const sourceIndexOf = (params: ParamKind[], index: number) => params.slice(0, index).filter(kind => kind === 'INT').length;

const Menu: FC<MenuProps> = ({ logic, onColorChange, setProof, proof }) => {
    const [actions, setActions] = useState<ActionDescriptor[]>([]);
    const [selectedAction, setSelectedAction] = useState<string>('');
    // One entry per INT input: the line typed in it, or null while it is empty or not a whole number.
    const [sources, setSources] = useState<Array<number | null>>([]);
    // Bumped to empty the inputs while keeping the selected rule.
    const [inputsKey, setInputsKey] = useState(0);
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
        setSources(new Array(descriptor?.params.filter(kind => kind === 'INT').length ?? 0).fill(null));
        setExpression('');
        setState('');
    };

    const onInput = (index: number, input: number | string | null) => {
        setErrorMessage('');
        setNotice('');
        const params = selectedDescriptor?.params ?? [];
        if (typeof input === 'number' || input === null) {
            const sourceIndex = sourceIndexOf(params, index);
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

    const lineCount = proof.steps.length;
    const params = selectedDescriptor?.params ?? [];

    // The problem with the line typed in the INT input at `sourceIndex`, if any. A missing line is not reported here:
    // the input is just incomplete, and the hint next to the button says so.
    const lineError = (sourceIndex: number): string | undefined => {
        const line = sources[sourceIndex];
        if (line === null || line === undefined || (line >= 1 && line <= lineCount)) return undefined;
        return lineCount === 0 ? 'The proof has no lines yet.' : `The proof has lines 1 to ${lineCount}.`;
    };

    const inputsComplete = params.every((kind, index) => {
        if (kind === 'INT') {
            const sourceIndex = sourceIndexOf(params, index);
            return sources[sourceIndex] != null && lineError(sourceIndex) === undefined;
        }
        return (kind === 'STATE' ? state : expression).trim() !== '';
    });
    const canApply = selectedDescriptor !== undefined && inputsComplete;

    const processAction = () => {
        if (!canApply) return;
        setErrorMessage('');
        setNotice('');
        setIsLoading(true);

        const actionDto: ActionDto = {
            name: selectedAction,
            sources: sources.filter((line): line is number => line !== null),
            extraParameters: selectedDescriptor?.params.includes('STATE') ? { expression, state } : { expression }
        };

        applyAction(logic, proof, actionDto, (response: ApplyActionResponse) => {
            setIsLoading(false);
            if (response.success && response.proof) {
                setProof(response.proof);
                glowingColors.forEach((color) => onColorChange(color,-1));
                // Pressing Apply again would add the same step twice, so the inputs start over; the rule stays.
                setSources(new Array(sources.length).fill(null));
                setExpression('');
                setState('');
                setInputsKey(key => key + 1);
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

    // Without a proof there is nothing to apply a rule to; App shows the hint to start one.
    if (noProof) return null;

    const applyHint = selectedAction === ''
        ? 'Choose a rule to apply.'
        : 'Fill in every input with a valid value to apply the rule.';

    return (
        <div className="menu">
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
                params.length > 0 ? (
                    <div className="input-container">
                        {params.map((kind, index) => (
                            <GlowingInput
                                key={`${selectedAction}-${index}-${inputsKey}`}
                                index={index}
                                label={inputLabels[kind]}
                                glowColor={glowingColors[index]}
                                onColorChange={onColorChange}
                                onInput={onInput}
                                shouldGlow={kind === 'INT'}
                                error={kind === 'INT' ? lineError(sourceIndexOf(params, index)) : undefined}
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
                    disabled={!canApply || busy}
                    aria-disabled={!canApply || busy}
                    aria-describedby={canApply ? undefined : 'apply-hint'}
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
            {!canApply && <p id="apply-hint" className="menu-hint">{applyHint}</p>}
        </div>
    );
};

export default Menu;
