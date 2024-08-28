package com.dan323.proof.generic;

import com.dan323.expressions.base.Constant;
import com.dan323.expressions.base.LogicOperation;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofStepSupplier;

import java.util.List;
import java.util.Objects;

/**
 * @author danco
 */
public abstract class FE<T extends LogicOperation, Q extends ProofStep<T>, P extends Proof<T, Q>> implements Action<T, Q, P>, AbstractAction<T,Q,P> {

    private final T intro;
    private final int falseIndex;

    public FE(T intro, int falseIndex) {
        this.intro = intro;
        this.falseIndex = falseIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FE<?, ?, ?> fe)) return false;
        return o.getClass().equals(getClass()) && falseIndex == fe.falseIndex &&
                Objects.equals(intro, fe.intro);
    }

    @Override
    public int hashCode() {
        return Objects.hash(intro, falseIndex, getClass());
    }

    @Override
    public boolean isValid(P pf) {
        if (RuleUtils.isValidIndexAndProp(pf, falseIndex) && RuleUtils.isOperation(pf, falseIndex, Constant.class)) {
            Constant cons = (Constant) pf.getSteps().get(falseIndex - 1).getStep();
            return cons.isFalsehood();
        }
        return false;
    }

    @Override
    public void applyStepSupplier(P pf, ProofStepSupplier<T, Q> supp) {
        pf.getSteps().add(supp.generateProofStep(RuleUtils.getLastAssumptionLevel(pf), intro, getReason()));
    }

    private ProofReason getReason() {
        return new ProofReason("FE", List.of(), List.of(falseIndex));
    }
}
