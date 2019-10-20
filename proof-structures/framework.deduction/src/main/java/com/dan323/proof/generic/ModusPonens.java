package com.dan323.proof.generic;

import com.dan323.expresions.base.BinaryOperation;
import com.dan323.expresions.base.LogicOperation;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStepSupplier;

import java.util.ArrayList;
import java.util.List;

/**
 * @author danco
 */
public abstract class ModusPonens implements AbstractAction {

    private final int applyAt1;
    private final int applyAt2;

    public ModusPonens(int i1, int i2) {
        applyAt1 = i1;
        applyAt2 = i2;
    }

    protected int get1() {
        return applyAt1;
    }

    protected int get2() {
        return applyAt2;
    }

    @Override
    public boolean isValid(Proof pf) {
        return RuleUtils.isValidModusPonens(pf, applyAt1, applyAt2);
    }

    @Override
    public void applyStepSupplier(Proof pf, ProofStepSupplier supp) {
        LogicOperation sol = ((BinaryOperation) pf.getSteps().get(applyAt1 - 1).getStep()).getRight();
        int assLevel = 0;
        if (!pf.getSteps().isEmpty()) {
            assLevel = Action.getLastAssumptionLevel(pf);
        }
        List<Integer> lst = new ArrayList<>();
        lst.add(applyAt1);
        lst.add(applyAt2);
        pf.getSteps().add(supp.generateProofStep(assLevel, sol, new ProofReason("->E", lst)));
    }
}
