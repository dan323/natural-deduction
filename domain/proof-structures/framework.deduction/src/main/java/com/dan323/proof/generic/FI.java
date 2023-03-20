package com.dan323.proof.generic;

import com.dan323.expressions.base.Constant;
import com.dan323.expressions.base.LogicOperation;
import com.dan323.expressions.base.Negation;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofStepSupplier;

import java.util.List;
import java.util.function.Supplier;

/**
 * @author danco
 */
public abstract class FI<T extends LogicOperation, Q extends ProofStep<T>, P extends Proof<T, Q>> implements Action<T, Q, P>, AbstractAction<T,Q,P> {

    private final int neg;
    private final int pos;
    private final Supplier<Constant> constantFunction;

    public FI(int a, int b, Supplier<Constant> genConst) {
        pos = a;
        neg = b;
        constantFunction = genConst;
    }

    @Override
    public boolean isValid(P pf) {
        if (RuleUtils.isValidIndexAndProp(pf, neg) &&
                RuleUtils.isValidIndexAndProp(pf, pos) &&
                RuleUtils.isOperation(pf, neg, Negation.class)) {
            Negation<?> negation = (Negation<?>) pf.getSteps().get(neg - 1).getStep();
            return negation.getElement().equals(pf.getSteps().get(pos - 1).getStep());
        }
        return false;
    }

    protected int getPos() {
        return pos;
    }

    protected int getNeg() {
        return neg;
    }

    @Override
    public void applyStepSupplier(P pf, ProofStepSupplier<T, Q> supp) {
        int assLevel = pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel();
        pf.getSteps().add(supp.generateProofStep(assLevel, (T)constantFunction.get(), new ProofReason("FI", List.of(pos, neg))));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode() * 31 + pos * 17 + neg * 23;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass().equals(getClass()) && ((FI<?, ?, ?>) obj).neg == neg && ((FI<?, ?, ?>) obj).pos == pos;
    }
}
