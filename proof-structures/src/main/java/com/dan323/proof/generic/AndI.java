package com.dan323.proof.generic;

import com.dan323.expresions.LogicOperation;
import com.dan323.expresions.util.Conjuntion;
import com.dan323.proof.Proof;
import com.dan323.proof.ProofReason;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author danco
 */
public abstract class AndI implements AbstractAction {

    private final int applyAt1;
    private final int applyAt2;
    private final BiFunction<LogicOperation, LogicOperation, Conjuntion> conjuntionConstructor;

    public AndI(int a, int b, BiFunction<LogicOperation, LogicOperation, Conjuntion> construct) {
        applyAt1 = a;
        applyAt2 = b;
        conjuntionConstructor = construct;
    }

    @Override
    public boolean isValid(Proof pf) {
        return RuleUtils.isValidIndexAndProp(pf, applyAt1) && RuleUtils.isValidIndexAndProp(pf, applyAt2);
    }

    @Override
    public void applyAbstract(Proof pf, ProofStepSupplier supp) {
        int assLevel = 0;
        if (!pf.getSteps().isEmpty()) {
            assLevel = Action.getLastAssumptionLevel(pf);
        }
        List<Integer> lst = new ArrayList<>();
        lst.add(applyAt1);
        lst.add(applyAt2);
        pf.getSteps().add(supp.generateProofStep(assLevel, conjuntionConstructor.apply(pf.getSteps().get(applyAt1 - 1).getStep(), pf.getSteps().get(applyAt2 - 1).getStep())
                , new ProofReason("&I", lst)));

    }

    protected int get1(){
        return applyAt1;
    }

    protected int get2(){
        return applyAt2;
    }

}
