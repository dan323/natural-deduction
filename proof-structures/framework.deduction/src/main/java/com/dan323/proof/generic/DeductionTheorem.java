package com.dan323.proof.generic;

import com.dan323.expresions.base.Implication;
import com.dan323.expresions.base.LogicOperation;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofStepSupplier;

import java.util.List;
import java.util.function.BiFunction;

/**
 * @author danco
 */
public abstract class DeductionTheorem<T extends LogicOperation, Q extends ProofStep<T>> implements AbstractAction<T, Q> {

    private BiFunction<T, T, Implication<T>> constructor;

    public DeductionTheorem(BiFunction<T, T, Implication<T>> construct) {
        constructor = construct;
    }

    public boolean equals(Object obj) {
        return obj != null && getClass().equals(obj.getClass());
    }

    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean isValid(Proof<T, Q> pf) {
        int assLevel = Action.getLastAssumptionLevel(pf);
        if (assLevel == 0) {
            return false;
        }
        int lastAssumption = Action.getToLastAssumption(pf, assLevel);
        ProofStep<T> log = pf.getSteps().get(pf.getSteps().size() - lastAssumption);
        return log.getProof().getNameProof().equals("Ass");
    }

    @Override
    public void applyStepSupplier(Proof<T, Q> pf, ProofStepSupplier<T, Q> supp) {
        int assLevel = Action.getLastAssumptionLevel(pf);
        int i = Action.disableUntilLastAssumption(pf, assLevel);
        pf.getSteps().add(supp.generateProofStep(assLevel - 1,
                constructor.apply(pf.getSteps().get(pf.getSteps().size() - i).getStep(),
                        pf.getSteps().get(pf.getSteps().size() - 1).getStep()).castToLanguage(), new ProofReason("->I", List.of(pf.getSteps().size() - i + 1, pf.getSteps().size()))));
    }
}
