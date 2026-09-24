package com.dan323.uses.modal;

import com.dan323.model.Difficulty;
import com.dan323.uses.Exercise;
import com.dan323.uses.LogicalExercises;

import java.util.List;

/**
 * The modal exercises, from easy to hard. Premises and goals are written the way the modal parser prints formulas
 * ({@code [] p}, {@code [] ([] p)}), so they parse back to the same formula. The premises are in the initial state
 * {@code s0}. Each reference solution is a complete proof in the proof-text layout, with the state of each line in front
 * of it ({@code s0: [] p}) and no state for a relation ({@code s0 <= s1}), and derives the goal in {@code s0};
 * {@code ModalExercisesTest} replays every one of them.
 */
public class ModalExercises implements LogicalExercises {

    private static final String BOX_P = "[] p";

    private static final List<Exercise> EXERCISES = List.of(
            new Exercise("box-elimination", "What is necessary is true",
                    List.of(BOX_P), "p", Difficulty.EASY,
                    """
                    s0: [] p           Ass
                    s0 <= s0           Refl [1]
                    s0: p           []E [1, 2]
                    """),
            new Exercise("diamond-introduction", "What is true is possible",
                    List.of("p"), "<> p", Difficulty.EASY,
                    """
                    s0: p           Ass
                    s0 <= s0           Refl [1]
                    s0: <> p           <>I [1, 2]
                    """),
            new Exercise("necessary-is-possible", "What is necessary is possible",
                    List.of(BOX_P), "<> p", Difficulty.MEDIUM,
                    """
                    s0: [] p           Ass
                    s0 <= s0           Refl [1]
                    s0: p           []E [1, 2]
                    s0: <> p           <>I [3, 2]
                    """),
            new Exercise("box-and-elimination", "Necessity of a part of a conjunction",
                    List.of("[] (p & q)"), BOX_P, Difficulty.MEDIUM,
                    """
                    s0: [] (p & q)           Ass
                       s0 <= s1           Ass
                       s1:    p & q           []E [1, 2]
                       s1:    p           &E [3]
                    s0: [] p           []I [2-4]
                    """),
            new Exercise("box-and-introduction", "Necessity of a conjunction",
                    List.of(BOX_P, "[] q"), "[] (p & q)", Difficulty.MEDIUM,
                    """
                    s0: [] p           Ass
                    s0: [] q           Ass
                       s0 <= s1           Ass
                       s1:    p           []E [1, 3]
                       s1:    q           []E [2, 3]
                       s1:    p & q           &I [4, 5]
                    s0: [] (p & q)           []I [3-6]
                    """),
            new Exercise("box-distribution", "Necessity distributes over implication",
                    List.of("[] (p -> q)", BOX_P), "[] q", Difficulty.HARD,
                    """
                    s0: [] (p -> q)           Ass
                    s0: [] p           Ass
                       s0 <= s1           Ass
                       s1:    p -> q           []E [1, 3]
                       s1:    p           []E [2, 3]
                       s1:    q           ->E [4, 5]
                    s0: [] q           []I [3-6]
                    """),
            new Exercise("box-transitive", "What is necessary is necessarily necessary",
                    List.of(BOX_P), "[] ([] p)", Difficulty.HARD,
                    """
                    s0: [] p           Ass
                       s0 <= s1           Ass
                          s1 <= s2           Ass
                          s0 <= s2           Trans [2, 3]
                       s2:       p           []E [1, 4]
                    s1:    [] p           []I [3-5]
                    s0: [] ([] p)           []I [2-6]
                    """)
    );

    @Override
    public String logic() {
        return "modal";
    }

    @Override
    public List<Exercise> exercises() {
        return EXERCISES;
    }
}
