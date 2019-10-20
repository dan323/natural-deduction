package com.dan323.proof.modal;

import com.dan323.expresions.modal.ModalLogicalExpression;
import com.dan323.expresions.modal.NegationModal;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.NotI;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalNotI extends NotI implements ModalAction {

    public ModalNotI() {
        super(lo -> new NegationModal((ModalLogicalExpression) lo));
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof ModalNotI) && super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 5;
    }

    @Override
    public void apply(Proof pf) {
        applyStepSupplier(pf, ((assLevel, log, reason) ->
                new ProofStepModal(((ProofStepModal) pf.getSteps().get(pf.getSteps().size() -
                        Action.getToLastAssumption(pf, assLevel + 1) + 1)).getState(), assLevel, log, reason)));

    }
}
