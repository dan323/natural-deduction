package com.dan323.proof.firstorder;

import com.dan323.proof.firstorder.proof.FirstOrderNaturalDeduction;
import com.dan323.proof.firstorder.proof.ParseFirstOrderAction;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import static com.dan323.proof.firstorder.FirstOrderTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class FirstOrderSolverTest {

    private static final Duration LIMIT = Duration.ofSeconds(5);

    private record Goal(String goal, String... premises) {
        @Override
        public String toString() {
            return String.join(", ", premises) + " |- " + goal;
        }
    }

    private static FirstOrderNaturalDeduction solve(Goal goal) {
        var proof = proofWithGoal(goal.goal(), goal.premises());
        assertTimeoutPreemptively(LIMIT, proof::automate, goal::toString);
        return proof;
    }

    private static void assertSolves(Goal goal) {
        var proof = solve(goal);
        assertTrue(proof.isDone(), () -> goal + ": done\n" + proof);
        assertEquals(formula(goal.goal()), proof.getSteps().getLast().getStep(), goal + ": the last line is the goal");
        // Written as the goal, not only equal to it up to renaming: a client compares the texts
        assertEquals(formula(goal.goal()).toString(), proof.getSteps().getLast().getStep().toString(), goal + ": the last line reads as the goal");
        assertEquals(0, proof.getSteps().getLast().getAssumptionLevel(), goal + ": at the top level");
        for (int i = 0; i < goal.premises().length; i++) {
            assertEquals(formula(goal.premises()[i]), proof.getSteps().get(i).getStep(), goal + ": the premises stay");
        }
        // Every step is a rule of first-order logic: the proof replays from its own steps
        assertTrue(replay(proof).isDone(), goal + ": replays and is done");
        // The text layout reads back, as the REST layer does
        for (var step : proof.getSteps()) {
            assertEquals(step.getProof(), ParseFirstOrderAction.parseReason(step.getProof().toString()));
        }
    }

    private static void assertNotSolved(Goal goal) {
        var proof = solve(goal);
        assertFalse(proof.isDone(), goal::toString);
        assertEquals(goal.premises().length, proof.getSteps().size(), goal + ": left with its premises");
    }

    @TestFactory
    Stream<DynamicTest> quantifierGoalsAreSolved() {
        return Stream.of(
                        new Goal("exists x. P(x)", "forall x. P(x)"),
                        new Goal("forall x. x = x"),
                        new Goal("b = a", "a = b"),
                        new Goal("a = c", "a = b", "b = c"),
                        new Goal("d = a", "a = b", "b = c", "c = d"),
                        new Goal("(forall x. P(x)) & (forall x. Q(x))", "forall x. P(x) & Q(x)"),
                        new Goal("forall x. P(x) & Q(x)", "(forall x. P(x)) & (forall x. Q(x))"),
                        new Goal("forall y. forall x. R(x, y)", "forall x. forall y. R(x, y)"),
                        new Goal("forall x. forall y. x = y -> y = x"),
                        new Goal("forall y. exists x. R(x, y)", "exists x. forall y. R(x, y)"),
                        new Goal("forall x. forall y. forall z. (x = y & y = z) -> x = z"),
                        // ∃E, then ∃I with the witness
                        new Goal("exists y. Q(y)", "exists x. P(x)", "forall x. P(x) -> Q(x)"),
                        new Goal("exists x. P(x) | Q(x)", "(exists x. P(x)) | (exists x. Q(x))"),
                        new Goal("(exists x. P(x)) | (exists x. Q(x))", "exists x. P(x) | Q(x)"),
                        // Terms of the proof, and a function application
                        new Goal("exists x. P(f(x))", "forall x. P(f(x))", "Q(a)"),
                        new Goal("P(f(a))", "forall x. P(x)", "Q(a)"),
                        new Goal("Q(a)", "forall x. P(x) -> Q(x)", "P(a)"),
                        // A vacuous quantifier
                        new Goal("p", "forall x. p"),
                        new Goal("forall x. p", "p"),
                        // Classical: by contradiction, and De Morgan for the quantifiers
                        new Goal("forall x. - P(x)", "- (exists x. P(x))"),
                        new Goal("- (exists x. P(x))", "forall x. - P(x)"),
                        new Goal("- (forall x. P(x))", "exists x. - P(x)"),
                        new Goal("exists x. - P(x)", "- (forall x. P(x))"),
                        new Goal("(forall x. P(x)) | (- (forall x. P(x)))"),
                        // The goal only up to renaming of bound variables: in a premise, or derived
                        new Goal("forall y. P(y)", "forall x. P(x)"),
                        new Goal("exists a. P(a)", "exists x. P(x)"),
                        new Goal("forall y. P(y)", "(forall x. P(x)) & Q"),
                        // Terms written in the premises or the goal are used however deep they are
                        new Goal("exists x. P(x)", "P(f(f(f(a))))"),
                        new Goal("P(f(f(f(a))))", "forall x. P(x)"),
                        new Goal("Q(f(f(f(a))))", "forall x. P(x) -> Q(x)", "P(f(f(f(a))))"))
                .map(goal -> DynamicTest.dynamicTest(goal.toString(), () -> assertSolves(goal)));
    }

    @TestFactory
    Stream<DynamicTest> propositionalGoalsAreSolved() {
        return Stream.of(
                        new Goal("p -> p"),
                        new Goal("r", "p -> q", "q -> r", "p"),
                        new Goal("p | (- p)"),
                        new Goal("p", "- (- p)"),
                        new Goal("((p -> q) -> p) -> p"),
                        new Goal("q | p", "p | q"),
                        new Goal("(- p) & (- q)", "- (p | q)"),
                        new Goal("(- p) | (- q)", "- (p & q)"),
                        new Goal("r", "(p -> q) -> r", "q"),
                        new Goal("q", "p", "- p"),
                        new Goal("p", "FALSE"))
                .map(goal -> DynamicTest.dynamicTest(goal.toString(), () -> assertSolves(goal)));
    }

    @TestFactory
    Stream<DynamicTest> unprovableGoalsEndUnsolved() {
        return Stream.of(
                        new Goal("forall x. P(x)", "exists x. P(x)"),
                        new Goal("forall x. P(x)", "P(a)"),
                        new Goal("P(b)", "P(a)"),
                        new Goal("exists x. forall y. R(x, y)", "forall y. exists x. R(x, y)"),
                        new Goal("a = b"),
                        new Goal("q", "p | q"),
                        new Goal("p"),
                        // No proof, and lots of terms to try
                        new Goal("Q(a)", "forall x. P(x) -> P(f(x))", "forall x. forall y. R(x, y) | R(y, x)", "P(a)"),
                        // The search runs out of its budget of goals
                        new Goal("r", "a1 -> b1", "b1 -> a2", "a2 -> b2", "(b2 -> r) -> a3", "a3 -> (b3 | c3)", "- c3", "(b3 -> a1) -> r1"))
                .map(goal -> DynamicTest.dynamicTest(goal.toString(), () -> assertNotSolved(goal)));
    }

    @TestFactory
    Stream<DynamicTest> theoremsBeyondItsBoundsEndUnsolved() {
        return Stream.of(
                        // ∃I would need a term deeper than MAX_TERM_DEPTH
                        new Goal("forall x. exists y. P(f(f(f(x)))) -> P(y)"),
                        // Equality is only used for symmetry and transitivity, not to rewrite inside a term
                        new Goal("forall x. i(i(x)) = x", "forall x. forall y. forall z. m(m(x, y), z) = m(x, m(y, z))",
                                "forall x. m(e, x) = x & m(x, e) = x", "forall x. m(i(x), x) = e & m(x, i(x)) = e"))
                .map(goal -> DynamicTest.dynamicTest(goal.toString(), () -> assertNotSolved(goal)));
    }

    @Test
    void theSolverStartsFromThePremises() {
        var proof = proofWithGoal("exists x. P(x)", "forall x. P(x)");
        assume(proof, "Q(a)");

        proof.automate();

        assertTrue(proof.isDone());
        assertEquals(formula("forall x. P(x)"), proof.getSteps().getFirst().getStep());
        assertTrue(proof.getSteps().stream().noneMatch(step -> step.getStep().equals(formula("Q(a)"))));
    }

    @Test
    void theNameItPicksIsFresh() {
        // a is in a premise, so the name for ∀I is another one
        var proof = solve(new Goal("forall x. P(x) -> P(x)", "Q(a)"));

        assertTrue(proof.isDone());
        assertEquals(List.of("Q(a)", "P(b)", "P(b) -> P(b)", "forall x. P(x) -> P(x)"),
                proof.getSteps().stream().map(step -> step.getStep().toString()).toList());
    }

    @Test
    void aGoalThatIsAPremiseIsCopied() {
        var proof = solve(new Goal("P(a)", "P(a)", "Q(b)"));

        assertTrue(proof.isDone());
        assertEquals(3, proof.getSteps().size());
        assertEquals("Rep [1]", proof.getSteps().getLast().getProof().toString());
    }

    @Test
    void theSolverIsDeterministic() {
        var goal = new Goal("forall y. exists x. R(x, y)", "exists x. forall y. R(x, y)");

        assertEquals(solve(goal).toString(), solve(goal).toString());
    }
}
