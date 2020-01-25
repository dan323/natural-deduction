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
public abstract class NotI<T extends LogicOperation, Q extends ProofStep<T>> implements AbstractAction<T, Q> {

    private Function<T, Negation<T>> negate;

    public NotI(Function<T, Negation<T>> negate) {
        this.negate = negate;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass().equals(getClass());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean isValid(Proof<T, Q> pf) {
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
            if (!cons.isFalsehood()) {
                return false;
            }
        } else {
            return false;
        }
        int lastAssumption = Action.getToLastAssumption(pf, assLevel);
        ProofStep log = pf.getSteps().get(pf.getSteps().size() - lastAssumption);
        return log.getProof().getNameProof().equals("Ass");
    }

    @Override
    public void applyStepSupplier(Proof<T, Q> pf, ProofStepSupplier<T, Q> supp) {
        int assLevel = pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel();
        int i = Action.disableUntilLastAssumption(pf, assLevel);
        List<Integer> lst = new ArrayList<>();
        lst.add(pf.getSteps().size() - i + 1);
        lst.add(pf.getSteps().size());
        pf.getSteps().add(supp.generateProofStep(assLevel - 1, negate.apply(pf.getSteps().get(pf.getSteps().size() - i).getStep()).castToLanguage(), new ProofReason("-I", lst)));
    }
}
