package com.dan323.proof.generic;

import com.dan323.expresions.base.Constant;
import com.dan323.expresions.base.LogicOperation;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofStepSupplier;

import java.util.ArrayList;
import java.util.List;

/**
 * @author danco
 */
public abstract class FE<T extends LogicOperation, Q extends ProofStep<T>> implements AbstractAction<T, Q> {

    private T intro;
    private int falseIndex;

    public FE(T intro, int falseIndex) {
        this.intro = intro;
        this.falseIndex = falseIndex;
    }

    @Override
    public boolean isValid(Proof<T, Q> pf) {
        if (RuleUtils.isValidIndexAndProp(pf, falseIndex) && RuleUtils.isOperation(pf, falseIndex, Constant.class)) {
            Constant cons = (Constant) pf.getSteps().get(falseIndex - 1).getStep();
            return cons.isFalsehood();
        }
        return false;
    }

    @Override
    public void applyStepSupplier(Proof<T, Q> pf, ProofStepSupplier<T, Q> supp) {
        int assLevel = 0;
        if (!pf.getSteps().isEmpty()) {
            assLevel = Action.getLastAssumptionLevel(pf);
        }
        List<Integer> lst = new ArrayList<>();
        lst.add(falseIndex);
        pf.getSteps().add(supp.generateProofStep(assLevel, intro, new ProofReason("FE", lst)));
    }
}
