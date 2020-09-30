package com.dan323.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author danco
 */
public class Proof<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = 1524625345L;
    private final List<Step<T>> steps;
    private String logic;
    private String goal;

    public Proof() {
        steps = new ArrayList<>();
    }

    public void addStep(Step<T> step) {
        steps.add(step);
    }

    public List<Step<T>> getSteps() {
        return steps;
    }

    public String getLogic() {
        return logic;
    }

    public void setLogic(String logic) {
        this.logic = logic;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }
}
