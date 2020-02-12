package com.dan323.proof.modal;

import com.dan323.expresions.base.UnaryOperation;
import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.expresions.modal.ModalOperation;
import com.dan323.expresions.modal.Sometime;
import com.dan323.expresions.relation.LessEqual;
import com.dan323.expresions.relation.RelationOperation;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStepSupplier;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.ArrayList;
import java.util.List;

public final class ModalDiaE<T> implements ModalAction<T> {

    private int j;
    private T state0;

    public ModalDiaE(int i, T state0) {
        j = i;
        this.state0 = state0;
    }

    @Override
    public boolean isValid(Proof<ModalOperation, ProofStepModal<T>> pf) {
        if (!(!pf.getSteps().isEmpty() && RuleUtils.isValidIndexAndProp(pf, j) && RuleUtils.isOperation(pf, j, Sometime.class))) {
            return false;
        }
        T origin = (pf.getSteps().get(j - 1)).getState();
        int assLevel = Action.getLastAssumptionLevel(pf);
        if (assLevel < 2) {
            return false;
        }
        int i = Action.getToLastAssumption(pf, assLevel);
        ProofStepModal<T> log1 = pf.getSteps().get(pf.getSteps().size() - i);
        ProofStepModal<T> log2 = pf.getSteps().get(pf.getSteps().size() - i - 1);

        if (!(log2.getStep() instanceof LessEqual)) {
            if (log1.getStep() instanceof LessEqual) {
                log2 = log1;
                log1 = pf.getSteps().get(pf.getSteps().size() - i - 1);
            } else {
                return false;
            }
        }

        T leftState = ((LessEqual<T>) log2.getStep()).getLeft();
        T rightState = ((LessEqual<T>) log2.getStep()).getRight();

        if (!(log1.getState().equals(rightState) && origin.equals(leftState))) {
            return false;
        }
        if ((pf.getSteps().get(pf.getSteps().size() - 1).getStep()) instanceof ModalLogicalOperation) {
            T last = (pf.getSteps().get(pf.getSteps().size() - 1)).getState();
            if (!ModalNaturalDeduction.isFresh(pf, last, pf.getSteps().size() - i, state0)) {
                return false;
            }
        } else {
            RelationOperation<T> operation = (RelationOperation<T>) (pf.getSteps().get(pf.getSteps().size() - 1).getStep());
            if (!ModalNaturalDeduction.isFresh(pf, operation.getLeft(), pf.getSteps().size() - i, state0) ||
                    !ModalNaturalDeduction.isFresh(pf, operation.getRight(), pf.getSteps().size() - i, state0)) {
                return false;
            }
        }

        return log1.getStep().equals(((UnaryOperation<?>) pf.getSteps().get(j - 1).getStep()).getElement());
    }

    public void applyStepSupplier(Proof<ModalOperation, ProofStepModal<T>> pf, ProofStepSupplier<ModalOperation, ProofStepModal<T>> supp) {
        ProofStepModal<T> psm = pf.getSteps().get(pf.getSteps().size() - 1);
        List<Integer> lst = new ArrayList<>();
        int i = Action.getToLastAssumption(pf, psm.getAssumptionLevel());
        lst.add(j);
        lst.add(pf.getSteps().size() - i + 1);
        lst.add(pf.getSteps().size());
        pf.getSteps().add(supp.generateProofStep(psm.getAssumptionLevel() - 2, psm.getStep(), new ProofReason("<>E", lst)));
    }

    @Override
    public void apply(Proof<ModalOperation, ProofStepModal<T>> pf) {
        ProofStepModal<T> psm = pf.getSteps().get(pf.getSteps().size() - 1);
        applyStepSupplier(pf, ((assLevel, log, reason) -> new ProofStepModal<>(psm.getState(), assLevel, (ModalLogicalOperation) log, reason)));
    }
}
