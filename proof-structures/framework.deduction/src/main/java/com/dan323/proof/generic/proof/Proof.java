package com.dan323.proof.generic.proof;


import com.dan323.expresions.base.LogicOperation;
import com.dan323.proof.generic.Action;

import java.util.ArrayList;
import java.util.List;

public abstract class Proof {

    private List<LogicOperation> assms;
    private LogicOperation goal;
    private List<ProofStep> steps = new ArrayList<>();

    public List<LogicOperation> getAssms() {
        return assms;
    }

    protected void setAssms(List<LogicOperation> assms) {
        this.assms = assms;
    }

    public abstract boolean isValid(Action act);

    public boolean isDone() {
        for (ProofStep st : steps) {
            if (goal.equals(st.getStep()) && st.getAssumptionLevel() == 0) {
                return true;
            }
        }
        return false;
    }

    protected void initializeProofSteps() {
        steps = new ArrayList<>();
    }

    public void initializeProof(List<LogicOperation> assms, LogicOperation goal) {
        setAssms(assms);
        initializeProofSteps();
        setGoal(goal);
        for (LogicOperation lo : assms) {
            steps.add(new ProofStep(0, lo, new ProofReason("Ass", new ArrayList<>())));
        }
    }

    public LogicOperation getGoal() {
        return goal;
    }

    protected void setGoal(LogicOperation goal) {
        this.goal = goal;
    }

    public List<ProofStep> getSteps() {
        return steps;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (steps != null) {
            for (ProofStep ps : steps) {
                sb.append(ps.toString()).append("\n");
            }
        }
        return sb.toString();
    }
}
