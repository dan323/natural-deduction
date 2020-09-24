package com.dan323.proof.modal;

import com.dan323.expresions.base.UnaryOperation;
import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.expresions.modal.ModalOperation;
import com.dan323.expresions.modal.Sometime;
import com.dan323.expresions.relation.LessEqual;
import com.dan323.expresions.relation.RelationOperation;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStepSupplier;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.List;

public final class ModalDiaE<T> implements ModalAction<T> {

    private final int j;

    public ModalDiaE(int i) {
        j = i;
    }

    @Override
    public boolean isValid(ModalNaturalDeduction<T> pf) {

        if (!(!pf.getSteps().isEmpty() &&
                RuleUtils.isValidIndexAndProp(pf, j) &&
                RuleUtils.isOperation(pf, j, Sometime.class))) {
            return false;
        }

        T origin = pf.getSteps().get(j - 1).getState();
        int assLevel = RuleUtils.getLastAssumptionLevel(pf);
        if (assLevel < 2) {
            return false;
        }

        int i = RuleUtils.getToLastAssumption(pf, assLevel);
        ProofStepModal<T> log1 = pf.getSteps().get(pf.getSteps().size() - i);
        ProofStepModal<T> log2 = pf.getSteps().get(pf.getSteps().size() - i - 1);

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

        T leftState = ((LessEqual<T>) log2.getStep()).getLeft();
        T rightState = ((LessEqual<T>) log2.getStep()).getRight();

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

    private boolean isNotFresh(ModalNaturalDeduction<T> pf, int i) {
        if ((pf.getSteps().get(pf.getSteps().size() - 1).getStep()) instanceof ModalLogicalOperation) {
            T last = (pf.getSteps().get(pf.getSteps().size() - 1)).getState();
            return pf.stateIsUsedBefore(last, i);
        } else {
            RelationOperation<T> operation = (RelationOperation<T>) (pf.getSteps().get(pf.getSteps().size() - 1).getStep());
            return pf.stateIsUsedBefore(operation.getLeft(), i) &&
                    pf.stateIsUsedBefore(operation.getRight(), i);
        }
    }

    public void applyStepSupplier(ModalNaturalDeduction<T> pf, ProofStepSupplier<ModalOperation, ProofStepModal<T>> supp) {
        ProofStepModal<T> psm = pf.getSteps().get(pf.getSteps().size() - 1);
        int i = RuleUtils.getToLastAssumption(pf, psm.getAssumptionLevel());
        List<Integer> lst = List.of(j, pf.getSteps().size() - i, pf.getSteps().size());
        pf.getSteps().add(supp.generateProofStep(psm.getAssumptionLevel() - 2, psm.getStep(), new ProofReason("<>E", lst)));
    }

    @Override
    public void apply(ModalNaturalDeduction<T> pf) {
        ProofStepModal<T> psm = pf.getSteps().get(pf.getSteps().size() - 1);
        applyStepSupplier(pf, ((assLevel, log, reason) -> new ProofStepModal<>(psm.getState(), assLevel, (ModalLogicalOperation) log, reason)));
    }
}