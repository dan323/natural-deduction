package com.dan323.proof.modal.until;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.modal.Sometime;
import com.dan323.expressions.modal.Until;
import com.dan323.proof.generic.proof.ProofStepSupplier;
import com.dan323.proof.modal.ModalAction;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.List;

/**
 * Until elimination, eventually: from {@code A U B} in state {@code s}, derive {@code ◇B} in {@code s}. {@code U} is
 * the strong until: {@code B} does hold in some state reachable from {@code s}.
 *
 * @param until the line with {@code A U B}
 */
public record ModalUntilE1(int until) implements ModalAction {

    @Override
    public boolean isValid(ModalNaturalDeduction pf) {
        return UntilSteps.isFormula(pf, until, Until.class);
    }

    @Override
    public void applyStepSupplier(ModalNaturalDeduction pf, ProofStepSupplier<ModalOperation, ProofStepModal> supp) {
        var right = (ModalLogicalOperation) UntilSteps.formula(pf, until, Until.class).getRight();
        UntilSteps.add(pf, supp, new Sometime(right), UntilSteps.UNTIL_E, List.of(until));
    }

    @Override
    public void apply(ModalNaturalDeduction pf) {
        applyStepSupplier(pf, UntilSteps.inStateOf(pf, until));
    }
}
