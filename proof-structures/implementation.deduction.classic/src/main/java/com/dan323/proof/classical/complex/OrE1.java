package com.dan323.proof.classical.complex;

import com.dan323.expresions.base.BinaryOperation;
import com.dan323.expresions.base.Disjunction;
import com.dan323.expresions.base.Negation;
import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.expresions.classical.NegationClassic;
import com.dan323.proof.classical.ClassicAssume;
import com.dan323.proof.classical.ClassicDeductionTheorem;
import com.dan323.proof.classical.ClassicFE;
import com.dan323.proof.classical.ClassicFI;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;

public final class OrE1 extends CompositionRule {

    private int a;
    private int b;

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
    public boolean isValid(Proof<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>> pf) {
        if (RuleUtils.isValidIndexAndProp(pf, a) && RuleUtils.isValidIndexAndProp(pf, b) && RuleUtils.isOperation(pf, a, Disjunction.class) && RuleUtils.isOperation(pf, b, Negation.class)) {
            return ((BinaryOperation<ClassicalLogicOperation>) pf.getSteps().get(a - 1).getStep()).getLeft().equals(((NegationClassic) pf.getSteps().get(b - 1).getStep()).getElement());
        }
        return false;
    }

    @Override
    public void apply(Proof<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>> pf) {
        (new ClassicAssume(((BinaryOperation<ClassicalLogicOperation>) pf.getSteps().get(a - 1).getStep()).getLeft())).apply(pf);
        (new ClassicFI(pf.getSteps().size(), a)).apply(pf);
        (new ClassicFE(pf.getSteps().size(), ((BinaryOperation<ClassicalLogicOperation>) pf.getSteps().get(a - 1).getStep()).getRight())).apply(pf);
        (new ClassicDeductionTheorem()).apply(pf);
        (new ClassicAssume(((BinaryOperation<ClassicalLogicOperation>) pf.getSteps().get(a - 1).getStep()).getRight())).apply(pf);
        (new ClassicDeductionTheorem()).apply(pf);
    }
}