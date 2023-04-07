package com.dan323.proof.modal.complex;

import com.dan323.expressions.base.BinaryOperation;
import com.dan323.expressions.base.Disjunction;
import com.dan323.expressions.base.Negation;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.NegationModal;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.modal.*;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;

public final class ModalOrE1 extends CompositionRule {

    private final int a;
    private final int b;

    public ModalOrE1(int i, int j) {
        a = i;
        b = j;
    }

    public boolean equals(Object ob) {
        if (ob instanceof ModalOrE1 ore) {
            return ore.a == a && ore.b == b;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode() * a + b;
    }

    @Override
    public boolean isValid(ModalNaturalDeduction pf) {
        if (ModalAction.checkEqualState(pf, a, b) &&
                RuleUtils.isValidIndexAndProp(pf, a) && RuleUtils.isValidIndexAndProp(pf, b) && RuleUtils.isOperation(pf, a, Disjunction.class) && RuleUtils.isOperation(pf, b, Negation.class)) {
            return ((BinaryOperation<?>) pf.getSteps().get(a - 1).getStep()).getLeft().equals(((NegationModal) pf.getSteps().get(b - 1).getStep()).getElement());
        }
        return false;
    }

    @Override
    public void apply(ModalNaturalDeduction pf) {
        String state = pf.getSteps().get(a - 1).getState();
        (new ModalAssume(((BinaryOperation<ModalLogicalOperation>) pf.getSteps().get(a - 1).getStep()).getLeft(), state)).apply(pf);
        (new ModalFI(pf.getSteps().size(), b)).apply(pf);
        (new ModalFE(pf.getSteps().size(), ((BinaryOperation<ModalLogicalOperation>) pf.getSteps().get(a - 1).getStep()).getRight(), state)).apply(pf);
        (new ModalDeductionTheorem()).apply(pf);
        (new ModalAssume(((BinaryOperation<ModalLogicalOperation>) pf.getSteps().get(a - 1).getStep()).getRight(), state)).apply(pf);
        (new ModalDeductionTheorem()).apply(pf);
        (new ModalOrE(a, pf.getSteps().size() - 2, pf.getSteps().size())).apply(pf);
    }
}