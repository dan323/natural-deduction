package com.dan323.proof.classical.complex;

import com.dan323.expresions.clasical.BinaryOperationClassic;
import com.dan323.expresions.clasical.NegationClassic;
import com.dan323.expresions.util.Disjunction;
import com.dan323.expresions.util.Negation;
import com.dan323.proof.Proof;
import com.dan323.proof.classical.ClassicAssume;
import com.dan323.proof.classical.ClassicDeductionTheorem;
import com.dan323.proof.classical.ClassicFE;
import com.dan323.proof.classical.ClassicFI;
import com.dan323.proof.generic.RuleUtils;

public final class OrE2 extends CompositionRule {

    private int a;
    private int b;

    public OrE2(int i, int j) {
        a = i;
        b = j;
    }

    public boolean equals(Object ob) {
        if (ob instanceof OrE2) {
            return ((OrE2) ob).a == a && ((OrE2) ob).b == b;
        }
        return false;
    }

    public int hashCode() {
        return super.hashCode() * a + b;
    }

    @Override
    public boolean isValid(Proof pf) {
        if (RuleUtils.isValidIndexAndProp(pf, a) && RuleUtils.isValidIndexAndProp(pf, b) && RuleUtils.isOperation(pf, a, Disjunction.class) && RuleUtils.isOperation(pf, b, Negation.class)) {
            return ((BinaryOperationClassic) pf.getSteps().get(a - 1).getStep()).getRight().equals(((NegationClassic) pf.getSteps().get(b - 1).getStep()).getElement());
        }
        return false;
    }

    @Override
    public void apply(Proof pf) {
        (new ClassicAssume(((BinaryOperationClassic) pf.getSteps().get(a - 1).getStep()).getRight())).apply(pf);
        (new ClassicFI(pf.getSteps().size(), a)).apply(pf);
        (new ClassicFE(pf.getSteps().size(), ((BinaryOperationClassic) pf.getSteps().get(a - 1).getStep()).getLeft())).apply(pf);
        (new ClassicDeductionTheorem()).apply(pf);
        (new ClassicAssume(((BinaryOperationClassic) pf.getSteps().get(a - 1).getStep()).getLeft())).apply(pf);
        (new ClassicDeductionTheorem()).apply(pf);
    }

}