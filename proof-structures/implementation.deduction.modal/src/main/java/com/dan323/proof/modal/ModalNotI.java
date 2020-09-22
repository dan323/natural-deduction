package com.dan323.proof.modal;

import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.expresions.modal.ModalOperation;
import com.dan323.expresions.modal.NegationModal;
import com.dan323.proof.generic.NotI;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalNotI<T> extends NotI<ModalOperation, ProofStepModal<T>, ModalNaturalDeduction<T>> implements ModalAction<T> {

    public ModalNotI() {
        super(NegationModal::new);
    }

    @Override
    public void apply(ModalNaturalDeduction<T> pf) {
        applyStepSupplier(pf, ((assLevel, log, reason) ->
                new ProofStepModal<>((pf.getSteps().get(pf.getSteps().size() - RuleUtils.getToLastAssumption(pf, assLevel + 1) + 1)).getState(), assLevel, (ModalLogicalOperation) log, reason)));
    }
}
