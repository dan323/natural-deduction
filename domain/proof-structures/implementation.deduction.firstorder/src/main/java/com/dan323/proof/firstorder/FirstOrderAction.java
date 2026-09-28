package com.dan323.proof.firstorder;

import com.dan323.expressions.firstorder.FirstOrderOperation;
import com.dan323.proof.firstorder.proof.FirstOrderNaturalDeduction;
import com.dan323.proof.generic.AbstractAction;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.proof.ProofStep;

/**
 * A rule of first-order natural deduction. Its steps are plain {@link ProofStep}s, so a proof prints in the usual text
 * layout, without the state prefix of modal proofs.
 */
public interface FirstOrderAction extends Action<FirstOrderOperation, ProofStep<FirstOrderOperation>, FirstOrderNaturalDeduction>,
        AbstractAction<FirstOrderOperation, ProofStep<FirstOrderOperation>, FirstOrderNaturalDeduction> {

    @Override
    default void apply(FirstOrderNaturalDeduction pf) {
        applyStepSupplier(pf, ProofStep::new);
    }
}
