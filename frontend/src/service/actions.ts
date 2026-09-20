import { ProofDto, ActionDto, ActionDescriptor, ApplyActionResponse } from "../types";

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
