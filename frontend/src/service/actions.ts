import { ProofDto, ActionDto, ApplyActionResponse } from "../types";
import { BASE_URL } from "../constant";


export async function fetchActions(logic: string, consumer: (actions: string[]) => void): Promise<void> {
    return fetch(`${BASE_URL}logic/${logic}/actions`)
        .then(response => response.text())
        .then(data => consumer(JSON.parse(data)))
        .catch(err => console.error("Error fetching actions:", err));
}

export async function applyAction(logic: string, proof: ProofDto, action: ActionDto, consumer: (actions: ApplyActionResponse) => void): Promise<void> {
    return fetch(`${BASE_URL}logic/${logic}/action`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ actionDto: action, proofDto: proof }),
    })
        .then(response => response.text())
        .then(data => consumer(JSON.parse(data)))
        .catch(err => console.error("Error applying an action:", err));
}