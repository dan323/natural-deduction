package com.dan323.proof.generic;

import com.dan323.expresions.base.Disjunction;
import com.dan323.expresions.base.LogicOperation;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofStepSupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author danco
 */
public abstract class OrI<T extends LogicOperation, Q extends ProofStep<T>> implements AbstractAction<T, Q> {

    private int applyAt;
    private T intro;
    private BiFunction<T, T, Disjunction<T>> disjunction;

    public OrI(int app, T lo, BiFunction<T, T, Disjunction<T>> disjunction) {
        applyAt = app;
        intro = lo;
        this.disjunction = disjunction;
    }

    @Override
    public boolean isValid(Proof<T, Q> pf) {
        return RuleUtils.isValidIndexAndProp(pf, applyAt);
    }

    @Override
    public void applyStepSupplier(Proof<T, Q> pf, ProofStepSupplier<T, Q> supp) {
        int assLevel = 0;
        if (!pf.getSteps().isEmpty()) {
            assLevel = pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel();
        }
        List<Integer> lst = new ArrayList<>();
        lst.add(applyAt);
        T result = disjunction.apply(pf.getSteps().get(applyAt - 1).getStep(), intro).castToLanguage();
        pf.getSteps().add(supp.generateProofStep(assLevel, result, new ProofReason("|I-1", lst)));
    }

    protected int getAt() {
        return applyAt;
    }
}
