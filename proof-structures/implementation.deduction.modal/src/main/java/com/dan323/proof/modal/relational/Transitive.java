package com.dan323.proof.modal.relational;

import com.dan323.expresions.modal.ModalOperation;
import com.dan323.expresions.relation.LessEqual;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStepSupplier;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.List;

/**
 * @author danco
 */
public final class Transitive<T> extends RelationalAction<T> {

    private final int first;
    private final int second;

    public Transitive(int first, int second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean isValid(ModalNaturalDeduction<T> pf) {
        if (!(pf.getSteps().get(first - 1).getStep() instanceof LessEqual) || !(pf.getSteps().get(second - 1).getStep() instanceof LessEqual)) {
            return false;
        } else {
            LessEqual<T> firstLog = (LessEqual<T>) pf.getSteps().get(first - 1).getStep();
            LessEqual<T> secondLog = (LessEqual<T>) pf.getSteps().get(second - 1).getStep();
            return firstLog.getRight().equals(secondLog.getLeft());
        }
    }

    @Override
    public void applyStepSupplier(ModalNaturalDeduction<T> pf, ProofStepSupplier<ModalOperation, ProofStepModal<T>> supp) {
        T s1 = ((LessEqual<T>) pf.getSteps().get(first - 1).getStep()).getLeft();
        T s3 = ((LessEqual<T>) pf.getSteps().get(first - 1).getStep()).getRight();
        pf.getSteps().add(supp.generateProofStep(RuleUtils.getLastAssumptionLevel(pf), new LessEqual<>(s1, s3), new ProofReason("Trans", List.of())));
    }
}
