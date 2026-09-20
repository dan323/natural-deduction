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

    public ProofDto {
        steps = steps == null ? List.of() : steps;
    }

    /**
     * Whether the goal is proved: some step at the top level (no open assumption) is the goal. This is the rule of
     * {@code Proof.isDone()} of the domain, the steps of a {@code ProofDto} being the valid ones that the domain
     * proof produced. Serialized as {@code done}.
     */
    public boolean isDone() {
        return steps.stream().anyMatch(step -> step.assmsLevel() == 0 && step.expression().equals(goal));
    }
}
