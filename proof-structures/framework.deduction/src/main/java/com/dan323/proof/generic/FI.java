package com.dan323.proof.generic;

import com.dan323.expresions.base.Constant;
import com.dan323.expresions.base.LogicOperation;
import com.dan323.expresions.base.Negation;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStepSupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author danco
 */
public abstract class FI implements AbstractAction {

    private final int neg;
    private final int pos;
    private final Function<LogicOperation, Negation> negationFunction;
    private final Supplier<Constant> constantFunction;

    public FI(int a, int b, Function<LogicOperation, Negation> negate, Supplier<Constant> genConst) {
        pos = a;
        neg = b;
        negationFunction = negate;
        constantFunction = genConst;
    }

    @Override
    public boolean isValid(Proof pf) {
        if (RuleUtils.isValidIndexAndProp(pf, neg) && RuleUtils.isValidIndexAndProp(pf, pos) && RuleUtils.isOperation(pf, neg, Negation.class)) {
            return negationFunction.apply(pf.getSteps().get(pos - 1).getStep()).equals(pf.getSteps().get(neg - 1).getStep());
        }
        return false;
    }

    protected int getPos() {
        return pos;
    }

    protected int getNeg() {
        return neg;
    }

    @Override
    public void applyStepSupplier(Proof pf, ProofStepSupplier supp) {
        int assLevel = 0;
        if (!pf.getSteps().isEmpty()) {
            assLevel = pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel();
        }
        List<Integer> lst = new ArrayList<>();
        lst.add(pos);
        lst.add(neg);
        pf.getSteps().add(supp.generateProofStep(assLevel, constantFunction.get(), new ProofReason("FI", lst)));
    }
}
