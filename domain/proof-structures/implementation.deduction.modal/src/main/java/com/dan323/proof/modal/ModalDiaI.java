package com.dan323.proof.modal;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.modal.Sometime;
import com.dan323.expressions.relation.LessEqual;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStepSupplier;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class ModalDiaI implements ModalAction {

    private final int i;
    private final int j;

    public ModalDiaI(int a, int b) {
        i = a;
        j = b;
    }

    @Override
    public boolean isValid(ModalNaturalDeduction pf) {
        if (RuleUtils.isValidIndexAndProp(pf, i) && pf.getSteps().get(i - 1).getStep() instanceof ModalLogicalOperation &&
                RuleUtils.isValidIndexAndProp(pf, j) && pf.getSteps().get(j - 1).getStep() instanceof LessEqual lessEqual) {
            String futureState = pf.getSteps().get(i - 1).getState();
            return futureState.equals(lessEqual.getRight());
        } else {
            return false;
        }
    }

    @Override
    public void applyStepSupplier(ModalNaturalDeduction pf, ProofStepSupplier<ModalOperation, ProofStepModal> supp) {
        ModalLogicalOperation log = (ModalLogicalOperation) pf.getSteps().get(i - 1).getStep();
        pf.getSteps().add(supp.generateProofStep(pf.getSteps().getLast().getAssumptionLevel(), new Sometime(log), new ProofReason("<>I", List.of(), Arrays.asList(i, j))));
    }

    @Override
    public void apply(ModalNaturalDeduction pf) {
        LessEqual lessEqual = (LessEqual) pf.getSteps().get(j - 1).getStep();
        applyStepSupplier(pf, ((assLevel, log, reason) -> new ProofStepModal(lessEqual.getLeft(), assLevel, (ModalLogicalOperation) log, reason)));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), i, j);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof ModalDiaI diaI) && diaI.i == i && diaI.j == j;
    }
}
