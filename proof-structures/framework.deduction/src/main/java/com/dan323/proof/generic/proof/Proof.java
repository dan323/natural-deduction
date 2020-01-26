package com.dan323.proof.generic.proof;


import com.dan323.expresions.base.LogicOperation;
import com.dan323.proof.generic.Action;

import java.util.ArrayList;
import java.util.List;

public abstract class Proof<T extends LogicOperation, Q extends ProofStep<T>> {

    private List<T> assms;
    private T goal;
    private List<Q> steps = new ArrayList<>();

    public List<T> getAssms() {
        return assms;
    }

    public void removeLastStep(){
        steps.remove(steps.size()-1);
    }

    protected void setAssms(List<T> assms) {
        this.assms = assms;
    }

    public abstract boolean isValid(Action<T,Q> act);

    public boolean isDone() {
        for (Q st : steps) {
            if (goal.equals(st.getStep()) && st.getAssumptionLevel() == 0) {
                return true;
            }
        }
        return false;
    }

    protected void initializeProofSteps() {
        steps = new ArrayList<>();
    }

    public abstract void initializeProof(List<T> assms, T goal);

    public LogicOperation getGoal() {
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
        if (steps != null) {
            for (Q ps : steps) {
                sb.append(ps.toString()).append("\n");
            }
        }
        return sb.toString();
    }
}
