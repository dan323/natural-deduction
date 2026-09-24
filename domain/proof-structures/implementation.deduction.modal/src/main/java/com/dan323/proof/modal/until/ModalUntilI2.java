package com.dan323.proof.modal.until;

import com.dan323.expressions.modal.Always;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.modal.Sometime;
import com.dan323.expressions.modal.Until;
import com.dan323.proof.generic.proof.ProofStepSupplier;
import com.dan323.proof.modal.ModalAction;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.List;

/**
 * Until introduction, later: from {@code □A} and {@code ◇B} in the same state {@code s}, derive {@code A U B} in
 * {@code s}. {@code B} holds in some state reachable from {@code s}, and {@code A} in every state reachable from
 * {@code s}, so in particular in every one before it.
 *
 * @param always   the line with {@code □A}
 * @param sometime the line with {@code ◇B}
 */
public record ModalUntilI2(int always, int sometime) implements ModalAction {

    @Override
    public boolean isValid(ModalNaturalDeduction pf) {
        return UntilSteps.isFormula(pf, always, Always.class) && UntilSteps.isFormula(pf, sometime, Sometime.class)
                && UntilSteps.sameState(pf, always, sometime);
    }

    @Override
    public void applyStepSupplier(ModalNaturalDeduction pf, ProofStepSupplier<ModalOperation, ProofStepModal> supp) {
        var left = UntilSteps.formula(pf, always, Always.class).getElement();
        var right = UntilSteps.formula(pf, sometime, Sometime.class).getElement();
        UntilSteps.add(pf, supp, new Until(left, right), UntilSteps.UNTIL_I, List.of(always, sometime));
    }

    @Override
    public void apply(ModalNaturalDeduction pf) {
        applyStepSupplier(pf, UntilSteps.inStateOf(pf, always));
    }
}
