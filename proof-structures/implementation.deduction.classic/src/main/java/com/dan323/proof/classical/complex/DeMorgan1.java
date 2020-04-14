package com.dan323.proof.classical.complex;

import com.dan323.expresions.base.BinaryOperation;
import com.dan323.expresions.base.Negation;
import com.dan323.expresions.base.UnaryOperation;
import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.expresions.classical.DisjunctionClassic;
import com.dan323.proof.classical.*;
import com.dan323.proof.classical.proof.NaturalDeduction;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;

public final class DeMorgan1 extends CompositionRule {

    private int i;

    public DeMorgan1(int j) {
        i = j;
    }

    public boolean equals(Object ob) {
        return (ob instanceof DeMorgan1) && ((DeMorgan1) ob).i == i;
    }

    public int hashCode() {
        return i * 23;
    }

    @Override
    public boolean isValid(NaturalDeduction pf) {
        return RuleUtils.isValidIndexAndProp(pf, i) && RuleUtils.isOperation(pf, i, Negation.class)
                && (((UnaryOperation<?>) pf.getSteps().get(i - 1).getStep()).getElement() instanceof DisjunctionClassic);
    }

    @Override
    public void apply(NaturalDeduction pf) {
        ClassicalLogicOperation left = ((BinaryOperation<ClassicalLogicOperation>) ((UnaryOperation<ClassicalLogicOperation>) pf.getSteps().get(i - 1).getStep()).getElement()).getLeft();
        ClassicalLogicOperation right = ((BinaryOperation<ClassicalLogicOperation>) ((UnaryOperation<ClassicalLogicOperation>) pf.getSteps().get(i - 1).getStep()).getElement()).getRight();
        (new ClassicAssume(left)).apply(pf);
        (new ClassicOrI1(pf.getSteps().size(), right)).apply(pf);
        (new ClassicFI(pf.getSteps().size(), i)).apply(pf);
        (new ClassicNotI()).apply(pf);
        (new ClassicAssume(right)).apply(pf);
        (new ClassicOrI2(pf.getSteps().size(), left)).apply(pf);
        (new ClassicFI(pf.getSteps().size(), i)).apply(pf);
        (new ClassicNotI()).apply(pf);
    }
}
