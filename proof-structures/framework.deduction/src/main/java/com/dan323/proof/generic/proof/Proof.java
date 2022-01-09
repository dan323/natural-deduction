package com.dan323.proof.generic.proof;


import com.dan323.expressions.base.LogicOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Proof<T extends LogicOperation, A, G, Q extends ProofStep<T>> {

    private G goal;
    private final List<Q> steps = new ArrayList<>();

    public List<A> getAssms() {
        return steps.stream()
                .takeWhile(step -> step.getProof().getNameProof().contains("Ass"))
                .takeWhile(step -> step.getAssumptionLevel() == 0)
                .map(this::toAssms)
                .collect(Collectors.toList());
    }

    public abstract A toAssms(Q step);

    public void removeLastStep() {
        steps.remove(steps.size() - 1);
    }

    protected void setAssms(List<A> assms) {
        for (A lo : assms) {
            getSteps().add(toStep(lo));
        }
    }

    public abstract G toGoal(Q step);

    protected abstract Q toStep(A logicExpression);

    public boolean isDone() {
        if (goal == null) {
            return false;
        }
        return steps.stream()
                .filter(ProofStep::isValid)
                .filter(s -> s.getStep() != null && toGoal(s).equals(goal))
                .anyMatch(s -> s.getAssumptionLevel() == 0);
    }

    protected void initializeProofSteps() {
        steps.clear();
    }

    public abstract void initializeProof(List<A> assms, G goal);

    public G getGoal() {
        return goal;
    }

    public void setGoal(G goal) {
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
