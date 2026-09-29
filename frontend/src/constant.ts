// A logic the UI offers: `id` is the `{logic}` of the backend's `/logic/{logic}/...` URLs (and `ProofDto.logic`), `name`
// is what the UI calls it. `hasSolver` is false for a logic whose `POST .../solve` always answers 400 ("no solver for
// this logic"), so that the UI does not offer Solve for it. `solverLimit`, when set, is added to the notice of a Solve
// that ends without a proof: what the solver cannot do yet in that logic.
export type LogicInfo = {
    id: string;
    name: string;
    description: string;
    hasSolver: boolean;
    solverLimit?: string;
};

// The logics the UI offers, in the order of its selectors.
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
        description: 'Classical logic without double negation elimination (¬E), so p does not follow from ¬¬p. Its solver never uses ¬E.',
        hasSolver: true,
    },
    {
        id: 'modal',
        name: 'Modal',
        description: 'Adds □ (necessarily) and ◇ (possibly): every formula holds in a state, and s0 <= s1 says that s1 is reachable from s0, a reflexive and transitive relation.',
        hasSolver: true,
    },
    {
        id: 'modal-next-until',
        name: 'Modal with Next and Until',
        description: 'Modal logic over discrete time: every state s has a next state s+1 (written s0+1, s0+2, ...), X A says that A holds in the next state and A U B that B holds in the current state or a later state and A until then. Its solver does not use induction (Ind).',
        hasSolver: true,
        solverLimit: 'The proof may need induction (Ind), which the solver cannot use yet.',
    },
    {
        id: 'first-order',
        name: 'First-order',
        description: 'Predicates P(x), functions f(x), equations s = t and the quantifiers ∀ (forall x. A) and ∃ (exists x. A). A name that is free in a premise, like e, acts as a constant. There is no solver.',
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

// The notice of a Solve that ends without a proof, with what the logic's solver cannot do yet, if anything.
export function solverFailureNotice(logic: string, notice: string): string {
    const limit = logicInfo(logic)?.solverLimit;
    return limit ? `${notice} ${limit}` : notice;
}

// The logics whose formulas hold in states (Kripke worlds): every step of a proof in one of them has the state it holds
// in, in `extraParameters.state`, except a relation between states (`s0 <= s1`, `s0 = s1`), which holds in none. In
// `modal-next-until` a state may be a successor term, `s0+1` (the state after `s0`), shown and sent as the backend
// writes it.
const LOGICS_WITH_STATES: ReadonlySet<string> = new Set(['modal', 'modal-next-until']);

// The state a proof starts in: the backend's `ModalNaturalDeduction` puts its premises there, and rejects a premise
// (other than a relation) that says it is in any other state.
export const INITIAL_STATE = 's0';

// Whether the steps of the logic's proofs are in states, so that the premises need `INITIAL_STATE` and the proof table
// shows each step's state.
export function hasStates(logic: string): boolean {
    return LOGICS_WITH_STATES.has(logic);
}
