package com.dan323.proof.generic;

import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStepSupplier;

import java.util.ArrayList;
import java.util.List;

/**
 * @author danco
 */
public abstract class Copy implements AbstractAction {

    private final int source;

    public Copy(int i) {
        source = i;
    }

    @Override
    public boolean isValid(Proof pf) {
        return RuleUtils.isValidIndexAndProp(pf, source);
    }

    @Override
    public void applyStepSupplier(Proof pf, ProofStepSupplier supp) {
        int assLevel = 0;
        if (!pf.getSteps().isEmpty()) {
            assLevel = Action.getLastAssumptionLevel(pf);
        }
        List<Integer> lst = new ArrayList<>();
        lst.add(source);
        pf.getSteps().add(supp.generateProofStep(assLevel, pf.getSteps().get(source - 1).getStep(), new ProofReason("Rep", lst)));
    }

    public int getAppliedAt() {
        return source;
    }
}