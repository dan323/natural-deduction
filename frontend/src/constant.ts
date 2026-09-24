// A logic the UI offers: `id` is the `{logic}` of the backend's `/logic/{logic}/...` URLs (and `ProofDto.logic`), `name`
// is what the UI calls it. `hasSolver` is false for a logic whose `POST .../solve` always answers 400 ("no solver for
// this logic"), so that the UI does not offer Solve for it.
export type LogicInfo = {
    id: string;
    name: string;
    description: string;
    hasSolver: boolean;
};

// The logics the UI offers, in the order of its selectors. The backend also serves "modal", which the UI cannot run yet
// (PR 10, #147-#149): it is added here once it can.
export const LOGICS: readonly LogicInfo[] = [
    {
        id: 'classical',
        name: 'Classical',
        description: 'Every rule, including double negation elimination (¬E).',
        hasSolver: true,
    },
    {
        id: 'intuitionistic',
        name: 'Intuitionistic',
        description: 'Classical logic without double negation elimination (¬E), so p does not follow from ¬¬p. There is no solver.',
        hasSolver: false,
    },
];

// The logic of a new page, before the user picks another one.
export const DEFAULT_LOGIC: string = LOGICS[0].id;

export function logicInfo(logic: string): LogicInfo | undefined {
    return LOGICS.find((info) => info.id === logic);
}

// Whether the UI offers the logic.
export function isSupportedLogic(logic: unknown): logic is string {
    return typeof logic === 'string' && logicInfo(logic) !== undefined;
}

// "Intuitionistic logic"; a logic the UI does not list is named by its id.
export function logicName(logic: string): string {
    return `${logicInfo(logic)?.name ?? logic} logic`;
}

// Whether Solve is offered. A logic the UI does not list is left to the backend, which says so if it has no solver.
export function hasSolver(logic: string): boolean {
    return logicInfo(logic)?.hasSolver ?? true;
}

// The logics whose formulas hold in states (Kripke worlds): every step of a proof in one of them has the state it holds
// in, in `extraParameters.state`, except a relation between states (`s0 <= s1`, `s0 = s1`), which holds in none.
const LOGICS_WITH_STATES: readonly string[] = ['modal'];

// The state a proof starts in: the backend's `ModalNaturalDeduction` puts its premises there, and rejects a premise
// (other than a relation) that says it is in any other state.
export const INITIAL_STATE = 's0';

// Whether the steps of the logic's proofs are in states, so that the premises need `INITIAL_STATE` and the proof table
// shows each step's state.
export function hasStates(logic: string): boolean {
    return LOGICS_WITH_STATES.includes(logic);
}
