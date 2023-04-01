package com.dan323.proof.modal;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.modal.NegationModal;
import com.dan323.proof.generic.NotI;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalNotI extends NotI<ModalOperation, ProofStepModal, ModalNaturalDeduction> implements ModalAction {

    public ModalNotI() {
        super(t -> new NegationModal((ModalLogicalOperation) t));
    }

    @Override
    public void apply(ModalNaturalDeduction pf) {
        applyStepSupplier(pf, ((assLevel, log, reason) ->
                new ProofStepModal((pf.getSteps().get(pf.getSteps().size() - RuleUtils.getToLastAssumption(pf, assLevel + 1) + 1)).getState(), assLevel, (ModalLogicalOperation) log, reason)));
    }
}
