package com.dan323.proof.generic;

import com.dan323.expresions.base.BinaryOperation;
import com.dan323.expresions.base.Disjunction;
import com.dan323.expresions.base.Implication;
import com.dan323.expresions.base.LogicOperation;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofStepSupplier;

import java.util.List;
import java.util.Objects;

/**
 * @author danco
 */
public abstract class OrE<T extends LogicOperation, Q extends ProofStep<T>, P extends Proof<T, Q, ?>> implements AbstractAction<T, Q, P> {

    private final int disj;
    private final int rule1;
    private final int rule2;

    public OrE(int dis, int r1, int r2) {
        disj = dis;
        rule1 = r1;
        rule2 = r2;
    }

    protected int getDisj() {
        return disj;
    }

    protected int get1() {
        return rule1;
    }

    protected int get2() {
        return rule2;
    }

    @Override
    public boolean isValid(P pf) {
        if (RuleUtils.isValidIndexAndProp(pf, disj) && RuleUtils.isValidIndexAndProp(pf, rule1) && RuleUtils.isValidIndexAndProp(pf, rule2)) {
            LogicOperation dis = pf.getSteps().get(disj - 1).getStep();
            LogicOperation r1 = pf.getSteps().get(rule1 - 1).getStep();
            LogicOperation r2 = pf.getSteps().get(rule2 - 1).getStep();
            if ((dis instanceof Disjunction) && (r1 instanceof Implication) && (r2 instanceof Implication)) {
                LogicOperation left = ((BinaryOperation<?>) dis).getLeft();
                LogicOperation right = ((BinaryOperation<?>) dis).getRight();
                if (((BinaryOperation<?>) r1).getLeft().equals(left) && ((BinaryOperation<?>) r2).getLeft().equals(right)) {
                    return ((BinaryOperation<?>) r1).getRight().equals(((BinaryOperation<?>) r2).getRight());
                }
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OrE)) {
            return false;
        }
        OrE<?, ?, ?> orE = (OrE<?, ?, ?>) o;
        return disj == orE.disj &&
                rule1 == orE.rule1 &&
                rule2 == orE.rule2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(disj, rule1, rule2);
    }

    @Override
    public void applyStepSupplier(P pf, ProofStepSupplier<T, Q> supp) {
        T sol = ((BinaryOperation<T>) pf.getSteps().get(rule1 - 1).getStep()).getRight();
        int assLevel = pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel();
        pf.getSteps().add(supp.generateProofStep(assLevel, sol, new ProofReason("|E", List.of(disj, rule1, rule2))));
    }
}
