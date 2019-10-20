package com.dan323.proof.generic;

import com.dan323.expresions.base.LogicOperation;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStepSupplier;

import java.util.ArrayList;

/**
 * @author danco
 */
public abstract class Assume implements AbstractAction {

    private final LogicOperation log;

    public Assume(LogicOperation clo) {
        log = clo;
    }

    @Override
    public boolean isValid(Proof pf) {
        return true;
    }

    @Override
    public void applyStepSupplier(Proof pf, ProofStepSupplier supp) {
        int assLevel = 0;
        if (!pf.getSteps().isEmpty()) {
            assLevel = pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel();
        }
        pf.getSteps().add(supp.generateProofStep(assLevel + 1, log, new ProofReason("Ass", new ArrayList<>())));
    }
}
