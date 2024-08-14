package com.dan323.proof.modal;

import com.dan323.expressions.modal.ConstantModal;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.proof.generic.FI;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalFI extends FI<ModalOperation, ProofStepModal, ModalNaturalDeduction> implements ModalAction {

    public ModalFI(int i, int j) {
        super(i, j, () -> ConstantModal.FALSE);
    }

    @Override
    public boolean isValid(ModalNaturalDeduction pf) {
        return super.isValid(pf) && ModalAction.checkEqualState(pf, getPos(), getNeg());
    }

    @Override
    public void apply(ModalNaturalDeduction pf) {
        applyStepSupplier(pf, (assLevel, log, reason) -> new ProofStepModal(pf.getState0(), assLevel, (ModalLogicalOperation) log, reason));
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 7;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ModalFI modalFI && super.equals(modalFI);
    }
}
