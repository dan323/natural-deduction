import { ProofDto, StepDto } from "../types";

// The proof on screen is kept in `sessionStorage` under this key, so that a reload of the page (in the same tab) finds
// it again. Only its steps and goal are kept: whether it is done is the backend's verdict, asked for again on restore.
export const SAVED_PROOF_KEY = 'natural-deduction.proof';

// What `readSavedProof` found: nothing, a proof of the right shape, or something that cannot be a proof.
export type SavedProof = { kind: 'none' } | { kind: 'proof', proof: ProofDto } | { kind: 'corrupt' };

function isStep(value: unknown): value is StepDto {
    if (typeof value !== 'object' || value === null) return false;
    const step = value as Record<string, unknown>;
    return typeof step.expression === 'string'
        && typeof step.rule === 'string'
        && typeof step.assmsLevel === 'number'
        && typeof step.extraParameters === 'object' && step.extraParameters !== null
        && Object.values(step.extraParameters).every((parameter) => typeof parameter === 'string');
}

// Reads the saved proof of the given logic. Storage that cannot be used (blocked, private mode) counts as nothing saved;
// text that is not JSON, or JSON that is not a proof of that logic, is corrupt.
export function readSavedProof(logic: string): SavedProof {
    let text: string | null;
    try {
        text = window.sessionStorage.getItem(SAVED_PROOF_KEY);
    } catch {
        return { kind: 'none' };
    }
    if (text === null) return { kind: 'none' };
    let value: unknown;
    try {
        value = JSON.parse(text);
    } catch {
        return { kind: 'corrupt' };
    }
    if (typeof value !== 'object' || value === null) return { kind: 'corrupt' };
    const saved = value as Record<string, unknown>;
    if (saved.logic !== logic || typeof saved.goal !== 'string' || saved.goal === ''
        || !Array.isArray(saved.steps) || !saved.steps.every(isStep)) {
        return { kind: 'corrupt' };
    }
    return { kind: 'proof', proof: { steps: saved.steps, logic, goal: saved.goal } };
}

// Saves the proof, leaving out its `done` verdict. Storage that cannot be used (or is full) is ignored.
export function writeSavedProof(proof: ProofDto): void {
    try {
        window.sessionStorage.setItem(SAVED_PROOF_KEY, JSON.stringify({ steps: proof.steps, logic: proof.logic, goal: proof.goal }));
    } catch {
        // Nothing to do: the proof just does not survive a reload.
    }
}

// Forgets the saved proof. Storage that cannot be used is ignored.
export function clearSavedProof(): void {
    try {
        window.sessionStorage.removeItem(SAVED_PROOF_KEY);
    } catch {
        // Nothing to do.
    }
}
