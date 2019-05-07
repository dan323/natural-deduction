package com.dan323.proof.generic;

import com.dan323.expresions.LogicOperation;
import com.dan323.expresions.util.BinaryOperation;
import com.dan323.expresions.util.Disjunction;
import com.dan323.expresions.util.Implication;
import com.dan323.proof.Proof;
import com.dan323.proof.ProofReason;

import java.util.ArrayList;
import java.util.List;

/**
 * @author danco
 */
public class OrE implements AbstractAction {

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
    public boolean isValid(Proof pf) {
        if (RuleUtils.isValidIndexAndProp(pf, disj) && RuleUtils.isValidIndexAndProp(pf, rule1) && RuleUtils.isValidIndexAndProp(pf, rule2)) {
            LogicOperation dis = pf.getSteps().get(disj - 1).getStep();
            LogicOperation r1 = pf.getSteps().get(rule1 - 1).getStep();
            LogicOperation r2 = pf.getSteps().get(rule2 - 1).getStep();
            if ((dis instanceof Disjunction) && (r1 instanceof Implication) && (r2 instanceof Implication)) {
                LogicOperation left = ((BinaryOperation) dis).getLeft();
                LogicOperation right = ((BinaryOperation) dis).getRight();
                if (((BinaryOperation) r1).getLeft().equals(left) && ((BinaryOperation) r2).getLeft().equals(right)) {
                    return ((BinaryOperation) r1).getRight().equals(((BinaryOperation) r2).getRight());
                }
            }
        }
        return false;
    }

    @Override
    public void applyAbstract(Proof pf, ProofStepSupplier supp) {
        LogicOperation sol = ((BinaryOperation) pf.getSteps().get(rule1 - 1).getStep()).getRight();
        int assLevel = 0;
        if (!pf.getSteps().isEmpty()) {
            assLevel = pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel();
        }
        List<Integer> lst = new ArrayList<>();
        lst.add(disj);
        lst.add(rule1);
        lst.add(rule2);
        pf.getSteps().add(supp.generateProofStep(assLevel, sol, new ProofReason("|E", lst)));
    }
}
