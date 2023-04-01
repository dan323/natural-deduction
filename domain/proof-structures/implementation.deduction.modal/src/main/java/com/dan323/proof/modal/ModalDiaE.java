package com.dan323.proof.modal;

import com.dan323.expressions.base.UnaryOperation;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.modal.Sometime;
import com.dan323.expressions.relation.LessEqual;
import com.dan323.expressions.relation.RelationOperation;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStepSupplier;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.List;
import java.util.Objects;

public final class ModalDiaE implements ModalAction {

    private final int j;

    public ModalDiaE(int i) {
        j = i;
    }

    @Override
    public boolean isValid(ModalNaturalDeduction pf) {

        if (!(!pf.getSteps().isEmpty() &&
                RuleUtils.isValidIndexAndProp(pf, j) &&
                RuleUtils.isOperation(pf, j, Sometime.class))) {
            return false;
        }

        String origin = pf.getSteps().get(j - 1).getState();
        int assLevel = RuleUtils.getLastAssumptionLevel(pf);
        if (assLevel < 2) {
            return false;
        }

        int i = RuleUtils.getToLastAssumption(pf, assLevel);
        ProofStepModal log1 = pf.getSteps().get(pf.getSteps().size() - i);
        ProofStepModal log2 = pf.getSteps().get(pf.getSteps().size() - i - 1);

        if (!log2.isValid()) {
            return false;
        }

        if (!(log2.getStep() instanceof LessEqual)) {
            if (log1.getStep() instanceof LessEqual && log2.getStep() instanceof ModalLogicalOperation) {
                log2 = log1;
                log1 = pf.getSteps().get(pf.getSteps().size() - i - 1);
            } else {
                return false;
            }
        } else if (!(log1.getStep() instanceof ModalLogicalOperation)) {
            return false;
        }

        String leftState = ((LessEqual) log2.getStep()).getLeft();
        String rightState = ((LessEqual) log2.getStep()).getRight();

        if (!(log1.getState().equals(rightState) && origin.equals(leftState))) {
            return false;
        }
        if (pf.stateIsUsedBefore(log1.getState(), pf.getSteps().size() - i - 1)) {
            return false;
        }
        if (!isNotFresh(pf, pf.getSteps().size() - i - 1)) {
            return false;
        }

        return log1.getStep().equals(((UnaryOperation<?>) pf.getSteps().get(j - 1).getStep()).getElement());
    }

    private boolean isNotFresh(ModalNaturalDeduction pf, int i) {
        if ((pf.getSteps().get(pf.getSteps().size() - 1).getStep()) instanceof ModalLogicalOperation) {
            String last = (pf.getSteps().get(pf.getSteps().size() - 1)).getState();
            return pf.stateIsUsedBefore(last, i);
        } else {
            RelationOperation operation = (RelationOperation) (pf.getSteps().get(pf.getSteps().size() - 1).getStep());
            return pf.stateIsUsedBefore(operation.getLeft(), i) &&
                    pf.stateIsUsedBefore(operation.getRight(), i);
        }
    }

    public void applyStepSupplier(ModalNaturalDeduction pf, ProofStepSupplier<ModalOperation, ProofStepModal> supp) {
        ProofStepModal psm = pf.getSteps().get(pf.getSteps().size() - 1);
        int i = RuleUtils.getToLastAssumption(pf, psm.getAssumptionLevel());
        List<Integer> lst = List.of(j, pf.getSteps().size() - i, pf.getSteps().size());
        pf.getSteps().add(supp.generateProofStep(psm.getAssumptionLevel() - 2, psm.getStep(), new ProofReason("<>E", lst)));
    }

    @Override
    public void apply(ModalNaturalDeduction pf) {
        ProofStepModal psm = pf.getSteps().get(pf.getSteps().size() - 1);
        applyStepSupplier(pf, ((assLevel, log, reason) -> new ProofStepModal(psm.getState(), assLevel, (ModalLogicalOperation) log, reason)));
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ModalDiaE) {
            return j == ((ModalDiaE) o).j;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), j);
    }
}