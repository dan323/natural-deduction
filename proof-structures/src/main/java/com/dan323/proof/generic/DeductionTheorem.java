package com.dan323.proof.generic;

import com.dan323.expresions.LogicOperation;
import com.dan323.expresions.util.Implication;
import com.dan323.proof.Proof;
import com.dan323.proof.ProofReason;
import com.dan323.proof.ProofStep;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author danco
 */
public abstract class DeductionTheorem implements AbstractAction {

    private int lastAssumption;
    private BiFunction<LogicOperation, LogicOperation, Implication> constructor;

    public DeductionTheorem(BiFunction<LogicOperation, LogicOperation, Implication> construct) {
        constructor = construct;
    }

    @Override
    public boolean isValid(Proof pf) {
        if (pf.getSteps().isEmpty()) {
            return false;
        }
        int assLevel = Action.getLastAssumptionLevel(pf);
        if (assLevel == 0) {
            return false;
        }
        lastAssumption = Action.getToLastAssumption(pf, assLevel);
        ProofStep log = pf.getSteps().get(pf.getSteps().size() - lastAssumption);
        return log.getProof().getNameProof().equals("Ass");
    }

    protected int getLastAssumption() {
        return lastAssumption;
    }

    @Override
    public void applyAbstract(Proof pf, ProofStepSupplier supp) {
        int assLevel = Action.getLastAssumptionLevel(pf);
        int i = Action.disableUntilLastAssumption(pf, assLevel);
        List<Integer> lst = new ArrayList<>();
        lst.add(pf.getSteps().size() - i + 1);
        lst.add(pf.getSteps().size());
        pf.getSteps().add(supp.generateProofStep(assLevel - 1, constructor.apply(pf.getSteps().get(pf.getSteps().size() - i).getStep(), pf.getSteps().get(pf.getSteps().size() - 1).getStep()), new ProofReason("->I", lst)));
    }
}
