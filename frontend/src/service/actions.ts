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

export async function fetchActions(
    logic: string,
    consumer: (actions: ActionDescriptor[]) => void,
    onError?: (message: string) => void
): Promise<void> {
    try {
        const response = await fetch(`/logic/${logic}/actions`);
        if (!response.ok) {
            throw new Error(await errorMessage(response));
        }
        consumer(await response.json());
    } catch (err) {
        console.error("Error fetching actions:", err);
        onError?.(err instanceof Error ? err.message : 'Could not load the available rules.');
    }
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
            ? await response.json()
            : { success: false, message: await errorMessage(response) };
    } catch (err) {
        console.error("Error applying an action:", err);
        result = { success: false, message: 'Network error. Please try again.' };
    }
    consumer(result);
}

// Asks the backend's automatic solver to finish the proof. It answers with the proof as far as it got, which may
// still be incomplete; an error body (for example when the solver runs out of time) becomes success=false.
export async function solveProof(logic: string, proof: ProofDto, consumer: (response: ApplyActionResponse) => void): Promise<void> {
    let result: ApplyActionResponse;
    try {
        const response = await fetch(`/logic/${logic}/solve`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(proof),
        });
        result = response.ok
            ? { success: true, proof: await response.json(), message: '' }
            : { success: false, message: await errorMessage(response) };
    } catch (err) {
        console.error("Error solving the proof:", err);
        result = { success: false, message: 'Network error. Please try again.' };
    }
    consumer(result);
}
