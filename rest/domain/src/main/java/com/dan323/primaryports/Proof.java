package com.dan323.primaryports;

import com.dan323.proof.generic.bean.NaturalDeductionFactory;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author danco
 */
public record Proof<T extends Serializable>(List<Step<T>> steps, String logic, String goal) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1524625345L;

    public void addStep(Step<T> step) {
        this.steps().add(step);
    }

    public com.dan323.proof.generic.proof.Proof<?,?> toInternalProof(NaturalDeductionFactory<?,?,?> naturalDeductionFactory){
        com.dan323.proof.generic.proof.Proof<?,?> proof = naturalDeductionFactory.get();
        proof.initializeProof(assms, goal);
    }
}
