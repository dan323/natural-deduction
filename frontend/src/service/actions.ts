import { ProofDto, StepDto, ActionDto, ActionDescriptor, ApplyActionResponse } from "../types";
import { parseProofText } from "./utils";

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

// The list of actions of a logic never changes while the server runs, so every consumer shares one request per logic
// (the Menu is remounted for each new proof, and mounted twice in development). A failed request is dropped, so that
// the next call tries again.
const actionsCache = new Map<string, Promise<ActionDescriptor[]>>();

async function requestActions(logic: string): Promise<ActionDescriptor[]> {
    const response = await fetch(`/logic/${logic}/actions`);
    if (!response.ok) {
        throw new Error(await errorMessage(response));
    }
    return response.json();
}

function loadActions(logic: string): Promise<ActionDescriptor[]> {
    let pending = actionsCache.get(logic);
    if (pending === undefined) {
        const request: Promise<ActionDescriptor[]> = requestActions(logic).catch((err) => {
            if (actionsCache.get(logic) === request) {
                actionsCache.delete(logic);
            }
            throw err;
        });
        actionsCache.set(logic, request);
        pending = request;
    }
    return pending;
}

// Forgets the cached lists of actions. Meant for tests.
export function clearActionsCache(): void {
    actionsCache.clear();
}

export async function fetchActions(
    logic: string,
    consumer: (actions: ActionDescriptor[]) => void,
    onError?: (message: string) => void
): Promise<void> {
    try {
        consumer(await loadActions(logic));
    } catch (err) {
        console.error("Error fetching actions:", err);
        onError?.(err instanceof Error ? err.message : 'Could not load the available rules.');
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
        const response = await fetch(`/logic/${logic}/action`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ actionDto: action, proofDto: proof }),
        });
        // A 202 is a valid request whose action does not apply; its body is a ProofResponse with success=false.
        result = response.ok
            ? withDone(await response.json())
            : { success: false, message: await errorMessage(response) };
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

// Loads a proof from text in the layout `proofToText` writes (the one of the backend's `ProofStep.toString()`), for the
// given goal. The text is split into steps here (see `parseProofText`) and the backend replays them (see `replayProof`),
// which checks every step and gives the `done` verdict for that goal. The loaded proof is the one the backend answers
// with, never the parsed steps: the indentation of the text only marks the premises, the subproof structure comes from
// the rules, so a mis-indented line comes back at the level its rule implies. `POST .../proof`, the endpoint for proof files, is
// not used: it rejects a proof that ends inside an open subproof, and takes the last line as the goal, so an unfinished
// proof would come back as a finished proof of its last line. That is also why the goal is required.
export async function loadProofFromText(logic: string, text: string, goal: string, consumer: (result: ApplyActionResponse) => void): Promise<void> {
    if (goal.trim() === '') {
        consumer({ success: false, message: 'Enter the goal of the proof.' });
        return;
    }
    let steps: StepDto[];
    try {
        steps = parseProofText(text);
    } catch (err) {
        consumer({ success: false, message: (err as Error).message });
        return;
    }
    const proof: ProofDto = { steps, logic, goal: goal.trim() };
    await replayProof(logic, proof, (result) => consumer(result.proof
        ? { success: true, proof: result.proof, done: result.done, message: '' }
        : { success: false, message: result.message }));
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
        const response = await fetch(`/logic/${logic}/solve`, {
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
