package com.dan323.uses.modal.nextuntil;

import com.dan323.model.Difficulty;
import com.dan323.uses.Exercise;
import com.dan323.uses.LogicalExercises;

import java.util.List;

/**
 * The exercises of {@code modal-next-until}, from easy to hard. The premises are in the initial state {@code s0}, and
 * each reference solution uses the Next and Until rules; {@code ModalNextUntilExercisesTest} replays every one of them.
 */
public class ModalNextUntilExercises implements LogicalExercises {

    private static final String P_UNTIL_Q = "p U q";

    private static final List<Exercise> EXERCISES = List.of(
            new Exercise("next-in-and-out", "In and out of the next state",
                    List.of("X p"), "X (p | q)", Difficulty.EASY,
                    """
                    s0: X p           Ass
                    s0+1: p           XE [1]
                    s0+1: p | q           |I [2]
                    s0: X (p | q)           XI [3]
                    """),
            new Exercise("until-now", "Until, right now",
                    List.of("q"), P_UNTIL_Q, Difficulty.EASY,
                    """
                    s0: q           Ass
                    s0: p U q           UI [1]
                    """),
            new Exercise("until-reaches-its-goal", "Until reaches its goal",
                    List.of(P_UNTIL_Q), "<> q", Difficulty.EASY,
                    """
                    s0: p U q           Ass
                    s0: <> q           U<> [1]
                    """),
            new Exercise("next-and", "Next distributes over and",
                    List.of("X p", "X q"), "X (p & q)", Difficulty.MEDIUM,
                    """
                    s0: X p           Ass
                    s0: X q           Ass
                    s0+1: p           XE [1]
                    s0+1: q           XE [2]
                    s0+1: p & q           &I [3, 4]
                    s0: X (p & q)           XI [5]
                    """),
            new Exercise("always-next", "Always includes the next state",
                    List.of("[] p"), "X p", Difficulty.MEDIUM,
                    """
                    s0: [] p           Ass
                    s0 <= s0+1           Succ [1]
                    s0+1: p           []E [1, 2]
                    s0: X p           XI [3]
                    """),
            new Exercise("until-later", "Until, one step later",
                    List.of("p", "X q"), P_UNTIL_Q, Difficulty.MEDIUM,
                    """
                    s0: p           Ass
                    s0: X q           Ass
                    s0+1: q           XE [2]
                    s0+1: p U q           UI [3]
                    s0: X (p U q)           XI [4]
                    s0: p U q           UI [1, 5]
                    """),
            new Exercise("induction", "Induction",
                    List.of("p", "[] (p -> (X p))"), "[] p", Difficulty.MEDIUM,
                    """
                    s0: p           Ass
                    s0: [] (p -> (X p))           Ass
                    s0: [] p           Ind [1, 2]
                    """),
            new Exercise("until-now-or-next", "Now, or until from the next state",
                    List.of(P_UNTIL_Q), "q | (X (p U q))", Difficulty.HARD,
                    """
                    s0: p U q           Ass
                    s0: q | (p & (X (p U q)))           UE [1]
                    s0:    q           Ass
                    s0:    q | (X (p U q))           |I [3]
                    s0: q -> (q | (X (p U q)))           ->I [3-4]
                    s0:    p & (X (p U q))           Ass
                    s0:    X (p U q)           &E [6]
                    s0:    q | (X (p U q))           |I [7]
                    s0: (p & (X (p U q))) -> (q | (X (p U q)))           ->I [6-8]
                    s0: q | (X (p U q))           |E [2, 5, 9]
                    """),
            new Exercise("always-always-next", "Always, from the next state on",
                    List.of("[] p"), "X ([] p)", Difficulty.HARD,
                    """
                    s0: [] p           Ass
                    s0 <= s0+1           Succ [1]
                       s0+1 <= s1           Ass
                       s0 <= s1           Trans [2, 3]
                    s1:    p           []E [1, 4]
                    s0+1: [] p           []I [3-5]
                    s0: X ([] p)           XI [6]
                    """));

    @Override
    public String logic() {
        return ModalNextUntilConfiguration.LOGIC;
    }

    @Override
    public List<Exercise> exercises() {
        return EXERCISES;
    }
}
