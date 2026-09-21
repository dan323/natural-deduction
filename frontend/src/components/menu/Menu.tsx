import { FC, Ref, useState, useEffect, useImperativeHandle, ChangeEventHandler, useMemo } from 'react';
import '../Expressions.css';
import { fetchActions, applyAction, solveProof } from '../../service/actions';
import './Menu.css';
import GlowingInput from '../input/GlowingInput';
import { ProofDto, ActionDto, ActionDescriptor, ActionCategory, ApplyActionResponse, ParamKind } from '../../types';

type MenuProps = {
    logic: string;
    onColorChange: (color: string, line: number) => void;
    setProof: (proof: ProofDto) => void;
    proof: ProofDto;
    // Gives the parent a way to pick a line of the proof for the rule, as if its number was typed in the next empty input.
    ref?: Ref<MenuHandle>;
    // Offered next to the "Proof complete" message, once there is nothing left to do in this proof.
    onNewProof?: () => void;
};

export type MenuHandle = {
    // `line` is 1-based. Fills the first empty line input of the selected rule; does nothing when there is none.
    selectLine: (line: number) => void;
};

const COMPLETE_MESSAGE = 'Proof complete.';

const glowingColors: string[] = ['#ffcc00', '#00f2ff', '#ff69b4'];

const inputLabels: Record<ParamKind, string> = {
    INT: 'Line number:',
    EXPRESSION: 'Expression:',
    STATE: 'State:',
};

// The order of the groups of the list of rules.
const categoryTitles: Record<ActionCategory, string> = {
    INTRODUCTION: 'Introduction rules',
    ELIMINATION: 'Elimination rules',
    OTHER: 'Other rules',
};
const categories = Object.keys(categoryTitles) as ActionCategory[];

// "Modus ponens (→E)"; falls back to the name of the action when the backend sends no label.
const optionText = (action: ActionDescriptor) => {
    if (!action.label) return action.name;
    return action.symbol ? `${action.label} (${action.symbol})` : action.label;
};

// The text of an input: the label of the backend, or the generic one of its kind.
const inputLabelOf = (descriptor: ActionDescriptor, index: number) =>
    descriptor.paramLabels?.[index] || inputLabels[descriptor.params[index]];

// The reason of a rejected action, followed by what the rule does. Only an action that was well formed comes back
// with its proof (a 202); the other failures are not about the rule.
const rejectionMessage = (response: ApplyActionResponse, descriptor?: ActionDescriptor) => {
    const reason = response.message || 'Action could not be applied.';
    if (!response.proof || !descriptor?.description) return reason;
    return `${reason.replace(/\.$/, '')}. ${descriptor.description}.`;
};

const renderOption = (action: ActionDescriptor) => (
    <option key={action.name} value={action.name}>{optionText(action)}</option>
);

// `index` is the position among all inputs of an action; `sources` only holds the INT inputs.
const sourceIndexOf = (params: ParamKind[], index: number) => params.slice(0, index).filter(kind => kind === 'INT').length;

