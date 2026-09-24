package com.dan323.uses.classical;

import com.dan323.model.Difficulty;
import com.dan323.uses.Exercise;
import com.dan323.uses.LogicalExercises;

import java.util.List;

/**
 * The classical exercises, from easy to hard. Premises and goals are written the way the classical parser prints
 * formulas (fully parenthesized, e.g. {@code - (- p)}), so they parse back to the same formula. Each reference
 * solution is a complete proof in the proof-text layout; {@code ClassicalExercisesTest} replays every one of them.
 */
public class ClassicalExercises implements LogicalExercises {

    private static final List<Exercise> EXERCISES = List.of(
            new Exercise("modus-ponens", "Modus ponens",
                    List.of("p", "p -> q"), "q", Difficulty.EASY,
                    """
                    p           Ass
                    p -> q           Ass
                    q           ->E [2, 1]
                    """),
            new Exercise("and-commutes", "Conjunction commutes",
                    List.of("p & q"), "q & p", Difficulty.EASY,
                    """
                    p & q           Ass
                    p           &E [1]
                    q           &E [1]
                    q & p           &I [3, 2]
                    """),
            new Exercise("or-introduction", "Disjunction introduction",
                    List.of("p"), "p | q", Difficulty.EASY,
                    """
                    p           Ass
                    p | q           |I [1]
                    """),
            new Exercise("double-negation-elimination", "Double negation elimination",
                    List.of("- (- p)"), "p", Difficulty.EASY,
                    """
                    - (- p)           Ass
                    p           -E [1]
                    """),
            new Exercise("modus-ponens-chain", "A chain of modus ponens",
                    List.of("p", "p -> q", "q -> r"), "r", Difficulty.EASY,
                    """
                    p           Ass
                    p -> q           Ass
                    q -> r           Ass
                    q           ->E [2, 1]
                    r           ->E [3, 4]
                    """),
            new Exercise("identity", "Every formula implies itself",
                    List.of(), "p -> p", Difficulty.EASY,
                    """
                       p           Ass
                    p -> p           ->I [1-1]
                    """),
            new Exercise("hypothetical-syllogism", "Hypothetical syllogism",
                    List.of("p -> q", "q -> r"), "p -> r", Difficulty.MEDIUM,
                    """
                    p -> q           Ass
                    q -> r           Ass
                       p           Ass
                       q           ->E [1, 3]
                       r           ->E [2, 4]
                    p -> r           ->I [3-5]
                    """),
            new Exercise("and-associates", "Conjunction associates",
                    List.of("p & (q & r)"), "(p & q) & r", Difficulty.MEDIUM,
                    """
                    p & (q & r)           Ass
                    p           &E [1]
                    q & r           &E [1]
                    q           &E [3]
                    r           &E [3]
                    p & q           &I [2, 4]
                    (p & q) & r           &I [6, 5]
                    """),
            new Exercise("or-commutes", "Disjunction commutes",
                    List.of("p | q"), "q | p", Difficulty.MEDIUM,
                    """
                    p | q           Ass
                       p           Ass
                       q | p           |I [2]
                    p -> (q | p)           ->I [2-3]
                       q           Ass
                       q | p           |I [5]
                    q -> (q | p)           ->I [5-6]
                    q | p           |E [1, 4, 7]
                    """),
            new Exercise("ex-falso", "From a contradiction, anything",
                    List.of("p", "- p"), "q", Difficulty.MEDIUM,
                    """
                    p           Ass
                    - p           Ass
                    FALSE           FI [1, 2]
                    q           FE [3]
                    """),
            new Exercise("modus-tollens", "Modus tollens",
                    List.of("p -> q", "- q"), "- p", Difficulty.MEDIUM,
                    """
                    p -> q           Ass
                    - q           Ass
                       p           Ass
                       q           ->E [1, 3]
                       FALSE           FI [4, 2]
                    - p           -I [3-5]
                    """),
            new Exercise("double-negation-introduction", "Double negation introduction",
                    List.of("p"), "- (- p)", Difficulty.MEDIUM,
                    """
                    p           Ass
                       - p           Ass
                       FALSE           FI [1, 2]
                    - (- p)           -I [2-3]
                    """),
            new Exercise("contraposition", "Contraposition",
                    List.of("p -> q"), "(- q) -> (- p)", Difficulty.HARD,
                    """
                    p -> q           Ass
                       - q           Ass
                          p           Ass
                          q           ->E [1, 3]
                          FALSE           FI [4, 2]
                       - p           -I [3-5]
                    (- q) -> (- p)           ->I [2-6]
                    """),
            new Exercise("excluded-middle", "The law of excluded middle",
                    List.of(), "p | (- p)", Difficulty.HARD,
                    """
                       - (p | (- p))           Ass
                          p           Ass
                          p | (- p)           |I [2]
                          FALSE           FI [3, 1]
                       - p           -I [2-4]
                       p | (- p)           |I [5]
                       FALSE           FI [6, 1]
                    - (- (p | (- p)))           -I [1-7]
                    p | (- p)           -E [8]
                    """)
    );

    @Override
    public String logic() {
        return "classical";
    }

    @Override
    public List<Exercise> exercises() {
        return EXERCISES;
    }
}
