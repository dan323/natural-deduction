// The exercises the user has solved, kept in `localStorage` (they outlive the tab, unlike the proof on screen) under this
// key, as one list of exercise ids per logic: `{ "classical": ["modus-ponens", ...] }`. Ids are only unique within a
// logic, so every read and write names the logic.
export const SOLVED_EXERCISES_KEY = 'natural-deduction.solved-exercises';

// Everything saved, for every logic. Storage that cannot be used, and text that is not the expected shape, count as
// nothing solved.
function readAll(): Record<string, string[]> {
    let text: string | null;
    try {
        text = window.localStorage.getItem(SOLVED_EXERCISES_KEY);
    } catch {
        return {};
    }
    if (text === null) return {};
    let value: unknown;
    try {
        value = JSON.parse(text);
    } catch {
        return {};
    }
    if (typeof value !== 'object' || value === null || Array.isArray(value)) return {};
    const all: Record<string, string[]> = {};
    for (const [logic, ids] of Object.entries(value)) {
        if (Array.isArray(ids)) all[logic] = ids.filter((id): id is string => typeof id === 'string');
    }
    return all;
}

// The ids of the solved exercises of the logic.
export function readSolvedExercises(logic: string): Set<string> {
    return new Set(readAll()[logic] ?? []);
}

// Records the exercise as solved, and answers the solved exercises of the logic, this one included. Storage that cannot
// be used (or is full) is ignored: the exercise then only counts as solved until the page is reloaded.
export function markExerciseSolved(logic: string, id: string): Set<string> {
    const all = readAll();
    const solved = new Set(all[logic] ?? []);
    if (!solved.has(id)) {
        solved.add(id);
        try {
            window.localStorage.setItem(SOLVED_EXERCISES_KEY, JSON.stringify({ ...all, [logic]: [...solved] }));
        } catch {
            // Nothing to do.
        }
    }
    return solved;
}
