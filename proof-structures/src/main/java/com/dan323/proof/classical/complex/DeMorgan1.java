package com.dan323.proof.classical.complex;

import com.dan323.expresions.clasical.*;
import com.dan323.proof.Proof;
import com.dan323.proof.classical.*;

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
    public boolean isValid(Proof pf) {
        return (i <= pf.getSteps().size()) && (pf.getSteps().get(i - 1).getStep() instanceof NegationClassic)
                && (((UnaryOperationClassic) pf.getSteps().get(i - 1).getStep()).getElement() instanceof DisjunctionClassic);
    }

    @Override
    public void apply(Proof pf) {
        ClassicalLogicOperation left = ((BinaryOperationClassic) ((UnaryOperationClassic) pf.getSteps().get(i - 1).getStep()).getElement()).getLeft();
        ClassicalLogicOperation right = ((BinaryOperationClassic) ((UnaryOperationClassic) pf.getSteps().get(i - 1).getStep()).getElement()).getRight();
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
