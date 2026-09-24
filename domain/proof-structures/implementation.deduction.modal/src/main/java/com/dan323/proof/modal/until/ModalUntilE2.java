package com.dan323.proof.modal.until;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.modal.NegationModal;
import com.dan323.expressions.modal.Until;
import com.dan323.proof.generic.proof.ProofStepSupplier;
import com.dan323.proof.modal.ModalAction;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.List;

/**
 * Until elimination, not yet: from {@code A U B} and {@code ¬B} in the same state {@code s}, derive {@code A} in
 * {@code s}. The state where {@code B} holds is not {@code s}, so it comes strictly after {@code s}, and {@code A}
 * holds in {@code s}.
 *
 * @param until    the line with {@code A U B}
 * @param negation the line with {@code ¬B}
 */
public record ModalUntilE2(int until, int negation) implements ModalAction {

    @Override
    public boolean isValid(ModalNaturalDeduction pf) {
        return UntilSteps.isFormula(pf, until, Until.class) && UntilSteps.isFormula(pf, negation, NegationModal.class)
                && UntilSteps.sameState(pf, until, negation)
                && UntilSteps.formula(pf, negation, NegationModal.class).getElement()
                .equals(UntilSteps.formula(pf, until, Until.class).getRight());
    }

    @Override
    public void applyStepSupplier(ModalNaturalDeduction pf, ProofStepSupplier<ModalOperation, ProofStepModal> supp) {
        var left = (ModalLogicalOperation) UntilSteps.formula(pf, until, Until.class).getLeft();
        UntilSteps.add(pf, supp, left, UntilSteps.UNTIL_E, List.of(until, negation));
    }

    @Override
    public void apply(ModalNaturalDeduction pf) {
        applyStepSupplier(pf, UntilSteps.inStateOf(pf, until));
    }
}
