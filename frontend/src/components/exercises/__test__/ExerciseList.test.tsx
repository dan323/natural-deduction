import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ExerciseList, { ExercisesState } from '../ExerciseList';
import { Exercise } from '../../../types';

const EXERCISES: Exercise[] = [
  { id: 'mp', title: 'Modus ponens', premises: ['p', 'p -> q'], goal: 'q', difficulty: 'EASY' },
  { id: 'lem', title: 'Excluded middle', premises: [], goal: 'p | (- p)', difficulty: 'HARD' },
];

type Props = Parameters<typeof ExerciseList>[0];

const renderList = (state: ExercisesState, overrides: Partial<Props> = {}) => {
  const props: Props = {
    logic: 'classical',
    state,
    solved: new Set<string>(),
    currentId: null,
    startingId: null,
    startError: '',
    onStart: jest.fn(),
    onClose: jest.fn(),
    ...overrides,
  };
  const { rerender } = render(<ExerciseList {...props} />);
  return { props, rerender: (changes: Partial<Props>) => rerender(<ExerciseList {...props} {...changes} />) };
};

describe('ExerciseList', () => {
  test('says which logic the exercises are for', () => {
    const { rerender } = renderList({ kind: 'loaded', exercises: EXERCISES });
    expect(screen.getByText(/In Classical logic\./)).toBeInTheDocument();

    rerender({ logic: 'intuitionistic' });
    expect(screen.getByText(/In Intuitionistic logic\./)).toBeInTheDocument();
  });

  test('groups the exercises by difficulty, leaving out the empty groups', () => {
    renderList({ kind: 'loaded', exercises: EXERCISES });

    expect(screen.getAllByRole('heading', { level: 3 }).map((heading) => heading.textContent)).toEqual(['Easy', 'Hard']);
    expect(within(screen.getByRole('region', { name: 'Easy' })).getByText('p, p -> q ⊢ q')).toBeInTheDocument();
    expect(within(screen.getByRole('region', { name: 'Hard' })).getByText('⊢ p | (- p)')).toBeInTheDocument();
  });

  test('says in text which exercises are solved and which one is on screen', () => {
    renderList({ kind: 'loaded', exercises: EXERCISES }, { solved: new Set(['lem']), currentId: 'mp' });

    const easy = within(screen.getByRole('region', { name: 'Easy' }));
    const hard = within(screen.getByRole('region', { name: 'Hard' }));
    expect(hard.getByText('(Solved)')).toBeInTheDocument();
    expect(easy.queryByText('(Solved)')).not.toBeInTheDocument();
    expect(easy.getByText('(Current)')).toBeInTheDocument();
    expect(screen.getByText('Solved 1 of 2.')).toBeInTheDocument();
  });

  test('Start hands the exercise over', async () => {
    const { props } = renderList({ kind: 'loaded', exercises: EXERCISES });

    await userEvent.setup().click(screen.getByRole('button', { name: 'Start exercise Excluded middle' }));

    expect(props.onStart).toHaveBeenCalledWith(EXERCISES[1]);
  });

  test('while one exercise is being started, no Start can be clicked', () => {
    renderList({ kind: 'loaded', exercises: EXERCISES }, { startingId: 'mp' });

    expect(screen.getByRole('button', { name: 'Start exercise Modus ponens' })).toHaveTextContent('Starting…');
    expect(screen.getByRole('button', { name: 'Start exercise Excluded middle' })).toBeDisabled();
  });

  test('shows the loading state, a failed fetch, an empty catalog and why an exercise could not be started', () => {
    const { rerender } = renderList({ kind: 'loading' });
    expect(screen.getByRole('status')).toHaveTextContent('Loading the exercises…');

    rerender({ state: { kind: 'error', message: 'Network down' } });
    expect(screen.getByRole('alert')).toHaveTextContent('The exercises could not be loaded: Network down');

    rerender({ state: { kind: 'loaded', exercises: [] }, startError: 'Refused' });
    expect(screen.getByRole('alert')).toHaveTextContent('Refused');
    expect(screen.getByText('There are no exercises for this logic yet.')).toBeInTheDocument();
  });

  test('Close closes the list', async () => {
    const { props } = renderList({ kind: 'loaded', exercises: EXERCISES });

    await userEvent.setup().click(screen.getByRole('button', { name: 'Close exercises' }));

    expect(props.onClose).toHaveBeenCalled();
  });
});
