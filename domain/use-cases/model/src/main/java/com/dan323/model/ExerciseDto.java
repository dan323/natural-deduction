package com.dan323.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * An exercise a logic offers: prove {@code goal} from {@code premises}. The formulas are written in the logic's input
 * syntax, so a client can start the proof with them as they are. {@code id} is stable and unique within a logic.
 */
public record ExerciseDto(String id, String title, List<String> premises, String goal,
                          Difficulty difficulty) implements Serializable {

    @Serial
    private static final long serialVersionUID = 8190273645L;

    public ExerciseDto {
        premises = premises == null ? List.of() : List.copyOf(premises);
    }
}
