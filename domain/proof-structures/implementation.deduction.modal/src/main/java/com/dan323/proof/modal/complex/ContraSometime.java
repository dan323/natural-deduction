package com.dan323.proof.modal.complex;

import com.dan323.expressions.modal.Always;
import com.dan323.expressions.modal.NegationModal;
import com.dan323.expressions.modal.Sometime;
import com.dan323.expressions.relation.LessEqual;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.modal.ModalAssume;
import com.dan323.proof.modal.ModalBoxE;
import com.dan323.proof.modal.ModalDiaE;
import com.dan323.proof.modal.ModalFI;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;

public class ContraSometime extends CompositionRule {

    private final int i;
    private final int j;

    public ContraSometime(int a, int b) {
        i = a;
        j = b;
    }


    @Override
    public boolean isValid(ModalNaturalDeduction pf) {
        return RuleUtils.isValidIndexAndProp(pf, i)
                && RuleUtils.isValidIndexAndProp(pf, j)
                && pf.getSteps().get(i - 1).getState().equals(pf.getSteps().get(j - 1).getState())
                && pf.getSteps().get(i - 1).getStep() instanceof Sometime some
                && pf.getSteps().get(j - 1).getStep() instanceof Always alw
                && alw.getElement() instanceof NegationModal neg
                && neg.getElement().equals(some.getElement());
    }

    @Override
    public void apply(ModalNaturalDeduction pf) {
        var k = pf.getSteps().size();
        String newState = pf.newState();
        String state = pf.getSteps().get(i - 1).getState();
        (new ModalAssume(new LessEqual(state, newState))).apply(pf);
        (new ModalAssume(((Sometime)pf.getSteps().get(i-1).getStep()).getElement(), newState)).apply(pf);
        (new ModalBoxE(j, k+1)).apply(pf);
        (new ModalFI(k+2,k+3)).apply(pf);
        (new ModalDiaE(i)).apply(pf);
    }
}
