package com.dan323.proof.generic;

import com.dan323.expressions.base.BinaryOperation;
import com.dan323.expressions.base.LogicOperation;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofStepSupplier;

import java.util.List;

/**
 * @author danco
 */
public abstract class ModusPonens<T extends LogicOperation, Q extends ProofStep<T>, P extends Proof<T, Q>> implements Action<T, Q, P>, AbstractAction<T,Q,P> {

    private final int applyAt1;
    private final int applyAt2;

    public ModusPonens(int i1, int i2) {
        applyAt1 = i1;
        applyAt2 = i2;
    }

    public boolean equals(Object obj) {
        if (obj != null && getClass().equals(obj.getClass())) {
            return ((ModusPonens<?, ?, ?>) obj).applyAt1 == applyAt1 && ((ModusPonens<?, ?, ?>) obj).applyAt2 == applyAt2;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return applyAt1 * 17 + applyAt2 * 19;
    }

    protected int get1() {
        return applyAt1;
    }

    protected int get2() {
        return applyAt2;
    }

    @Override
    public boolean isValid(P pf) {
        return RuleUtils.isValidModusPonens(pf, applyAt1, applyAt2);
    }

    @Override
    public void applyStepSupplier(P pf, ProofStepSupplier<T, Q> supp) {
        T sol = ((BinaryOperation<T>) pf.getSteps().get(applyAt1 - 1).getStep()).getRight();
        int assLevel = RuleUtils.getLastAssumptionLevel(pf);
        pf.getSteps().add(supp.generateProofStep(assLevel, sol, new ProofReason("->E", List.of(), List.of(applyAt1, applyAt2))));
    }
}
