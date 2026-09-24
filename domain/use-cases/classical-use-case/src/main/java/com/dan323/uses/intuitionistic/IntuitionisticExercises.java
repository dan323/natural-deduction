package com.dan323.uses.intuitionistic;

import com.dan323.model.Difficulty;
import com.dan323.uses.Exercise;
import com.dan323.uses.LogicalExercises;
import com.dan323.uses.classical.ClassicalExercises;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * The intuitionistic exercises, from easy to hard: every classical exercise whose reference solution does not need
 * double negation elimination, and a few that show what intuitionistic logic can still prove about negation.
 * {@code IntuitionisticExercisesTest} replays every reference solution with the intuitionistic parser, and checks that
 * the solutions of the {@link #CLASSICAL_ONLY} exercises are rejected by it.
 */
public class IntuitionisticExercises implements LogicalExercises {

    /**
     * The classical exercises that are not provable intuitionistically: {@code - (- p) ⊢ p} and {@code ⊢ p | (- p)}.
     */
    public static final Set<String> CLASSICAL_ONLY = Set.of("double-negation-elimination", "excluded-middle");

    private static final List<Exercise> OWN = List.of(
            new Exercise("disjunctive-syllogism", "Disjunctive syllogism",
                    List.of("p | q", "- p"), "q", Difficulty.MEDIUM,
                    """
                    p | q           Ass
                    - p           Ass
                       p           Ass
                       FALSE           FI [3, 2]
                       q           FE [4]
                    p -> q           ->I [3-5]
                       q           Ass
                    q -> q           ->I [7-7]
                    q           |E [1, 6, 8]
                    """),
            new Exercise("triple-negation", "Triple negation is a single one",
                    List.of("- (- (- p))"), "- p", Difficulty.MEDIUM,
                    """
                    - (- (- p))           Ass
                       p           Ass
                          - p           Ass
                          FALSE           FI [2, 3]
                       - (- p)           -I [3-4]
                       FALSE           FI [5, 1]
                    - p           -I [2-6]
                    """),
            new Exercise("de-morgan-or", "Neither one nor the other",
                    List.of("- (p | q)"), "(- p) & (- q)", Difficulty.HARD,
                    """
                    - (p | q)           Ass
                       p           Ass
                       p | q           |I [2]
                       FALSE           FI [3, 1]
                    - p           -I [2-4]
                       q           Ass
                       p | q           |I [6]
                       FALSE           FI [7, 1]
                    - q           -I [6-8]
                    (- p) & (- q)           &I [5, 9]
                    """),
            new Exercise("excluded-middle-not-refutable", "Excluded middle cannot be refuted",
                    List.of(), "- (- (p | (- p)))", Difficulty.HARD,
                    """
                       - (p | (- p))           Ass
                          p           Ass
                          p | (- p)           |I [2]
                          FALSE           FI [3, 1]
                       - p           -I [2-4]
                       p | (- p)           |I [5]
                       FALSE           FI [6, 1]
                    - (- (p | (- p)))           -I [1-7]
                    """)
    );

    // A stable sort keeps the classical order within each difficulty and puts the own exercises after them.
    private static final List<Exercise> EXERCISES = Stream.concat(
                    new ClassicalExercises().exercises().stream().filter(exercise -> !CLASSICAL_ONLY.contains(exercise.id())),
                    OWN.stream())
            .sorted(Comparator.comparing(Exercise::difficulty))
            .toList();

    @Override
    public String logic() {
        return IntuitionisticRules.LOGIC;
    }

    @Override
    public List<Exercise> exercises() {
        return EXERCISES;
    }
}
