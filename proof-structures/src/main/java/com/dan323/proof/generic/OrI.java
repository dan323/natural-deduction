package com.dan323.proof.generic;

import com.dan323.expresions.LogicOperation;
import com.dan323.expresions.util.Disjunction;
import com.dan323.proof.Proof;
import com.dan323.proof.ProofReason;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author danco
 */
public class OrI implements AbstractAction {

    private int applyAt;
    private LogicOperation intro;
    private BiFunction<LogicOperation, LogicOperation, Disjunction> disjunction;

    public OrI(int app, LogicOperation lo, BiFunction<LogicOperation, LogicOperation, Disjunction> disjunction) {
        applyAt = app;
        intro = lo;
        this.disjunction = disjunction;
    }

    @Override
    public boolean isValid(Proof pf) {
        return RuleUtils.isValidIndexAndProp(pf, applyAt);
    }

    @Override
    public void applyAbstract(Proof pf, ProofStepSupplier supp) {
        int assLevel = 0;
        if (!pf.getSteps().isEmpty()) {
            assLevel = pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel();
        }
        List<Integer> lst = new ArrayList<>();
        lst.add(applyAt);
        LogicOperation result = disjunction.apply(pf.getSteps().get(applyAt - 1).getStep(), intro);
        pf.getSteps().add(supp.generateProofStep(assLevel, result, new ProofReason("|I-1", lst)));
    }

    protected int getAt(){
        return applyAt;
    }
}
