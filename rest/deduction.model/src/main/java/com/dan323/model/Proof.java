package com.dan323.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author danco
 */
public class Proof {

    private List<Rule> steps;

    public Proof() {
        steps = new ArrayList<>();
    }

    public void addStep(Rule rule) {
        steps.add(rule);
    }

    public List<Rule> getProof() {
        return steps;
    }
}
