package com.dan323.proof.modal;

import com.dan323.expresions.modal.ModalLogicalExpression;
import com.dan323.proof.Proof;
import com.dan323.proof.generic.FE;
import com.dan323.proof.modal.proof.ProofStepModal;

public class ModalFE extends FE implements ModalAction {

    private final String state;

    public ModalFE(int j, ModalLogicalExpression clo, String st) {
        super(clo, j);
        state = st;
    }

    @Override
    public void apply(Proof pf) {
        applyAbstract(pf, ((assLevel, log, reason) -> new ProofStepModal(state, assLevel, log, reason)));
    }
}
