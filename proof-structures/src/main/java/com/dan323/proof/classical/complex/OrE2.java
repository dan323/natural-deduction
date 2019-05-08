package com.dan323.proof.classical.complex;

import com.dan323.expresions.clasical.BinaryOperationClassic;
import com.dan323.expresions.clasical.DisjunctionClassic;
import com.dan323.expresions.clasical.NegationClassic;
import com.dan323.proof.Proof;
import com.dan323.proof.classical.ClassicAssume;
import com.dan323.proof.classical.ClassicDeductionTheorem;
import com.dan323.proof.classical.ClassicFE;
import com.dan323.proof.classical.ClassicFI;

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
        if (a <= pf.getSteps().size() && b <= pf.getSteps().size() && pf.getSteps().get(a - 1).isValid() && (pf.getSteps().get(b - 1).isValid() && pf.getSteps().get(a - 1).getStep() instanceof DisjunctionClassic) && pf.getSteps().get(b - 1).getStep() instanceof NegationClassic) {
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
