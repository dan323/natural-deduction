package com.dan323.proof.generic.proof;

import com.dan323.expresions.base.LogicOperation;

public class ProofStep<T extends LogicOperation> {

    private final int assumptionLevel;
    private final T step;
    private final ProofReason proof;
    private boolean valid;

    public ProofStep(int ass, T statement, ProofReason why) {
        assumptionLevel = ass;
        step = statement;
        proof = why;
        valid = true;
    }

    public boolean isValid() {
        return valid;
    }

    public void disable() {
        valid = false;
    }

    public int getAssumptionLevel() {
        return assumptionLevel;
    }

    public T getStep() {
        return step;
    }

    public ProofReason getProof() {
        return proof;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("   ".repeat(assumptionLevel));
        if (step != null) {
            sb.append(step.toString());
        }
        sb.append("           ");
        sb.append(proof.toString());
        return sb.toString();
    }
}
