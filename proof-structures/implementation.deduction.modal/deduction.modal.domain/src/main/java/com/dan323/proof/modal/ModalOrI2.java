package com.dan323.proof.modal;

import com.dan323.expressions.modal.DisjunctionModal;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.proof.generic.OrI;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalOrI2<T> extends OrI<ModalOperation, ProofStepModal<T>, ModalNaturalDeduction<T>> implements ModalAction<T> {

    public ModalOrI2(int i, ModalLogicalOperation intro) {
        super(i, intro, (l1, l2) -> new DisjunctionModal(l2, l1));
    }

    @Override
    public void apply(ModalNaturalDeduction<T> pf) {
        applyStepSupplier(pf, ((assLevel, log, reason) ->
                new ProofStepModal<>(pf.getSteps().get(getAt() - 1).getState(), assLevel, (ModalLogicalOperation) log, reason)));
    }
}
