package com.dan323.proof.modal.complex;

import com.dan323.expressions.modal.ModalOperation;
import com.dan323.proof.generic.proof.ProofStepSupplier;
import com.dan323.proof.modal.ModalAction;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

/**
 * @author danco
 */
public abstract class CompositionRule implements ModalAction {

    @Override
    public void applyStepSupplier(ModalNaturalDeduction pf, ProofStepSupplier<ModalOperation, ProofStepModal> supp) {
        throw new UnsupportedOperationException("This rule is a composition, hence no abstraction");
    }
}
