package com.dan323.proof.modal;

import com.dan323.expresions.modal.ModalLogicalExpression;
import com.dan323.proof.Proof;
import com.dan323.proof.generic.Assume;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalAssume extends Assume implements ModalAction {

    private final String state;

    public ModalAssume(ModalLogicalExpression clo, String st) {
        super(clo);
        state = st;
    }

    @Override
    public void apply(Proof pf) {
        applyAbstract(pf, ((assLevel, log, reason) -> new ProofStepModal(state, assLevel, log, reason)));
    }
}
