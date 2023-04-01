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

    @Override
    public void apply(ModalNaturalDeduction pf) {
        String newState = pf.newState();
        NegationModal operation = (NegationModal) pf.getSteps().get(i - 1).getStep();
        (new ModalAssume(new LessEqual(pf.getSteps().get(i - 1).getState(), newState))).apply(pf);
        (new ModalAssume(operation.getElement(), newState)).apply(pf);
        (new ModalDiaI(i + 1, i + 2)).apply(pf);
        (new ModalFI(i + 3, i)).apply(pf);
        (new ModalNotI()).apply(pf);
        (new ModalBoxI()).apply(pf);
    }
}
