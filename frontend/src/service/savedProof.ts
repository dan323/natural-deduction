import { ProofDto, StepDto } from "../types";
import { isSupportedLogic } from "../constant";

// The proof on screen is kept in `sessionStorage` under this key, so that a reload of the page (in the same tab) finds
// it again. Only its steps, logic and goal are kept: whether it is done is the backend's verdict, asked for again on restore.
// When the proof was started from an exercise, the exercise's id is kept with it, so that the restored proof still counts
// for that exercise.
export const SAVED_PROOF_KEY = 'natural-deduction.proof';

// What `readSavedProof` found: nothing, a proof of the right shape, or something that cannot be a proof.
export type SavedProof = { kind: 'none' } | { kind: 'proof', proof: ProofDto, exerciseId: string | null } | { kind: 'corrupt' };

function isStep(value: unknown): value is StepDto {
    if (typeof value !== 'object' || value === null) return false;
    const step = value as Record<string, unknown>;
    return typeof step.expression === 'string'
        && typeof step.rule === 'string'
        && typeof step.assmsLevel === 'number'
        && typeof step.extraParameters === 'object' && step.extraParameters !== null
        && Object.values(step.extraParameters).every((parameter) => typeof parameter === 'string');
}

// Reads the saved proof, which keeps its own logic. Storage that cannot be used (blocked, private mode) counts as nothing
// saved; text that is not JSON, or JSON that is not a proof of a logic the UI offers, is corrupt.
export function readSavedProof(): SavedProof {
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
    const logic = saved.logic;
    if (!isSupportedLogic(logic) || typeof saved.goal !== 'string' || saved.goal === ''
        || !Array.isArray(saved.steps) || !saved.steps.every(isStep)) {
        return { kind: 'corrupt' };
    }
    // A missing or malformed exercise id only means the proof is not tied to an exercise; the proof itself is fine.
    const exerciseId = typeof saved.exerciseId === 'string' && saved.exerciseId !== '' ? saved.exerciseId : null;
    return { kind: 'proof', proof: { steps: saved.steps, logic, goal: saved.goal }, exerciseId };
}

// Saves the proof, leaving out its `done` verdict, with the id of the exercise it was started from, if any. Storage that
// cannot be used (or is full) is ignored.
export function writeSavedProof(proof: ProofDto, exerciseId: string | null = null): void {
    const saved = { steps: proof.steps, logic: proof.logic, goal: proof.goal, ...(exerciseId === null ? {} : { exerciseId }) };
    try {
        window.sessionStorage.setItem(SAVED_PROOF_KEY, JSON.stringify(saved));
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
