package com.dan323.proof.modal;

import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.proof.generic.Assume;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalAssume extends Assume<ModalLogicalOperation, ProofStepModal> implements ModalAction {

    private final String state;

    public ModalAssume(ModalLogicalOperation clo, String st) {
        super(clo);
        state = st;
    }

    @Override
    public void apply(Proof<ModalLogicalOperation, ProofStepModal> pf) {
        applyStepSupplier(pf, ((assLevel, log, reason) -> new ProofStepModal(state, assLevel, log, reason)));
    }
}
