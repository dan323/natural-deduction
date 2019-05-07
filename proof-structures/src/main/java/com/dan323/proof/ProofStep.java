package com.dan323.proof;

import com.dan323.expresions.LogicOperation;

public class ProofStep {

    private final int assumptionLevel;
    private final LogicOperation step;
    private final ProofReason proof;
    private boolean valid;

    public ProofStep(int ass, LogicOperation statement, ProofReason why) {
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

    public LogicOperation getStep() {
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
