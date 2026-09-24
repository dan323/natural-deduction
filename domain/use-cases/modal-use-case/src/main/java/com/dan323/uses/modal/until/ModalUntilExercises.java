package com.dan323.uses.modal.until;

import com.dan323.model.Difficulty;
import com.dan323.uses.Exercise;
import com.dan323.uses.LogicalExercises;

import java.util.List;

/**
 * The exercises of modal logic with Until, from easy to hard. The premises are in the initial state {@code s0}, and
 * each reference solution uses the Until rules; {@code ModalUntilExercisesTest} replays every one of them.
 */
public class ModalUntilExercises implements LogicalExercises {

    private static final List<Exercise> EXERCISES = List.of(
            new Exercise("until-now", "Until, right now",
                    List.of("q"), "p U q", Difficulty.EASY,
                    """
                    s0: q           Ass
                    s0: p U q           UI [1]
                    """),
            new Exercise("until-eventually", "Until promises its goal",
                    List.of("p U q"), "<> q", Difficulty.EASY,
                    """
                    s0: p U q           Ass
                    s0: <> q           UE [1]
                    """),
            new Exercise("until-from-always", "Always until sometime",
                    List.of("[] p", "<> q"), "p U q", Difficulty.EASY,
                    """
                    s0: [] p           Ass
                    s0: <> q           Ass
                    s0: p U q           UI [1, 2]
                    """),
            new Exercise("until-not-yet", "Not yet, so still",
                    List.of("p U q", "- q"), "p", Difficulty.MEDIUM,
                    """
                    s0: p U q           Ass
                    s0: - q           Ass
                    s0: p           UE [1, 2]
                    """),
            new Exercise("until-from-box", "Always is until",
                    List.of("[] q"), "p U q", Difficulty.MEDIUM,
                    """
                    s0: [] q           Ass
                    s0 <= s0           Refl [1]
                    s0: q           []E [1, 2]
                    s0: p U q           UI [3]
                    """),
            new Exercise("until-left-or-right", "One side holds now",
                    List.of("p U q"), "p | q", Difficulty.HARD,
                    """
                    s0: p U q           Ass
                    s0:    - (p | q)           Ass
                    s0:       q           Ass
                    s0:       p | q           |I [3]
                    s0:       FALSE           FI [4, 2]
                    s0:    - q           -I [3-5]
                    s0:    p           UE [1, 6]
                    s0:    p | q           |I [7]
                    s0:    FALSE           FI [8, 2]
                    s0: - (- (p | q))           -I [2-9]
                    s0: p | q           -E [10]
                    """));

    @Override
    public String logic() {
        return ModalUntilConfiguration.LOGIC;
    }

    @Override
    public List<Exercise> exercises() {
        return EXERCISES;
    }
}
