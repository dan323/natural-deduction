package com.dan323.proof.generic;

import com.dan323.expresions.base.Constant;
import com.dan323.expresions.base.LogicOperation;
import com.dan323.expresions.base.Negation;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofStepSupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author danco
 */
public abstract class NotI implements AbstractAction {

    private Function<LogicOperation, Negation> negate;

    public NotI(Function<LogicOperation, Negation> negate) {
        this.negate = negate;
    }

    @Override
    public boolean isValid(Proof pf) {
        if (pf.getSteps().isEmpty()) {
            return false;
        }
        int assLevel = pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel();
        if (assLevel == 0) {
            return false;
        }
        LogicOperation lo = pf.getSteps().get(pf.getSteps().size() - 1).getStep();
        if (lo instanceof Constant) {
            Constant cons = (Constant) lo;
            if (!cons.construct(0).equals(cons)) {
                return false;
            }
        }
        int lastAssumption = Action.getToLastAssumption(pf, assLevel);
        ProofStep log = pf.getSteps().get(pf.getSteps().size() - lastAssumption);
        return log.getProof().getNameProof().equals("Ass");
    }

    @Override
    public void applyStepSupplier(Proof pf, ProofStepSupplier supp) {
        int assLevel = pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel();
        int i = Action.disableUntilLastAssumption(pf, assLevel);
        List<Integer> lst = new ArrayList<>();
        lst.add(pf.getSteps().size() - i + 1);
        lst.add(pf.getSteps().size());
        pf.getSteps().add(supp.generateProofStep(assLevel - 1, negate.apply(pf.getSteps().get(pf.getSteps().size() - i).getStep()), new ProofReason("-I", lst)));
    }
}
