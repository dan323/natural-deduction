package com.dan323.proof.generic;

import com.dan323.expresions.base.Conjunction;
import com.dan323.expresions.base.LogicOperation;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofStepSupplier;

import java.util.List;
import java.util.function.BiFunction;

/**
 * @author danco
 */
public abstract class AndI<T extends LogicOperation, Q extends ProofStep<T>, P extends Proof<T, Q>> implements AbstractAction<T, Q, P> {

    private final int applyAt1;
    private final int applyAt2;
    private final BiFunction<T, T, Conjunction<T>> conjunctionConstructor;

    public AndI(int a, int b, BiFunction<T, T, Conjunction<T>> construct) {
        applyAt1 = a;
        applyAt2 = b;
        conjunctionConstructor = construct;
    }

    @Override
    public boolean equals(Object object) {
        if (object != null && object.getClass().equals(getClass())) {
            AndI<?, ?, ?> obj = (AndI<?, ?, ?>) object;
            return applyAt1 == obj.applyAt1 && applyAt2 == obj.applyAt2;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 13 * applyAt1 + 17 * applyAt2;
    }

    @Override
    public boolean isValid(P pf) {
        return RuleUtils.isValidIndexAndProp(pf, applyAt1) && RuleUtils.isValidIndexAndProp(pf, applyAt2);
    }

    @Override
    public void applyStepSupplier(P pf, ProofStepSupplier<T, Q> supp) {
        int assLevel = RuleUtils.getLastAssumptionLevel(pf);
        pf.getSteps().add(supp.generateProofStep(assLevel,
                conjunctionConstructor.apply(pf.getSteps().get(applyAt1 - 1).getStep(),
                        pf.getSteps().get(applyAt2 - 1).getStep()).castToLanguage(), getReason()));

    }

    private ProofReason getReason() {
        return new ProofReason("&I", List.of(applyAt1, applyAt2));
    }

    protected int get1() {
        return applyAt1;
    }

    protected int get2() {
        return applyAt2;
    }

}
