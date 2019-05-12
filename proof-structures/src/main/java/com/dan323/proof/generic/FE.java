package com.dan323.proof.generic;

import com.dan323.expresions.LogicOperation;
import com.dan323.expresions.util.Constant;
import com.dan323.proof.Proof;
import com.dan323.proof.ProofReason;

import java.util.ArrayList;
import java.util.List;

/**
 * @author danco
 */
public abstract class FE implements AbstractAction {

    private LogicOperation intro;
    private int falseIndex;

    public FE(LogicOperation intro, int falseIndex) {
        this.intro = intro;
        this.falseIndex = falseIndex;
    }

    @Override
    public boolean isValid(Proof pf) {
        if (RuleUtils.isValidIndexAndProp(pf, falseIndex) && RuleUtils.isOperation(pf, falseIndex, Constant.class)) {
            Constant cons = (Constant) pf.getSteps().get(falseIndex - 1).getStep();
            return cons.construct(0).equals(cons);
        }
        return false;
    }

    @Override
    public void applyAbstract(Proof pf, ProofStepSupplier supp) {
        int assLevel = 0;
        if (!pf.getSteps().isEmpty()) {
            assLevel = Action.getLastAssumptionLevel(pf);
        }
        List<Integer> lst = new ArrayList<>();
        lst.add(falseIndex);
        pf.getSteps().add(supp.generateProofStep(assLevel, intro, new ProofReason("FE", lst)));
    }
}
