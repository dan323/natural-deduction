import { FC } from 'react';
import './ExerciseList.css';
import { Difficulty, Exercise } from '../../types';

// What the list shows: the exercises while they are being fetched, once they are in, or why they could not be fetched.
export type ExercisesState = { kind: 'loading' } | { kind: 'loaded', exercises: Exercise[] } | { kind: 'error', message: string };

type ExerciseListProps = {
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

// The exercises of the logic, grouped by difficulty. A solved exercise says so in text ("Solved"), not by colour alone.
const ExerciseList: FC<ExerciseListProps> = ({ state, solved, currentId, startingId, startError, onStart, onClose }) => {
    const renderBody = () => {
        if (state.kind === 'loading') return <p role="status">Loading the exercises…</p>;
        if (state.kind === 'error') {
            return <p className="exercises-error" role="alert">The exercises could not be loaded: {state.message}</p>;
        }
        if (state.exercises.length === 0) return <p>There are no exercises for this logic yet.</p>;
        const solvedCount = state.exercises.filter((exercise) => solved.has(exercise.id)).length;
        return (
            <>
                <p className="exercises-progress">Solved {solvedCount} of {state.exercises.length}.</p>
                {difficulties.map((difficulty) => {
                    const group = state.exercises.filter((exercise) => exercise.difficulty === difficulty);
                    if (group.length === 0) return null;
                    const headingId = `exercises-${difficulty.toLowerCase()}`;
                    return (
                        <section key={difficulty} aria-labelledby={headingId}>
                            <h3 id={headingId} className="exercises-group-title">{difficultyTitles[difficulty]}</h3>
                            <ul className="exercises-group">
                                {group.map((exercise) => (
                                    <li key={exercise.id} className="exercise-item">
                                        <div className="exercise-text">
                                            <span className="exercise-title">{exercise.title}</span>
                                            {solved.has(exercise.id) && <span className="exercise-solved">(Solved)</span>}
                                            {exercise.id === currentId && <span className="exercise-current">(Current)</span>}
                                            <code className="exercise-statement">
                                                {exercise.premises.length > 0 ? `${exercise.premises.join(', ')} ` : ''}⊢ {exercise.goal}
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
            {startError && <p className="exercises-error" role="alert">{startError}</p>}
            {renderBody()}
        </section>
    );
};

export default ExerciseList;
