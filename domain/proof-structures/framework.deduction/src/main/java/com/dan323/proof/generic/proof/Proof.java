package com.dan323.proof.generic.proof;


import com.dan323.expressions.base.LogicOperation;
import com.dan323.proof.generic.Action;

import java.util.ArrayList;
import java.util.List;

public abstract class Proof<T extends LogicOperation, Q extends ProofStep<T>> {

    private List<T> assms = List.of();
    private T goal;
    private final List<Q> steps = new ArrayList<>();

    public List<T> getAssms() {
        return assms;
    }

    public abstract List<? extends Action<T,Q,? extends Proof<T,Q>>> parse();

    public void removeLastStep() {
        steps.removeLast();
    }

    public void reset(){
        initializeProof(assms, goal);
    }

    protected void setAssms(List<T> assms) {
        this.assms = assms;
        for (T lo : assms) {
            getSteps().add(generateAssm(lo));
        }
    }

    protected abstract Q generateAssm(T logicexpression);

    public abstract void automate();

    public boolean isDone() {
        if (goal == null) {
            return false;
        }
        return steps.stream()
                .filter(ProofStep::isValid)
                .filter(s -> s.getStep() != null && s.getStep().equals(goal))
                .anyMatch(s -> s.getAssumptionLevel() == 0);
    }

    protected void initializeProofSteps() {
        steps.clear();
    }

    public abstract void initializeProof(List<T> assms, T goal);

    public T getGoal() {
        return goal;
    }

    protected void setGoal(T goal) {
        this.goal = goal;
    }

    public List<Q> getSteps() {
        return steps;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Q ps : steps) {
            sb.append(ps.toString()).append("\n");
        }
        return sb.toString();
    }
}