const Menu: FC<MenuProps> = ({ logic, onColorChange, setProof, proof, ref, onNewProof }) => {
    const [actions, setActions] = useState<ActionDescriptor[]>([]);
    const [selectedAction, setSelectedAction] = useState<string>('');
    // One entry per INT input: the line typed in it, or null while it is empty or not a whole number.
    const [sources, setSources] = useState<Array<number | null>>([]);
    // Bumped to empty the inputs while keeping the selected rule.
    const [inputsKey, setInputsKey] = useState(0);
    // Per INT input, how many times a line was picked for it from the proof. A picked line remounts the input (it
    // owns the text it shows), and this is what tells the new mount apart from the old one.
    const [picks, setPicks] = useState<number[]>([]);
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

    // Rules of a known category go in one group each; the rest (e.g. a logic without categories) stay ungrouped.
    const groupedActions = useMemo(
        () => categories
            .map(category => ({ category, actions: actions.filter(action => action.category === category) }))
            .filter(group => group.actions.length > 0),
        [actions]
    );
    const ungroupedActions = useMemo(
        () => actions.filter(action => !action.category || !categories.includes(action.category)),
        [actions]
    );

    const handleActionChange: ChangeEventHandler<HTMLSelectElement> = (event) => {
        const newAction = event.target.value;
        setSelectedAction(newAction);
        setErrorMessage('');
        setNotice('');

        glowingColors.forEach((color) => onColorChange(color,-1));

        const descriptor = actions.find(action => action.name === newAction);
        setSources(new Array(descriptor?.params.filter(kind => kind === 'INT').length ?? 0).fill(null));
        setPicks([]);
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

    // A finished proof takes no more rules; the message is derived from the proof, so it also covers a proof that a
    // rule completed, not only one that the solver did.
    const done = proof.done === true;
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
    const canApply = !done && selectedDescriptor !== undefined && inputsComplete;

    useImperativeHandle(ref, () => ({
        selectLine: (line: number) => {
            const sourceIndex = sources.findIndex(source => source === null);
            if (done || sourceIndex < 0) return;
            const index = params.findIndex((kind, i) => kind === 'INT' && sourceIndexOf(params, i) === sourceIndex);
            setErrorMessage('');
            setNotice('');
            setSources(prevSources => prevSources.map((source, i) => (i === sourceIndex ? line : source)));
            setPicks(prevPicks => {
                const newPicks = [...prevPicks];
                newPicks[sourceIndex] = (newPicks[sourceIndex] ?? 0) + 1;
                return newPicks;
            });
            onColorChange(glowingColors[index], line - 1);
        },
    }), [sources, params, onColorChange, done]);

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
                setPicks([]);
                setExpression('');
                setState('');
                setInputsKey(key => key + 1);
            } else {
                setErrorMessage(rejectionMessage(response, selectedDescriptor));
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
                    ? ''
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

    const message = done ? COMPLETE_MESSAGE : notice;
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
                disabled={done}
                aria-describedby={selectedDescriptor?.description ? 'action-description' : undefined}
            >
                <option value="">-- Choose a rule --</option>
                {ungroupedActions.map(renderOption)}
                {groupedActions.map(group => (
                    <optgroup key={group.category} label={categoryTitles[group.category]}>
                        {group.actions.map(renderOption)}
                    </optgroup>
                ))}
            </select>
            {selectedDescriptor?.description && (
                <p id="action-description" className="menu-description">{selectedDescriptor.description}</p>
            )}
            {selectedDescriptor && (
                params.length > 0 ? (
                    <div className="input-container">
                        {params.map((kind, index) => (
                            <GlowingInput
                                key={`${selectedAction}-${index}-${inputsKey}-${picks[sourceIndexOf(params, index)] ?? 0}`}
                                index={index}
                                label={inputLabelOf(selectedDescriptor, index)}
                                glowColor={glowingColors[index]}
                                onColorChange={onColorChange}
                                onInput={onInput}
                                initialValue={kind === 'INT' ? (sources[sourceIndexOf(params, index)]?.toString() ?? '') : undefined}
                                shouldGlow={kind === 'INT'}
                                disabled={done}
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
            {message && (
                <output className="menu-notice">{message}</output>
            )}
            {/* Outside of the status, so that only the message is announced. */}
            {done && onNewProof && (
                <button className="menu-button menu-new-proof" onClick={onNewProof}>New Proof</button>
            )}
            <div className="menu-buttons">
                <button
                    className="menu-button"
                    onClick={processAction}
                    disabled={!canApply || busy}
                    aria-disabled={!canApply || busy}
                    aria-describedby={canApply || done ? undefined : 'apply-hint'}
                >
                    {isLoading ? 'Applying…' : 'Apply Rule'}
                </button>
                <button
                    className="menu-button menu-button-secondary"
                    onClick={solve}
                    disabled={busy || done}
                    aria-disabled={busy || done}
                    title="Let the solver try to finish the whole proof"
                >
                    {isSolving ? 'Solving…' : 'Solve'}
                </button>
            </div>
            {!canApply && !done && <p id="apply-hint" className="menu-hint">{applyHint}</p>}
        </div>
    );
};

export default Menu;
