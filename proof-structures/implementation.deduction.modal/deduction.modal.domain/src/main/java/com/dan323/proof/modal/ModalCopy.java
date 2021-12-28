package com.dan323.proof.modal;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.proof.generic.Copy;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.Objects;

public final class ModalCopy<T> extends Copy<ModalOperation, ProofStepModal<T>, ModalNaturalDeduction<T>> implements ModalAction<T> {

    public ModalCopy(int i) {
        super(i);
    }

    @Override
    public void apply(ModalNaturalDeduction<T> pf) {
        applyStepSupplier(pf, (assLevel, log, reason) -> new ProofStepModal<>(getState(pf, getAppliedAt()), assLevel, (ModalLogicalOperation) log, reason));
    }

    private T getState(ModalNaturalDeduction<T> pf, int k) {
        return (pf.getSteps().get(k - 1)).getState();
    }

    @Override
    public boolean equals(Object o){
        if (o instanceof ModalCopy){
            return getAppliedAt() == ((ModalCopy<?>)o).getAppliedAt();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode(){
        return Objects.hash(getClass(), getAppliedAt());
    }
}
