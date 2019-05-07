package com.dan323.proof.generic;

import com.dan323.expresions.LogicOperation;
import com.dan323.expresions.util.BinaryOperation;
import com.dan323.expresions.util.Conjuntion;
import com.dan323.proof.Proof;
import com.dan323.proof.ProofReason;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author danco
 */
public class AndE implements AbstractAction {

    private final int applyAt;
    private Function<BinaryOperation,LogicOperation> side;

    public AndE(int app,Function<BinaryOperation,LogicOperation> fun){
        applyAt=app;
        side=fun;
    }

    @Override
    public boolean isValid(Proof pf) {
        return RuleUtils.isValidIndexAndProp(pf, applyAt) && RuleUtils.isOperation(pf, applyAt, Conjuntion.class);
    }

    @Override
    public void applyAbstract(Proof pf, ProofStepSupplier supp){
        LogicOperation log = pf.getSteps().get(applyAt - 1).getStep();
        int assLevel = 0;
        if (!pf.getSteps().isEmpty()) {
            assLevel = Action.getLastAssumptionLevel(pf);
        }
        List<Integer> lst = new ArrayList<>();
        lst.add(applyAt);
        pf.getSteps().add(supp.generateProofStep(assLevel,side.apply((BinaryOperation) log), new ProofReason("&E-2", lst)));
    }

    protected int getAppliedAt(){
        return applyAt;
    }

}
