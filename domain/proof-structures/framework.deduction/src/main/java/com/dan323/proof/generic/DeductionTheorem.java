package com.dan323.proof.generic;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofStepSupplier;

import java.util.List;
import java.util.function.BinaryOperator;

/**
 * @author danco
 */
public abstract class DeductionTheorem<T extends LogicOperation, Q extends ProofStep<T>, P extends Proof<T, Q>> implements Action<T, Q, P>, AbstractAction<T,Q,P> {

    private final BinaryOperator<T> constructor;

    public DeductionTheorem(BinaryOperator<T> construct) {
        constructor = construct;
    }

    public boolean equals(Object obj) {
        return obj != null && getClass().equals(obj.getClass());
    }

    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean isValid(P pf) {
        int assLevel = RuleUtils.getLastAssumptionLevel(pf);
        if (assLevel == 0) {
            return false;
        }
        int lastAssumption = RuleUtils.getToLastAssumption(pf, assLevel);
        ProofStep<T> log = pf.getSteps().get(pf.getSteps().size() - lastAssumption);
        return "Ass".equals(log.getProof().getNameProof());
    }

    @Override
    public void applyStepSupplier(P pf, ProofStepSupplier<T, Q> supp) {
        int assLevel = RuleUtils.getLastAssumptionLevel(pf);
        int i = RuleUtils.disableUntilLastAssumption(pf, assLevel);
        pf.getSteps().add(supp.generateProofStep(assLevel - 1,
                constructor.apply(pf.getSteps().get(pf.getSteps().size() - i).getStep(),
                        pf.getSteps().getLast().getStep()), new ProofReason("->I", List.of(new ProofReason.Range(pf.getSteps().size() - i + 1, pf.getSteps().size())), List.of())));
    }
}
