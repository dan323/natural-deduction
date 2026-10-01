package com.dan323.proof.modal.complex;

import com.dan323.expressions.base.Negation;
import com.dan323.expressions.modal.NegationModal;
import com.dan323.expressions.modal.Sometime;
import com.dan323.expressions.relation.LessEqual;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.modal.*;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;

public final class DeMorgan extends CompositionRule {


    private final int i;

    public DeMorgan(int j) {
        i = j;
    }

    public boolean equals(Object ob) {
        return (ob instanceof DeMorgan morgan) && morgan.i == i;
    }

    public int hashCode() {
        return i * 31;
    }

    @Override
    public boolean isValid(ModalNaturalDeduction pf) {
        return RuleUtils.isValidIndexAndProp(pf, i) &&
                RuleUtils.isOperation(pf, i, Negation.class) &&
                (((Negation<?>) pf.getSteps().get(i - 1).getStep()).getElement() instanceof Sometime);
    }

    /**
     * From {@code - (<> A)} it derives {@code [] (- A)}, or {@code [] B} when {@code A} is {@code - B}: the
     * {@code - (- B)} is eliminated with {@code -E} before {@code []I}, so the result has no double negation.
     */
    @Override
    public void apply(ModalNaturalDeduction pf) {
        int k = pf.getSteps().size();
        String newState = pf.newState();
        NegationModal operation = (NegationModal) pf.getSteps().get(i - 1).getStep();
        var element = ((Sometime) operation.getElement()).getElement();
        (new ModalAssume(new LessEqual(pf.getSteps().get(i - 1).getState(), newState))).apply(pf);
        (new ModalAssume(element, newState)).apply(pf);
        (new ModalDiaI(k + 2, k + 1)).apply(pf);
        (new ModalFI(k + 3, i)).apply(pf);
        (new ModalNotI()).apply(pf);
        if (element instanceof NegationModal) {
            (new ModalNotE(pf.getSteps().size())).apply(pf);
        }
        (new ModalBoxI()).apply(pf);
    }
}
