import { markExerciseSolved, readSolvedExercises, SOLVED_EXERCISES_KEY } from '../solvedExercises';

describe('service/solvedExercises', () => {
  beforeEach(() => window.localStorage.clear());
  afterEach(() => jest.restoreAllMocks());

  test('nothing is solved at first', () => {
    expect(readSolvedExercises('classical')).toEqual(new Set());
  });

  test('a solved exercise is remembered, per logic', () => {
    expect(markExerciseSolved('classical', 'modus-ponens')).toEqual(new Set(['modus-ponens']));
    markExerciseSolved('classical', 'and-elimination');
    markExerciseSolved('modal', 'modus-ponens');
    markExerciseSolved('classical', 'modus-ponens');

    expect(readSolvedExercises('classical')).toEqual(new Set(['modus-ponens', 'and-elimination']));
    expect(readSolvedExercises('modal')).toEqual(new Set(['modus-ponens']));
    expect(JSON.parse(window.localStorage.getItem(SOLVED_EXERCISES_KEY)!)).toEqual({
      classical: ['modus-ponens', 'and-elimination'],
      modal: ['modus-ponens'],
    });
  });

  test.each([
    ['text that is not JSON', '{'],
    ['JSON that is not an object', '["modus-ponens"]'],
    ['null', 'null'],
  ])('%s counts as nothing solved, and is replaced by the next solved exercise', (_, text) => {
    window.localStorage.setItem(SOLVED_EXERCISES_KEY, text);

    expect(readSolvedExercises('classical')).toEqual(new Set());
    expect(markExerciseSolved('classical', 'modus-ponens')).toEqual(new Set(['modus-ponens']));
    expect(readSolvedExercises('classical')).toEqual(new Set(['modus-ponens']));
  });

  test('entries that are not lists of ids are left out', () => {
    window.localStorage.setItem(SOLVED_EXERCISES_KEY, JSON.stringify({ classical: ['a', 3, null, 'b'], modal: 'a' }));

    expect(readSolvedExercises('classical')).toEqual(new Set(['a', 'b']));
    expect(readSolvedExercises('modal')).toEqual(new Set());
  });

  test('storage that cannot be used counts as nothing solved, and marking still answers the exercise', () => {
    jest.spyOn(Storage.prototype, 'getItem').mockImplementation(() => { throw new Error('blocked'); });
    jest.spyOn(Storage.prototype, 'setItem').mockImplementation(() => { throw new Error('blocked'); });

    expect(readSolvedExercises('classical')).toEqual(new Set());
    expect(markExerciseSolved('classical', 'modus-ponens')).toEqual(new Set(['modus-ponens']));
  });
});
