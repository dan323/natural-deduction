import { FC } from 'react';
import './ExerciseList.css';
import { Difficulty, Exercise, Theory } from '../../types';
import { logicName } from '../../constant';

// What the list shows: the exercises while they are being fetched, once they are in, or why they could not be fetched.
// `theories` are the logic's premise sets (`GET /logic/{logic}/theories`), when they could be fetched: an exercise whose
// premises are exactly those of a theory is listed under that theory's own heading.
export type ExercisesState =
    | { kind: 'loading' }
    | { kind: 'loaded', exercises: Exercise[], theories?: Theory[] }
    | { kind: 'error', message: string };

type ExerciseListProps = {
    // The logic the exercises are for: the one of the proof on screen (or picked on the empty page).
    logic: string;
    state: ExercisesState;
    // The ids of the exercises the user has solved, for this logic.
    solved: Set<string>;
    // The exercise the proof on screen was started from, if any.
    currentId: string | null;
    // The exercise being checked by the backend before it is shown, if any.
    startingId: string | null;
    // Why the last exercise could not be started.
    startError: string;
    onStart: (exercise: Exercise) => void;
    onClose: () => void;
};

// The groups of the list, in order, with their headings.
const difficultyTitles: Record<Difficulty, string> = {
    EASY: 'Easy',
    MEDIUM: 'Medium',
    HARD: 'Hard',
};
const difficulties = Object.keys(difficultyTitles) as Difficulty[];

// Whether the exercise starts from exactly the premises of the theory, in the same order.
const isFromTheory = (exercise: Exercise, theory: Theory) =>
    theory.premises.length > 0
    && exercise.premises.length === theory.premises.length
    && exercise.premises.every((premise, index) => premise === theory.premises[index]);

// The exercises of the logic, grouped by difficulty; those that start from a theory (the group axioms) come after the
// others, under the theory's own heading, again by difficulty. A solved exercise says so in text ("Solved"), not by
// colour alone.
const ExerciseList: FC<ExerciseListProps> = ({ logic, state, solved, currentId, startingId, startError, onStart, onClose }) => {
    const renderItems = (group: Exercise[], theory?: Theory) => (
        <ul className="exercises-group">
            {group.map((exercise) => (
                <li key={exercise.id} className="exercise-item">
                    <div className="exercise-text">
                        <span className="exercise-title">{exercise.title}</span>
                        {solved.has(exercise.id) && <span className="exercise-solved">(Solved)</span>}
                        {exercise.id === currentId && <span className="exercise-current">(Current)</span>}
                        <code className="exercise-statement" title={theory ? exercise.premises.join(', ') : undefined}>
                            {theory && `${theory.name} axioms `}
                            {!theory && exercise.premises.length > 0 ? `${exercise.premises.join(', ')} ` : ''}⊢ {exercise.goal}
                        </code>
                    </div>
                    <button
                        className="exercise-start-btn"
                        onClick={() => onStart(exercise)}
                        disabled={startingId !== null}
                        aria-label={`Start exercise ${exercise.title}`}
                    >
                        {startingId === exercise.id ? 'Starting…' : 'Start'}
                    </button>
                </li>
            ))}
        </ul>
    );

    const renderBody = () => {
        if (state.kind === 'loading') return <output className="paragraph" aria-live="polite">Loading the exercises…</output>;
        if (state.kind === 'error') {
            return <p className="exercises-error" role="alert">The exercises could not be loaded: {state.message}</p>;
        }
        if (state.exercises.length === 0) return <p>There are no exercises for this logic yet.</p>;
        const solvedCount = state.exercises.filter((exercise) => solved.has(exercise.id)).length;
        const theories = state.theories ?? [];
        const theoryOf = (exercise: Exercise) => theories.find((theory) => isFromTheory(exercise, theory));
        const plain = state.exercises.filter((exercise) => theoryOf(exercise) === undefined);
        return (
            <>
                <p className="exercises-progress">Solved {solvedCount} of {state.exercises.length}.</p>
                {difficulties.map((difficulty) => {
                    const group = plain.filter((exercise) => exercise.difficulty === difficulty);
                    if (group.length === 0) return null;
                    const headingId = `exercises-${difficulty.toLowerCase()}`;
                    return (
                        <section key={difficulty} aria-labelledby={headingId}>
                            <h3 id={headingId} className="exercises-group-title">{difficultyTitles[difficulty]}</h3>
                            {renderItems(group)}
                        </section>
                    );
                })}
                {theories.map((theory) => {
                    const ofTheory = state.exercises.filter((exercise) => theoryOf(exercise) === theory);
                    if (ofTheory.length === 0) return null;
                    const headingId = `exercises-theory-${theory.id}`;
                    return (
                        <section key={theory.id} aria-labelledby={headingId}>
                            <h3 id={headingId} className="exercises-group-title">{theory.name} theory</h3>
                            {difficulties.map((difficulty) => {
                                const group = ofTheory.filter((exercise) => exercise.difficulty === difficulty);
                                if (group.length === 0) return null;
                                return (
                                    <div key={difficulty}>
                                        <h4 className="exercises-subgroup-title">{difficultyTitles[difficulty]}</h4>
                                        {renderItems(group, theory)}
                                    </div>
                                );
                            })}
                        </section>
                    );
                })}
            </>
        );
    };

    return (
        <section id="exercises-panel" className="exercises" aria-labelledby="exercises-title">
            <div className="exercises-header">
                <h2 id="exercises-title" className="exercises-title" tabIndex={-1}>Exercises</h2>
                <button className="exercises-close-btn" onClick={onClose}>Close exercises</button>
            </div>
            <p className="exercises-logic">In {logicName(logic)}. To see the exercises of another logic, start a new proof in it.</p>
            {startError && <p className="exercises-error" role="alert">{startError}</p>}
            {renderBody()}
        </section>
    );
};

export default ExerciseList;
