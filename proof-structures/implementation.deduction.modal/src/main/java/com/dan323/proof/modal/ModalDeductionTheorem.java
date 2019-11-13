package com.dan323.proof.modal;

import com.dan323.expresions.modal.ImplicationModal;
import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.proof.generic.DeductionTheorem;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalDeductionTheorem extends DeductionTheorem<ModalLogicalOperation, ProofStepModal> implements ModalAction {

    public ModalDeductionTheorem() {
        super(ImplicationModal::new);
    }

    @Override
    public boolean isValid(Proof<ModalLogicalOperation, ProofStepModal> pf) {
        if (super.isValid(pf)) {
            return ModalAction.checkEqualState(pf, pf.getSteps().size() - getLastAssumption() + 1, pf.getSteps().size());
        }
        return false;
    }

    @Override
    public void apply(Proof<ModalLogicalOperation, ProofStepModal> pf) {
        applyStepSupplier(pf, ((assLevel, log, reason) -> new ProofStepModal((pf.getSteps().get(pf.getSteps().size() - 1)).getState(), assLevel, log, reason)));
    }
}
