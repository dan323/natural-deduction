package com.dan323.proof.modal.complex;

import com.dan323.expressions.base.BinaryOperation;
import com.dan323.expressions.base.Negation;
import com.dan323.expressions.base.UnaryOperation;
import com.dan323.expressions.modal.DisjunctionModal;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.modal.*;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;

public final class ModalDeMorgan extends CompositionRule {

    private final int i;

    public ModalDeMorgan(int j) {
        i = j;
    }

    public boolean equals(Object ob) {
        return (ob instanceof ModalDeMorgan morgan) && morgan.i == i;
    }

    public int hashCode() {
        return i * 23;
    }

    @Override
    public boolean isValid(ModalNaturalDeduction pf) {
        return RuleUtils.isValidIndexAndProp(pf, i) &&
                RuleUtils.isOperation(pf, i, Negation.class) &&
                (((UnaryOperation<?>) pf.getSteps().get(i - 1).getStep()).getElement() instanceof DisjunctionModal);
    }

    @Override
    public void apply(ModalNaturalDeduction pf) {
        BinaryOperation<ModalLogicalOperation> operation = (BinaryOperation<ModalLogicalOperation>) ((UnaryOperation<?>) pf.getSteps().get(i - 1).getStep()).getElement();
        ModalLogicalOperation left = operation.getLeft();
        ModalLogicalOperation right = operation.getRight();
        String state = pf.getSteps().get(i - 1).getState();
        (new ModalAssume(left, state)).apply(pf);
        (new ModalOrI1(pf.getSteps().size(), right)).apply(pf);
        (new ModalFI(pf.getSteps().size(), i)).apply(pf);
        (new ModalNotI()).apply(pf);
        (new ModalAssume(right, state)).apply(pf);
        (new ModalOrI2(pf.getSteps().size(), left)).apply(pf);
        (new ModalFI(pf.getSteps().size(), i)).apply(pf);
        (new ModalNotI()).apply(pf);
    }
}
