package com.dan323.proof.generic;

import com.dan323.expresions.base.Constant;
import com.dan323.expresions.base.LogicOperation;
import com.dan323.expresions.base.Negation;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofStepSupplier;

import java.util.List;
import java.util.function.Function;

/**
 * @author danco
 */
public abstract class NotI<T extends LogicOperation, Q extends ProofStep<T>, P extends Proof<T, Q, ?>> implements AbstractAction<T, Q, P> {

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
        LogicOperation lo = pf.getSteps().get(pf.getSteps().size() - 1).getStep();
        if (lo instanceof Constant) {
            Constant<?> cons = (Constant<?>) lo;
            if (!cons.isFalsehood()) {
                return false;
            }
        } else {
            return false;
        }
        int lastAssumption = RuleUtils.getToLastAssumption(pf, assLevel);
        Q log = pf.getSteps().get(pf.getSteps().size() - lastAssumption);
        return log.getProof().getNameProof().equals("Ass");
    }

    @Override
    public void applyStepSupplier(P pf, ProofStepSupplier<T, Q> supp) {
        int assLevel = RuleUtils.getLastAssumptionLevel(pf);
        int i = RuleUtils.disableUntilLastAssumption(pf, assLevel);
        List<Integer> lst = List.of(pf.getSteps().size() - i + 1, pf.getSteps().size());
        pf.getSteps().add(supp.generateProofStep(assLevel - 1, negate.apply(pf.getSteps().get(pf.getSteps().size() - i).getStep()).castToLanguage(), new ProofReason("-I", lst)));
    }
}
