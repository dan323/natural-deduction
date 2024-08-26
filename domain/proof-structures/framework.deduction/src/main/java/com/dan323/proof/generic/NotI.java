package com.dan323.proof.generic;

import com.dan323.expressions.base.Constant;
import com.dan323.expressions.base.LogicOperation;
import com.dan323.expressions.base.Negation;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofStepSupplier;

import java.util.List;
import java.util.function.Function;

/**
 * @author danco
 */
public abstract class NotI<T extends LogicOperation, Q extends ProofStep<T>, P extends Proof<T, Q>> implements Action<T, Q, P>, AbstractAction<T,Q,P> {

    private final Function<T, Negation<T>> negate;

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
    public boolean isValid(P pf) {
        int assLevel = RuleUtils.getLastAssumptionLevel(pf);
        if (assLevel == 0) {
            return false;
        }
        LogicOperation lo = pf.getSteps().getLast().getStep();
        if (lo instanceof Constant cons) {
            if (!cons.isFalsehood()) {
                return false;
            }
        } else {
            return false;
        }
        int lastAssumption = RuleUtils.getToLastAssumption(pf, assLevel);
        Q log = pf.getSteps().get(pf.getSteps().size() - lastAssumption);
        return "Ass".equals(log.getProof().getNameProof());
    }

    @Override
    public void applyStepSupplier(P pf, ProofStepSupplier<T, Q> supp) {
        int assLevel = RuleUtils.getLastAssumptionLevel(pf);
        int i = RuleUtils.disableUntilLastAssumption(pf, assLevel);
        var lst = new ProofReason.Range(pf.getSteps().size() - i + 1, pf.getSteps().size());
        pf.getSteps().add(supp.generateProofStep(assLevel - 1, negate.apply(pf.getSteps().get(pf.getSteps().size() - i).getStep()).castToLanguage(), new ProofReason("-I", List.of(lst), List.of())));
    }
}
