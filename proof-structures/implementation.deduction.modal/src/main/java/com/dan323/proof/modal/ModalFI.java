package com.dan323.proof.modal;

import com.dan323.expresions.modal.ConstantModal;
import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.expresions.modal.ModalOperation;
import com.dan323.proof.generic.FI;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalFI<T> extends FI<ModalOperation, ProofStepModal<T>, ModalNaturalDeduction<T>> implements ModalAction<T> {

    private final T state;

    public ModalFI(T state, int i, int j) {
        super(i, j, () -> ConstantModal.FALSE);
        this.state = state;
    }

    @Override
    public boolean isValid(ModalNaturalDeduction<T> pf) {
        return super.isValid(pf) && ModalAction.checkEqualState(pf, getPos(), getNeg());
    }

    @Override
    public void apply(ModalNaturalDeduction<T> pf) {
        applyStepSupplier(pf, (assLevel, log, reason) -> new ProofStepModal<>(state, assLevel, (ModalLogicalOperation) log, reason));
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 7 + state.hashCode() * 5;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && ((ModalFI<?>) obj).state.equals(state);
    }
}
