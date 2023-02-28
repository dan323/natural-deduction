package com.dan323.uses.modal;

import com.dan323.expressions.modal.ModalOperation;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;
import com.dan323.uses.LogicalSolver;

public class ModalSolver<T> implements LogicalSolver<ModalOperation, ProofStepModal<T>, ModalNaturalDeduction<T>> {
    @Override
    public ModalNaturalDeduction<T> perform(ModalNaturalDeduction<T> proof) {
        throw new UnsupportedOperationException("This is not yet implemented");
    }

    @Override
    public String getLogicName() {
        return "modal";
    }
}
