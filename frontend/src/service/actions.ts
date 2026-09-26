import { ProofDto, ActionDto, ActionDescriptor, ApplyActionResponse, Exercise } from "../types";

// Extracts the `message` of an error body ({ "message": "..." }), falling back to the HTTP status.
async function errorMessage(response: Response): Promise<string> {
    try {
        const body = await response.json();
        if (typeof body?.message === 'string' && body.message !== '') {
            return body.message;
        }
    } catch {
        // The body was not JSON; use the generic message below.
    }
    return `Request failed with status ${response.status}.`;
}

// The URL of an endpoint of a logic. The logic ids (`LOGICS`) are plain words, which encoding leaves as they are; it
// keeps any other value inside its path segment.
function logicUrl(logic: string, endpoint: string): string {
    return `/logic/${encodeURIComponent(logic)}/${endpoint}`;
}

// A list the server answers for `GET /logic/{logic}/{resource}` never changes while it runs, so every consumer shares
// one request per logic (the Menu is remounted for each new proof, and mounted twice in development). A failed request
// is dropped, so that the next call tries again.
function perLogicCache<T>(resource: string) {
    const cache = new Map<string, Promise<T[]>>();

    const request = async (logic: string): Promise<T[]> => {
        const response = await fetch(logicUrl(logic, resource));
        if (!response.ok) {
            throw new Error(await errorMessage(response));
        }
        const body: unknown = await response.json();
        if (!Array.isArray(body)) {
            throw new TypeError(`The server did not answer with a list of ${resource}.`);
        }
        return body as T[];
    };

    const load = (logic: string): Promise<T[]> => {
        let pending = cache.get(logic);
        if (pending === undefined) {
            const started: Promise<T[]> = request(logic).catch((err) => {
                if (cache.get(logic) === started) {
                    cache.delete(logic);
                }
                throw err;
            });
            cache.set(logic, started);
            pending = started;
        }
        return pending;
    };

    return { load, clear: () => cache.clear() };
}

const actionsCache = perLogicCache<ActionDescriptor>('actions');
const exercisesCache = perLogicCache<Exercise>('exercises');

// Forgets the cached lists of actions. Meant for tests.
export function clearActionsCache(): void {
    actionsCache.clear();
}

// Forgets the cached lists of exercises. Meant for tests.
export function clearExercisesCache(): void {
    exercisesCache.clear();
}

export async function fetchActions(
    logic: string,
    consumer: (actions: ActionDescriptor[]) => void,
    onError?: (message: string) => void
): Promise<void> {
    try {
        consumer(await actionsCache.load(logic));
    } catch (err) {
        console.error("Error fetching actions:", err);
        onError?.(err instanceof Error ? err.message : 'Could not load the available rules.');
    }
}

// The exercises of a logic (`GET /logic/{logic}/exercises`), ordered from easy to hard; a logic without a catalog has
// none. Cached like the actions.
export async function fetchExercises(
    logic: string,
    consumer: (exercises: Exercise[]) => void,
    onError?: (message: string) => void
): Promise<void> {
    try {
        consumer(await exercisesCache.load(logic));
    } catch (err) {
        console.error("Error fetching exercises:", err);
        onError?.(err instanceof Error ? err.message : 'Could not load the exercises.');
    }
}

// `ProofResponse.done` is the backend's verdict on the returned proof; carry it on the proof, so that the UI only has
// to look at the proof.
function withDone(body: ApplyActionResponse): ApplyActionResponse {
    return body.proof && typeof body.done === 'boolean'
        ? { ...body, proof: { ...body.proof, done: body.done } }
        : body;
}

export async function applyAction(logic: string, proof: ProofDto, action: ActionDto, consumer: (actions: ApplyActionResponse) => void): Promise<void> {
    let result: ApplyActionResponse;
    try {
        const response = await fetch(logicUrl(logic, 'action'), {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ actionDto: action, proofDto: proof }),
        });
        // A 202 is a valid request whose action does not apply; its body is a ProofResponse with success=false.
        result = response.ok
            ? withDone(await response.json())
            : { success: false, message: await errorMessage(response), status: response.status };
    } catch (err) {
        console.error("Error applying an action:", err);
        result = { success: false, message: 'Network error. Please try again.' };
    }
    consumer(result);
}

