package com.dan323.proof.modal;

import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.expresions.modal.ModalOperation;
import com.dan323.expresions.modal.NegationModal;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.NotI;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalNotI<T> extends NotI<ModalOperation, ProofStepModal<T>> implements ModalAction<T> {

    public ModalNotI() {
        super(NegationModal::new);
    }

    @Override
    public void apply(Proof<ModalOperation, ProofStepModal<T>> pf) {
        applyStepSupplier(pf, ((assLevel, log, reason) ->
                new ProofStepModal<>((pf.getSteps().get(pf.getSteps().size() - Action.getToLastAssumption(pf, assLevel + 1) + 1)).getState(), assLevel, (ModalLogicalOperation) log, reason)));

    }
}
