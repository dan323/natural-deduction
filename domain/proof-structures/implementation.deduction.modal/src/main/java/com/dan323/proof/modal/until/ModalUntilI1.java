package com.dan323.proof.modal.until;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.modal.Until;
import com.dan323.proof.generic.proof.ProofStepSupplier;
import com.dan323.proof.modal.ModalAction;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.List;

/**
 * Until introduction, now: from {@code B} in state {@code s}, derive {@code A U B} in {@code s} for any {@code A}.
 * {@code B} already holds, so nothing has to hold before it.
 *
 * @param line the line with {@code B}
 * @param left the formula {@code A}
 */
public record ModalUntilI1(int line, ModalLogicalOperation left) implements ModalAction {

    @Override
    public boolean isValid(ModalNaturalDeduction pf) {
        return left != null && UntilSteps.isFormula(pf, line, ModalLogicalOperation.class);
    }

    @Override
    public void applyStepSupplier(ModalNaturalDeduction pf, ProofStepSupplier<ModalOperation, ProofStepModal> supp) {
        var right = UntilSteps.formula(pf, line, ModalLogicalOperation.class);
        UntilSteps.add(pf, supp, new Until(left, right), UntilSteps.UNTIL_I, List.of(line));
    }

    @Override
    public void apply(ModalNaturalDeduction pf) {
        applyStepSupplier(pf, UntilSteps.inStateOf(pf, line));
    }
}
