package com.dan323.proof.generic;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofStepSupplier;

import java.util.Collections;

/**
 * @author danco
 */
public abstract class Assume<T extends LogicOperation, Q extends ProofStep<T>, P extends Proof<T, Q, ?>> implements AbstractAction<T, Q, P> {

    protected final T log;

    public Assume(T clo) {
        log = clo;
    }

    @Override
    public boolean isValid(P pf) {
        return true;
    }

    @Override
    public void applyStepSupplier(P pf, ProofStepSupplier<T, Q> supp) {
        int assLevel = RuleUtils.getLastAssumptionLevel(pf);
        pf.getSteps().add(supp.generateProofStep(assLevel + 1, log, getReason()));
    }

    protected ProofReason getReason() {
        return new ProofReason("Ass", Collections.emptyList());
    }
}
