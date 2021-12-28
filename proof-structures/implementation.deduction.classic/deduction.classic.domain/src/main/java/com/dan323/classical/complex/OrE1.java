package com.dan323.classical.complex;

import com.dan323.classical.ClassicAssume;
import com.dan323.classical.ClassicDeductionTheorem;
import com.dan323.classical.ClassicFE;
import com.dan323.classical.ClassicFI;
import com.dan323.classical.ClassicOrE;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.expressions.base.BinaryOperation;
import com.dan323.expressions.base.Disjunction;
import com.dan323.expressions.base.Negation;
import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.expressions.classical.NegationClassic;
import com.dan323.proof.generic.RuleUtils;

public final class OrE1 extends CompositionRule {

    private final int a;
    private final int b;

    public OrE1(int i, int j) {
        a = i;
        b = j;
    }

    public boolean equals(Object ob) {
        if (ob instanceof OrE1) {
            return ((OrE1) ob).a == a && ((OrE1) ob).b == b;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode() * a + b;
    }

    @Override
    public boolean isValid(NaturalDeduction pf) {
        if (RuleUtils.isValidIndexAndProp(pf, a) && RuleUtils.isValidIndexAndProp(pf, b) && RuleUtils.isOperation(pf, a, Disjunction.class) && RuleUtils.isOperation(pf, b, Negation.class)) {
            return ((BinaryOperation<?>) pf.getSteps().get(a - 1).getStep()).getLeft().equals(((NegationClassic) pf.getSteps().get(b - 1).getStep()).getElement());
        }
        return false;
    }

    @Override
    public void apply(NaturalDeduction pf) {
        (new ClassicAssume(((BinaryOperation<ClassicalLogicOperation>) pf.getSteps().get(a - 1).getStep()).getLeft())).apply(pf);
        (new ClassicFI(pf.getSteps().size(), b)).apply(pf);
        (new ClassicFE(pf.getSteps().size(), ((BinaryOperation<ClassicalLogicOperation>) pf.getSteps().get(a - 1).getStep()).getRight())).apply(pf);
        (new ClassicDeductionTheorem()).apply(pf);
        (new ClassicAssume(((BinaryOperation<ClassicalLogicOperation>) pf.getSteps().get(a - 1).getStep()).getRight())).apply(pf);
        (new ClassicDeductionTheorem()).apply(pf);
        (new ClassicOrE(a, pf.getSteps().size() - 2, pf.getSteps().size())).apply(pf);
    }
}