package com.dan323.proof.modal.relational;

import com.dan323.expressions.relation.RelationOperation;
import com.dan323.proof.generic.AbstractAction;
import com.dan323.proof.modal.AbstractModalAction;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

public abstract class RelationalAction implements AbstractModalAction {

    @Override
    public void apply(ModalNaturalDeduction pf) {
        applyStepSupplier(pf, (assLevel, log, reason) -> new ProofStepModal(assLevel, (RelationOperation) log, reason));
    }
}
