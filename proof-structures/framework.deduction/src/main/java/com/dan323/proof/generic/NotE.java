package com.dan323.proof.generic;

import com.dan323.expresions.base.LogicOperation;
import com.dan323.expresions.base.Negation;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofStepSupplier;

import java.util.ArrayList;
import java.util.List;

/**
 * @author danco
 */
public abstract class NotE<T extends LogicOperation, Q extends ProofStep<T>> implements AbstractAction<T, Q> {

    private final int neg;

    public NotE(int i) {
        neg = i;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj.getClass().equals(getClass())) {
            return ((NotE<?, ?>) obj).neg == neg;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getClass().hashCode() * 13 + neg * 17;
    }

    @Override
    public boolean isValid(Proof<T, Q> pf) {
        if (RuleUtils.isValidIndexAndProp(pf, neg)) {
            T lo = pf.getSteps().get(neg - 1).getStep();
            return (lo instanceof Negation) && (((Negation<?>) lo).getElement() instanceof Negation);
        }
        return false;
    }

    @Override
    public void applyStepSupplier(Proof<T, Q> pf, ProofStepSupplier<T, Q> supp) {
        int assLevel = 0;
        if (!pf.getSteps().isEmpty()) {
            assLevel = pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel();
        }
        List<Integer> lst = new ArrayList<>();
        lst.add(neg);
        T sol = ((Negation<T>) ((Negation) (pf.getSteps().get(neg - 1).getStep())).getElement()).getElement();
        pf.getSteps().add(supp.generateProofStep(assLevel, sol, new ProofReason("-E", lst)));
    }

    protected int getNeg() {
        return neg;
    }
}