// Undo drops the last step and asks the backend to revalidate what remains. The server is stateless (see CLAUDE.md
// "Statelessness"): every request replays the whole `ProofDto` from scratch, so resending it without its last step
// is safe by construction, and there is no dedicated "resend" endpoint to ask for the result of that replay.
// `POST .../proof` does not fit: it treats a text file's last line as the goal, which is wrong once the proof is no
// longer complete. `POST .../solve` does not fit either: it would keep solving, not just replay what is left.
// `POST .../action` does fit: a deliberately out-of-range `COPY` is always rejected (a 202, `success: false`) before
// it can change anything, but `LogicalApplyAction` still runs the full replay first and always reports the domain's
// own `isDone()` and the replayed proof without the action (see `RestServiceIT#outOfRangeSourceIsRejectedWithAMessage`
// and `#undoDropsTheLastStepAndRevalidatesTheDischarge`). That proof is serialized by the domain, so its assumption
// levels are the ones the rules imply, not the ones sent (see `#rejectedActionAnswersWithTheReplayedLevels`). That is exactly what undo needs: the remaining steps
// revalidated (so a discharge only the removed step caused is gone too, since that is derived from the steps on
// every replay, never stored) and an authoritative `done`.
export async function undoLastStep(logic: string, proof: ProofDto, consumer: (result: ApplyActionResponse) => void): Promise<void> {
    await replayProof(logic, { steps: proof.steps.slice(0, -1), logic: proof.logic, goal: proof.goal }, consumer);
}

// Has the backend replay a proof without changing it, through the out-of-range `COPY` described above. The answer
// carries the proof and its `done` verdict when the proof is valid (with `success: false`, as for any action that does
// not apply), or no proof and the reason when it is not. The replay parses the goal and every step, so it is also how a
// new proof's premises and goal are checked before the proof is shown (see `App.handleNewProofSubmit`).
export async function replayProof(logic: string, proof: ProofDto, consumer: (result: ApplyActionResponse) => void): Promise<void> {
    // One past the end of the proof: always out of range, however many steps it has.
    const noOpAction: ActionDto = { name: 'COPY', sources: [proof.steps.length + 1], extraParameters: {} };
    await applyAction(logic, proof, noOpAction, consumer);
}

// Loads a finished proof from text in the layout `proofToText` writes (the one of the backend's `ProofStep.toString()`),
// through `POST /logic/{logic}/proof`, the endpoint for proof files: the backend reads the leading top-level `Ass` lines
// as the premises and the last line as the goal, replays every step, and rejects (a 400 naming the line) a text it
// cannot read, a step that does not follow, or a proof that does not end at the top level. So only a finished proof
// loads, with its goal and nothing else to type. The loaded proof is the one the backend answers with, never the text:
// the subproof structure comes from the rules. Trailing spaces and blank lines at the end of a paste are dropped first,
// since the backend reads a blank line as an error.
export async function loadProofFromText(logic: string, text: string, consumer: (result: ApplyActionResponse) => void): Promise<void> {
    const cleaned = text.split(/\r?\n/).map((line) => line.trimEnd()).join('\n').trimEnd();
    if (cleaned === '') {
        consumer({ success: false, message: 'The proof is empty.' });
        return;
    }
    let result: ApplyActionResponse;
    try {
        const body = new FormData();
        body.append('file', new Blob([cleaned], { type: 'text/plain' }), 'proof.txt');
        const response = await fetch(logicUrl(logic, 'proof'), { method: 'POST', body });
        if (response.ok) {
            const proof: ProofDto = await response.json();
            result = { success: true, proof, done: proof.done, message: '' };
        } else {
            result = { success: false, message: await errorMessage(response), status: response.status };
        }
    } catch (err) {
        console.error("Error loading a proof from text:", err);
        result = { success: false, message: 'Network error. Please try again.' };
    }
    consumer(result);
}

// The server stops a solve after its own limit (10 seconds by default). This is only a safety net, so that a request
// that never answers cannot leave the UI waiting for ever.
const SOLVE_DEADLINE_MS = 60_000;

// Asks the backend's automatic solver to finish the proof. It answers with the proof as far as it got, which may
// still be incomplete; an error body (for example when the solver runs out of time) becomes success=false.
export async function solveProof(logic: string, proof: ProofDto, consumer: (response: ApplyActionResponse) => void): Promise<void> {
    let result: ApplyActionResponse;
    const controller = new AbortController();
    const deadline = setTimeout(() => controller.abort(), SOLVE_DEADLINE_MS);
    try {
        const response = await fetch(logicUrl(logic, 'solve'), {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(proof),
            signal: controller.signal,
        });
        result = response.ok
            ? { success: true, proof: await response.json(), message: '' }
            : { success: false, message: await errorMessage(response) };
    } catch (err) {
        console.error("Error solving the proof:", err);
        result = {
            success: false,
            message: controller.signal.aborted ? 'The solver took too long to answer. Please try again.' : 'Network error. Please try again.',
        };
    } finally {
        clearTimeout(deadline);
    }
    consumer(result);
}
