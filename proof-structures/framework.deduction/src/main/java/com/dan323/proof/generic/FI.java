package com.dan323.proof.generic;

import com.dan323.expresions.base.Constant;
import com.dan323.expresions.base.LogicOperation;
import com.dan323.expresions.base.Negation;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofStepSupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author danco
 */
public abstract class FI<T extends LogicOperation, Q extends ProofStep<T>> implements AbstractAction<T, Q> {

    private final int neg;
    private final int pos;
    private final Function<T, Negation<T>> negationFunction;
    private final Supplier<Constant<T>> constantFunction;

    public FI(int a, int b, Function<T, Negation<T>> negate, Supplier<Constant<T>> genConst) {
        pos = a;
        neg = b;
        negationFunction = negate;
        constantFunction = genConst;
    }

    @Override
    public boolean isValid(Proof<T, Q> pf) {
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
    public void applyStepSupplier(Proof<T, Q> pf, ProofStepSupplier<T, Q> supp) {
        int assLevel = 0;
        if (!pf.getSteps().isEmpty()) {
            assLevel = pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel();
        }
        List<Integer> lst = new ArrayList<>();
        lst.add(pos);
        lst.add(neg);
        pf.getSteps().add(supp.generateProofStep(assLevel, constantFunction.get().castToLanguage(), new ProofReason("FI", lst)));
    }
}
