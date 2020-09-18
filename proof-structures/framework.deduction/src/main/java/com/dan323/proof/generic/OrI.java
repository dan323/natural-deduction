package com.dan323.proof.generic;

import com.dan323.expresions.base.LogicOperation;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofStepSupplier;

import java.util.Collections;
import java.util.Objects;
import java.util.function.BinaryOperator;

/**
 * @author danco
 */
public abstract class OrI<T extends LogicOperation, Q extends ProofStep<T>, P extends Proof<T, Q, ?>> implements AbstractAction<T, Q, P> {

    private final int applyAt;
    private final T intro;
    private final BinaryOperator<T> disjunction;

    public OrI(int app, T lo, BinaryOperator<T> disjunction) {
        applyAt = app;
        intro = lo;
        this.disjunction = disjunction;
    }

    @Override
    public boolean isValid(P pf) {
        return RuleUtils.isValidIndexAndProp(pf, applyAt);
    }

    @Override
    public void applyStepSupplier(P pf, ProofStepSupplier<T, Q> supp) {
        int assLevel = 0;
        if (!pf.getSteps().isEmpty()) {
            assLevel = pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel();
        }
        T result = disjunction.apply(pf.getSteps().get(applyAt - 1).getStep(), intro);
        pf.getSteps().add(supp.generateProofStep(assLevel, result, getReason()));
    }

    private ProofReason getReason() {
        return new ProofReason("|I", Collections.singletonList(applyAt));
    }

    protected int getAt() {
        return applyAt;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof OrI &&
                o.getClass().equals(getClass()) &&
                ((OrI<?, ?, ?>) o).applyAt == applyAt &&
                Objects.equals(((OrI<?, ?, ?>) o).intro, intro);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applyAt, intro, getClass());
    }
}
