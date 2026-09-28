package com.dan323.uses.firstorder;

import com.dan323.model.Difficulty;
import com.dan323.uses.Exercise;
import com.dan323.uses.LogicalExercises;

import java.util.List;

/**
 * The exercises of first-order logic with equality, from easy to hard: the quantifier rules, swapping quantifiers,
 * distributing {@code forall} over {@code &} and the symmetry and transitivity of {@code =}. A free name in a premise
 * (such as {@code a}) acts as a constant. {@code FirstOrderExercisesTest} replays every reference solution.
 */
public class FirstOrderExercises implements LogicalExercises {

    private static final String FORALL_P = "forall x. P(x)";

    private static final List<Exercise> EXERCISES = List.of(
            new Exercise("forall-gives-exists", "What holds for all holds for some",
                    List.of(FORALL_P), "exists x. P(x)", Difficulty.EASY,
                    """
                    forall x. P(x)           Ass
                    P(a)           ∀E [1]
                    exists x. P(x)           ∃I [2]
                    """),
            new Exercise("reflexivity", "Everything equals itself",
                    List.of(), "forall x. x = x", Difficulty.EASY,
                    """
                    a = a           =I
                    forall x. x = x           ∀I [1]
                    """),
            new Exercise("symmetry", "Symmetry of equality",
                    List.of("a = b"), "b = a", Difficulty.EASY,
                    """
                    a = b           Ass
                    a = a           =I
                    b = a           =E [1, 2]
                    """),
            new Exercise("transitivity", "Transitivity of equality",
                    List.of("a = b", "b = c"), "a = c", Difficulty.EASY,
                    """
                    a = b           Ass
                    b = c           Ass
                    a = c           =E [2, 1]
                    """),
            new Exercise("forall-and", "For all distributes over and",
                    List.of("forall x. P(x) & Q(x)"), "(forall x. P(x)) & (forall x. Q(x))", Difficulty.MEDIUM,
                    """
                    forall x. P(x) & Q(x)           Ass
                    P(a) & Q(a)           ∀E [1]
                    P(a)           &E [2]
                    forall x. P(x)           ∀I [3]
                    Q(a)           &E [2]
                    forall x. Q(x)           ∀I [5]
                    (forall x. P(x)) & (forall x. Q(x))           &I [4, 6]
                    """),
            new Exercise("and-forall", "And of for-alls is a for-all",
                    List.of("(forall x. P(x)) & (forall x. Q(x))"), "forall x. P(x) & Q(x)", Difficulty.MEDIUM,
                    """
                    (forall x. P(x)) & (forall x. Q(x))           Ass
                    forall x. P(x)           &E [1]
                    forall x. Q(x)           &E [1]
                    P(a)           ∀E [2]
                    Q(a)           ∀E [3]
                    P(a) & Q(a)           &I [4, 5]
                    forall x. P(x) & Q(x)           ∀I [6]
                    """),
            new Exercise("forall-swap", "Swapping two for-alls",
                    List.of("forall x. forall y. R(x, y)"), "forall y. forall x. R(x, y)", Difficulty.MEDIUM,
                    """
                    forall x. forall y. R(x, y)           Ass
                    forall y. R(a, y)           ∀E [1]
                    R(a, b)           ∀E [2]
                    forall x. R(x, b)           ∀I [3]
                    forall y. forall x. R(x, y)           ∀I [4]
                    """),
            new Exercise("symmetry-for-all", "Equality is symmetric",
                    List.of(), "forall x. forall y. x = y -> y = x", Difficulty.MEDIUM,
                    """
                       a = b           Ass
                       a = a           =I
                       b = a           =E [1, 2]
                    a = b -> b = a           ->I [1-3]
                    forall y. a = y -> y = a           ∀I [4]
                    forall x. forall y. x = y -> y = x           ∀I [5]
                    """),
            new Exercise("exists-forall-swap", "Some for all gives for all some",
                    List.of("exists x. forall y. R(x, y)"), "forall y. exists x. R(x, y)", Difficulty.HARD,
                    """
                    exists x. forall y. R(x, y)           Ass
                       forall y. R(a, y)           Ass
                       R(a, b)           ∀E [2]
                       exists x. R(x, b)           ∃I [3]
                    exists x. R(x, b)           ∃E [1, 2-4]
                    forall y. exists x. R(x, y)           ∀I [5]
                    """),
            new Exercise("transitivity-for-all", "Equality is transitive",
                    List.of(), "forall x. forall y. forall z. (x = y & y = z) -> x = z", Difficulty.HARD,
                    """
                       a = b & b = c           Ass
                       a = b           &E [1]
                       b = c           &E [1]
                       a = c           =E [3, 2]
                    (a = b & b = c) -> a = c           ->I [1-4]
                    forall z. (a = b & b = z) -> a = z           ∀I [5]
                    forall y. forall z. (a = y & y = z) -> a = z           ∀I [6]
                    forall x. forall y. forall z. (x = y & y = z) -> x = z           ∀I [7]
                    """));

    @Override
    public String logic() {
        return FirstOrderConfiguration.LOGIC;
    }

    @Override
    public List<Exercise> exercises() {
        return EXERCISES;
    }
}
