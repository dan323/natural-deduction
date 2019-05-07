package com.dan323.proof.modal;

import com.dan323.expresions.modal.ImplicationModal;
import com.dan323.expresions.modal.ModalLogicalExpression;
import com.dan323.proof.Proof;
import com.dan323.proof.generic.DeductionTheorem;
import com.dan323.proof.modal.proof.ProofStepModal;

public class ModalDeductionTheorem extends DeductionTheorem implements ModalAction {

    public ModalDeductionTheorem() {
        super((l1, l2) -> new ImplicationModal((ModalLogicalExpression) l1, (ModalLogicalExpression) l2));
    }

    @Override
    public boolean isValid(Proof pf) {
        if (super.isValid(pf)) {
            return ModalAction.checkEqualState(pf, pf.getSteps().size() - getLastAssumption() + 1, pf.getSteps().size());
        }
        return false;
    }

    @Override
    public void apply(Proof pf) {
        this.applyAbstract(pf, ((assLevel, log, reason) -> new ProofStepModal(((ProofStepModal) pf.getSteps().get(pf.getSteps().size() - 1)).getState(), assLevel, log, reason)));
    }

    @Override
    public boolean equals(Object ob) {
        return ob instanceof ModalDeductionTheorem;
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 19 * 21;
    }
}
