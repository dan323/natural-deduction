package com.dan323.proof.generic;

import com.dan323.expresions.base.Constant;
import com.dan323.expresions.base.LogicOperation;
import com.dan323.expresions.base.Negation;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofStepSupplier;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author danco
 */
public abstract class FI<T extends LogicOperation, Q extends ProofStep<T>> implements AbstractAction<T, Q> {

    private final int neg;
    private final int pos;
    private final Class<Negation<T>> negationClass;
    private final Supplier<Constant<T>> constantFunction;

    public FI(int a, int b, Function<T, Negation<T>> negate, Supplier<Constant<T>> genConst) {
        pos = a;
        neg = b;
        negationClass = (Class<Negation<T>>) negate.apply(null).getClass();
        constantFunction = genConst;
    }

    @Override
    public boolean isValid(Proof<T, Q> pf) {
        if (RuleUtils.isValidIndexAndProp(pf, neg) && RuleUtils.isValidIndexAndProp(pf, pos) && RuleUtils.isOperation(pf, neg, Negation.class)) {
            Negation<?> negation = (Negation<?>) pf.getSteps().get(neg - 1).getStep();
            if (negation.getElement().equals(pf.getSteps().get(pos - 1).getStep())) {
                return negationClass.equals(negation.getClass());
            }
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
    public void applyStepSupplier(Proof<T, Q> pf, ProofStepSupplier<T, Q> supp) {
        int assLevel = pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel();
        pf.getSteps().add(supp.generateProofStep(assLevel, constantFunction.get().castToLanguage(), new ProofReason("FI", List.of(pos, neg))));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode() * 31 + pos * 17 + neg * 23;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass().equals(getClass()) && ((FI<?, ?>) obj).neg == neg && ((FI<?, ?>) obj).pos == pos;
    }
}
