package com.dan323.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author danco
 */
public record ProofDto(List<StepDto> steps, String logic, String goal) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1524625345L;

    public boolean isDone() {
        var lastStep = steps.getLast();
        return lastStep.assmsLevel() == 0 && lastStep.expression().equals(goal);
    }
}
