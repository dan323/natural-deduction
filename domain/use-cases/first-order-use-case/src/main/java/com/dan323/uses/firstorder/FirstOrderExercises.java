package com.dan323.uses.firstorder;

import com.dan323.model.Difficulty;
import com.dan323.uses.Exercise;
import com.dan323.uses.LogicalExercises;

import java.util.List;

/**
 * The exercises of first-order logic with equality, from easy to hard: the quantifier rules, swapping quantifiers,
 * distributing {@code forall} over {@code &} and the symmetry and transitivity of {@code =}. A free name in a premise
 * (such as {@code a}) acts as a constant. The group exercises (ids {@code group-...}) have exactly the
 * {@link FirstOrderTheories#GROUP_AXIOMS} as premises, and sit in the catalog by difficulty next to the others. {@code FirstOrderExercisesTest} replays every reference solution.
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
            new Exercise("group-identity-unique", "The identity is unique",
                    FirstOrderTheories.GROUP_AXIOMS, "forall y. (forall x. m(y, x) = x) -> y = e", Difficulty.MEDIUM,
                    """
                    forall x. forall y. forall z. m(m(x, y), z) = m(x, m(y, z))           Ass
                    forall x. m(e, x) = x & m(x, e) = x           Ass
                    forall x. m(i(x), x) = e & m(x, i(x)) = e           Ass
                       forall x. m(a, x) = x           Ass
                       m(a, e) = e           ∀E [4]
                       m(e, a) = a & m(a, e) = a           ∀E [2]
                       m(a, e) = a           &E [6]
                       a = e           =E [7, 5]
                    (forall x. m(a, x) = x) -> a = e           ->I [4-8]
                    forall y. (forall x. m(y, x) = x) -> y = e           ∀I [9]
                    """),
            new Exercise("group-left-cancellation", "Left cancellation",
                    FirstOrderTheories.GROUP_AXIOMS, "forall x. forall y. forall z. m(x, y) = m(x, z) -> y = z", Difficulty.MEDIUM,
                    """
                    forall x. forall y. forall z. m(m(x, y), z) = m(x, m(y, z))           Ass
                    forall x. m(e, x) = x & m(x, e) = x           Ass
                    forall x. m(i(x), x) = e & m(x, i(x)) = e           Ass
                       m(a, b) = m(a, c)           Ass
                       m(i(a), a) = e & m(a, i(a)) = e           ∀E [3]
                       m(i(a), a) = e           &E [5]
                       forall y. forall z. m(m(i(a), y), z) = m(i(a), m(y, z))           ∀E [1]
                       forall z. m(m(i(a), a), z) = m(i(a), m(a, z))           ∀E [7]
                       m(m(i(a), a), b) = m(i(a), m(a, b))           ∀E [8]
                       m(m(i(a), a), c) = m(i(a), m(a, c))           ∀E [8]
                       m(m(i(a), a), b) = m(i(a), m(a, c))           =E [4, 9]
                       m(m(i(a), a), c) = m(m(i(a), a), c)           =I
                       m(i(a), m(a, c)) = m(m(i(a), a), c)           =E [10, 12]
                       m(m(i(a), a), b) = m(m(i(a), a), c)           =E [13, 11]
                       m(e, b) = m(e, c)           =E [6, 14]
                       m(e, b) = b & m(b, e) = b           ∀E [2]
                       m(e, b) = b           &E [16]
                       m(e, c) = c & m(c, e) = c           ∀E [2]
                       m(e, c) = c           &E [18]
                       b = m(e, c)           =E [17, 15]
                       b = c           =E [19, 20]
                    m(a, b) = m(a, c) -> b = c           ->I [4-21]
                    forall z. m(a, b) = m(a, z) -> b = z           ∀I [22]
                    forall y. forall z. m(a, y) = m(a, z) -> y = z           ∀I [23]
                    forall x. forall y. forall z. m(x, y) = m(x, z) -> y = z           ∀I [24]
                    """),
            new Exercise("group-double-inverse", "The inverse of the inverse",
                    FirstOrderTheories.GROUP_AXIOMS, "forall x. i(i(x)) = x", Difficulty.MEDIUM,
                    """
                    forall x. forall y. forall z. m(m(x, y), z) = m(x, m(y, z))           Ass
                    forall x. m(e, x) = x & m(x, e) = x           Ass
                    forall x. m(i(x), x) = e & m(x, i(x)) = e           Ass
                    m(e, i(i(a))) = i(i(a)) & m(i(i(a)), e) = i(i(a))           ∀E [2]
                    m(i(i(a)), e) = i(i(a))           &E [4]
                    m(i(a), a) = e & m(a, i(a)) = e           ∀E [3]
                    m(i(a), a) = e           &E [6]
                    m(i(i(a)), i(a)) = e & m(i(a), i(i(a))) = e           ∀E [3]
                    m(i(i(a)), i(a)) = e           &E [8]
                    forall y. forall z. m(m(i(i(a)), y), z) = m(i(i(a)), m(y, z))           ∀E [1]
                    forall z. m(m(i(i(a)), i(a)), z) = m(i(i(a)), m(i(a), z))           ∀E [10]
                    m(m(i(i(a)), i(a)), a) = m(i(i(a)), m(i(a), a))           ∀E [11]
                    m(e, a) = m(i(i(a)), m(i(a), a))           =E [9, 12]
                    m(e, a) = m(i(i(a)), e)           =E [7, 13]
                    m(e, a) = i(i(a))           =E [5, 14]
                    m(e, a) = a & m(a, e) = a           ∀E [2]
                    m(e, a) = a           &E [16]
                    i(i(a)) = a           =E [15, 17]
                    forall x. i(i(x)) = x           ∀I [18]
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
                    """),
            new Exercise("group-inverse-unique", "Inverses are unique",
                    FirstOrderTheories.GROUP_AXIOMS, "forall x. forall y. m(x, y) = e -> y = i(x)", Difficulty.HARD,
                    """
                    forall x. forall y. forall z. m(m(x, y), z) = m(x, m(y, z))           Ass
                    forall x. m(e, x) = x & m(x, e) = x           Ass
                    forall x. m(i(x), x) = e & m(x, i(x)) = e           Ass
                       m(a, b) = e           Ass
                       m(e, b) = b & m(b, e) = b           ∀E [2]
                       m(e, b) = b           &E [5]
                       m(i(a), a) = e & m(a, i(a)) = e           ∀E [3]
                       m(i(a), a) = e           &E [7]
                       forall y. forall z. m(m(i(a), y), z) = m(i(a), m(y, z))           ∀E [1]
                       forall z. m(m(i(a), a), z) = m(i(a), m(a, z))           ∀E [9]
                       m(m(i(a), a), b) = m(i(a), m(a, b))           ∀E [10]
                       m(e, b) = m(i(a), m(a, b))           =E [8, 11]
                       m(e, b) = m(i(a), e)           =E [4, 12]
                       m(e, i(a)) = i(a) & m(i(a), e) = i(a)           ∀E [2]
                       m(i(a), e) = i(a)           &E [14]
                       m(e, b) = i(a)           =E [15, 13]
                       b = i(a)           =E [6, 16]
                    m(a, b) = e -> b = i(a)           ->I [4-17]
                    forall y. m(a, y) = e -> y = i(a)           ∀I [18]
                    forall x. forall y. m(x, y) = e -> y = i(x)           ∀I [19]
                    """),
            new Exercise("group-inverse-of-product", "The inverse of a product",
                    FirstOrderTheories.GROUP_AXIOMS, "forall x. forall y. i(m(x, y)) = m(i(y), i(x))", Difficulty.HARD,
                    """
                    forall x. forall y. forall z. m(m(x, y), z) = m(x, m(y, z))           Ass
                    forall x. m(e, x) = x & m(x, e) = x           Ass
                    forall x. m(i(x), x) = e & m(x, i(x)) = e           Ass
                    forall y. forall z. m(m(a, y), z) = m(a, m(y, z))           ∀E [1]
                    forall z. m(m(a, b), z) = m(a, m(b, z))           ∀E [4]
                    m(m(a, b), m(i(b), i(a))) = m(a, m(b, m(i(b), i(a))))           ∀E [5]
                    forall y. forall z. m(m(b, y), z) = m(b, m(y, z))           ∀E [1]
                    forall z. m(m(b, i(b)), z) = m(b, m(i(b), z))           ∀E [7]
                    m(m(b, i(b)), i(a)) = m(b, m(i(b), i(a)))           ∀E [8]
                    m(i(b), b) = e & m(b, i(b)) = e           ∀E [3]
                    m(b, i(b)) = e           &E [10]
                    m(e, i(a)) = m(b, m(i(b), i(a)))           =E [11, 9]
                    m(e, i(a)) = i(a) & m(i(a), e) = i(a)           ∀E [2]
                    m(e, i(a)) = i(a)           &E [13]
                    m(b, m(i(b), i(a))) = i(a)           =E [12, 14]
                    m(m(a, b), m(i(b), i(a))) = m(a, i(a))           =E [15, 6]
                    m(i(a), a) = e & m(a, i(a)) = e           ∀E [3]
                    m(a, i(a)) = e           &E [17]
                    m(m(a, b), m(i(b), i(a))) = e           =E [18, 16]
                    m(e, i(m(a, b))) = i(m(a, b)) & m(i(m(a, b)), e) = i(m(a, b))           ∀E [2]
                    m(i(m(a, b)), e) = i(m(a, b))           &E [20]
                    forall y. forall z. m(m(i(m(a, b)), y), z) = m(i(m(a, b)), m(y, z))           ∀E [1]
                    forall z. m(m(i(m(a, b)), m(a, b)), z) = m(i(m(a, b)), m(m(a, b), z))           ∀E [22]
                    m(m(i(m(a, b)), m(a, b)), m(i(b), i(a))) = m(i(m(a, b)), m(m(a, b), m(i(b), i(a))))           ∀E [23]
                    m(i(m(a, b)), m(a, b)) = e & m(m(a, b), i(m(a, b))) = e           ∀E [3]
                    m(i(m(a, b)), m(a, b)) = e           &E [25]
                    m(e, m(i(b), i(a))) = m(i(m(a, b)), m(m(a, b), m(i(b), i(a))))           =E [26, 24]
                    m(e, m(i(b), i(a))) = m(i(m(a, b)), e)           =E [19, 27]
                    m(e, m(i(b), i(a))) = i(m(a, b))           =E [21, 28]
                    m(e, m(i(b), i(a))) = m(i(b), i(a)) & m(m(i(b), i(a)), e) = m(i(b), i(a))           ∀E [2]
                    m(e, m(i(b), i(a))) = m(i(b), i(a))           &E [30]
                    i(m(a, b)) = m(i(b), i(a))           =E [29, 31]
                    forall y. i(m(a, y)) = m(i(y), i(a))           ∀I [32]
                    forall x. forall y. i(m(x, y)) = m(i(y), i(x))           ∀I [33]
                    """),
            new Exercise("group-exponent-two-commutative", "Every element its own inverse: the group is commutative",
                    FirstOrderTheories.GROUP_AXIOMS, "(forall x. m(x, x) = e) -> (forall x. forall y. m(x, y) = m(y, x))", Difficulty.HARD,
                    """
                    forall x. forall y. forall z. m(m(x, y), z) = m(x, m(y, z))           Ass
                    forall x. m(e, x) = x & m(x, e) = x           Ass
                    forall x. m(i(x), x) = e & m(x, i(x)) = e           Ass
                       forall x. m(x, x) = e           Ass
                       m(a, a) = e           ∀E [4]
                       m(b, b) = e           ∀E [4]
                       m(m(a, b), m(a, b)) = e           ∀E [4]
                       m(e, b) = b & m(b, e) = b           ∀E [2]
                       m(e, b) = b           &E [8]
                       m(e, a) = a & m(a, e) = a           ∀E [2]
                       m(a, e) = a           &E [10]
                       forall y. forall z. m(m(a, y), z) = m(a, m(y, z))           ∀E [1]
                       forall z. m(m(a, a), z) = m(a, m(a, z))           ∀E [12]
                       m(m(a, a), b) = m(a, m(a, b))           ∀E [13]
                       m(e, b) = m(a, m(a, b))           =E [5, 14]
                       m(a, m(a, b)) = b           =E [15, 9]
                       forall z. m(m(a, b), z) = m(a, m(b, z))           ∀E [12]
                       m(m(a, b), b) = m(a, m(b, b))           ∀E [17]
                       m(m(a, b), b) = m(a, e)           =E [6, 18]
                       m(m(a, b), b) = a           =E [11, 19]
                       forall y. forall z. m(m(m(a, b), y), z) = m(m(a, b), m(y, z))           ∀E [1]
                       forall z. m(m(m(a, b), m(a, b)), z) = m(m(a, b), m(m(a, b), z))           ∀E [21]
                       m(m(m(a, b), m(a, b)), b) = m(m(a, b), m(m(a, b), b))           ∀E [22]
                       m(e, b) = m(m(a, b), m(m(a, b), b))           =E [7, 23]
                       m(m(a, b), m(m(a, b), b)) = b           =E [24, 9]
                       forall z. m(m(a, m(a, b)), z) = m(a, m(m(a, b), z))           ∀E [12]
                       m(m(a, m(a, b)), m(m(a, b), b)) = m(a, m(m(a, b), m(m(a, b), b)))           ∀E [26]
                       m(m(a, m(a, b)), m(m(a, b), b)) = m(a, b)           =E [25, 27]
                       m(b, m(m(a, b), b)) = m(a, b)           =E [16, 28]
                       m(b, a) = m(a, b)           =E [20, 29]
                       forall y. m(b, y) = m(y, b)           ∀I [30]
                       forall x. forall y. m(x, y) = m(y, x)           ∀I [31]
                    (forall x. m(x, x) = e) -> (forall x. forall y. m(x, y) = m(y, x))           ->I [4-32]
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
