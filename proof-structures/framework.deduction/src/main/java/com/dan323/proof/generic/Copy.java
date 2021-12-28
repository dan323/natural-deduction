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
public abstract class Copy<T extends LogicOperation, Q extends ProofStep<T>, P extends Proof<T, Q>> implements AbstractAction<T, Q, P> {

    private final int source;

    public Copy(int i) {
        source = i;
    }

    @Override
    public boolean isValid(P pf) {
        return RuleUtils.isValidIndexAndProp(pf, source);
    }

    @Override
    public void applyStepSupplier(P pf, ProofStepSupplier<T, Q> supp) {
        pf.getSteps().add(supp.generateProofStep(RuleUtils.getLastAssumptionLevel(pf), pf.getSteps().get(source - 1).getStep(), getReason()));
    }

    public int getAppliedAt() {
        return source;
    }

    protected ProofReason getReason() {
        return new ProofReason("Rep", Collections.singletonList(source));
    }
}
