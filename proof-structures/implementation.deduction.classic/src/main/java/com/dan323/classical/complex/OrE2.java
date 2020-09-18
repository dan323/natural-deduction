package com.dan323.classical.complex;

import com.dan323.classical.ClassicAssume;
import com.dan323.classical.ClassicDeductionTheorem;
import com.dan323.classical.ClassicFE;
import com.dan323.classical.ClassicFI;
import com.dan323.classical.ClassicOrE;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.expresions.base.BinaryOperation;
import com.dan323.expresions.base.Disjunction;
import com.dan323.expresions.base.Negation;
import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.expresions.classical.NegationClassic;
import com.dan323.proof.generic.RuleUtils;

public final class OrE2 extends CompositionRule {

    private final int a;
    private final int b;

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
    public boolean isValid(NaturalDeduction pf) {
        if (RuleUtils.isValidIndexAndProp(pf, a) && RuleUtils.isValidIndexAndProp(pf, b) && RuleUtils.isOperation(pf, a, Disjunction.class) && RuleUtils.isOperation(pf, b, Negation.class)) {
            return ((BinaryOperation<?>) pf.getSteps().get(a - 1).getStep()).getRight().equals(((NegationClassic) pf.getSteps().get(b - 1).getStep()).getElement());
        }
        return false;
    }

    @Override
    public void apply(NaturalDeduction pf) {
        (new ClassicAssume(((BinaryOperation<ClassicalLogicOperation>) pf.getSteps().get(a - 1).getStep()).getRight())).apply(pf);
        (new ClassicFI(pf.getSteps().size(), a)).apply(pf);
        (new ClassicFE(pf.getSteps().size(), ((BinaryOperation<ClassicalLogicOperation>) pf.getSteps().get(a - 1).getStep()).getLeft())).apply(pf);
        (new ClassicDeductionTheorem()).apply(pf);
        (new ClassicAssume(((BinaryOperation<ClassicalLogicOperation>) pf.getSteps().get(a - 1).getStep()).getLeft())).apply(pf);
        (new ClassicDeductionTheorem()).apply(pf);
        (new ClassicOrE(a, pf.getSteps().size(), pf.getSteps().size() - 2)).apply(pf);
    }

}