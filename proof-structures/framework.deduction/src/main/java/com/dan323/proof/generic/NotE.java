package com.dan323.proof.generic;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.expressions.base.Negation;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofStepSupplier;

import java.util.Collections;

/**
 * @author danco
 */
public abstract class NotE<T extends LogicOperation, Q extends ProofStep<T>, P extends Proof<T, Q, ?>> implements AbstractAction<T, Q, P> {

    private final int neg;

    public NotE(int i) {
        neg = i;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj.getClass().equals(getClass())) {
            return ((NotE<?, ?, ?>) obj).neg == neg;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getClass().hashCode() * 13 + neg * 17;
    }

    @Override
    public boolean isValid(P pf) {
        if (RuleUtils.isValidIndexAndProp(pf, neg)) {
            T lo = pf.getSteps().get(neg - 1).getStep();
            return (lo instanceof Negation) && (((Negation<?>) lo).getElement() instanceof Negation);
        }
        return false;
    }

    @Override
    public void applyStepSupplier(P pf, ProofStepSupplier<T, Q> supp) {
        int assLevel = pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel();
        T sol = ((Negation<Negation<T>>) (pf.getSteps().get(neg - 1).getStep())).getElement().getElement();
        pf.getSteps().add(supp.generateProofStep(assLevel, sol, getReason()));
    }

    private ProofReason getReason() {
        return new ProofReason("-E", Collections.singletonList(neg));
    }

    protected int getNeg() {
        return neg;
    }
}
