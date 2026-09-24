package com.dan323.uses;

import com.dan323.model.Difficulty;
import com.dan323.model.ExerciseDto;

import java.util.List;

/**
 * An exercise of a logic's catalog together with a reference {@code solution}: a complete proof in the proof-text
 * layout ({@link ProofParser.ProofLine}) whose leading top-level assumptions are the premises and whose last line is
 * the goal. The solution stays on the server; only {@link #toDto()} is sent to clients.
 */
public record Exercise(String id, String title, List<String> premises, String goal, Difficulty difficulty,
                       String solution) {

    public Exercise {
        premises = List.copyOf(premises);
    }

    public ExerciseDto toDto() {
        return new ExerciseDto(id, title, premises, goal, difficulty);
    }
}
