package com.dan323.proof.modal;

import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.expresions.modal.ModalOperation;
import com.dan323.expresions.modal.Sometime;
import com.dan323.expresions.relation.LessEqual;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStepSupplier;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.Arrays;
import java.util.Objects;

public final class ModalDiaI<T> implements ModalAction<T> {

    private final int i;
    private final int j;

    public ModalDiaI(int a, int b) {
        i = a;
        j = b;
    }

    @Override
    public boolean isValid(ModalNaturalDeduction<T> pf) {
        if (RuleUtils.isValidIndexAndProp(pf, i) && pf.getSteps().get(i - 1).getStep() instanceof ModalLogicalOperation &&
                RuleUtils.isValidIndexAndProp(pf, j) && pf.getSteps().get(j - 1).getStep() instanceof LessEqual) {
            LessEqual<T> lessEqual = (LessEqual<T>) pf.getSteps().get(j - 1).getStep();
            T futureState = pf.getSteps().get(i - 1).getState();
            return futureState.equals(lessEqual.getRight());
        } else {
            return false;
        }
    }

    @Override
    public void applyStepSupplier(ModalNaturalDeduction<T> pf, ProofStepSupplier<ModalOperation, ProofStepModal<T>> supp) {
        ModalLogicalOperation log = (ModalLogicalOperation) pf.getSteps().get(i - 1).getStep();
        pf.getSteps().add(supp.generateProofStep(pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel(), new Sometime(log), new ProofReason("<>I", Arrays.asList(i, j))));
    }

    @Override
    public void apply(ModalNaturalDeduction<T> pf) {
        LessEqual<T> lessEqual = (LessEqual<T>) pf.getSteps().get(j - 1).getStep();
        applyStepSupplier(pf, ((assLevel, log, reason) -> new ProofStepModal<>(lessEqual.getLeft(), assLevel, (ModalLogicalOperation) log, reason)));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), i, j);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof ModalDiaI) && ((ModalDiaI<?>) obj).i == i && ((ModalDiaI<?>) obj).j == j;
    }
}
