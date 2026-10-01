package com.dan323.uses.firstorder.test;

import com.dan323.model.ProofDto;
import com.dan323.model.StepDto;
import com.dan323.uses.Exercise;
import com.dan323.uses.LogicalSolver;
import com.dan323.uses.firstorder.FirstOrderExercises;
import com.dan323.uses.firstorder.FirstOrderProofTransformer;
import com.dan323.uses.firstorder.FirstOrderTheories;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class FirstOrderSolverTest {

    private static final String LOGIC = "first-order";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final FirstOrderProofTransformer transformer = new FirstOrderProofTransformer();
    private final LogicalSolver<?, ?, ?, ?> solver = new LogicalSolver<>(transformer, TIMEOUT);

    private static ProofDto start(List<String> premises, String goal) {
        var steps = premises.stream().map(premise -> new StepDto(premise, "Ass", 0, Map.of())).toList();
        return new ProofDto(steps, LOGIC, goal);
    }

    private static boolean isGroupExercise(Exercise exercise) {
        return exercise.premises().equals(FirstOrderTheories.GROUP_AXIOMS);
    }

    private static Stream<Exercise> pureExercises() {
        return new FirstOrderExercises().exercises().stream().filter(exercise -> !isGroupExercise(exercise));
    }

    @Test
    void theCatalogHasPureExercises() {
        assertTrue(pureExercises().count() >= 10);
    }

    @TestFactory
    Stream<DynamicTest> everyPureExerciseIsSolved() {
        return pureExercises()
                .map(exercise -> DynamicTest.dynamicTest(exercise.id(),
                        () -> assertSolves(start(exercise.premises(), exercise.goal()))));
    }

    @TestFactory
    Stream<DynamicTest> theSolverStartsFromThePremises() {
        return pureExercises()
                .map(exercise -> DynamicTest.dynamicTest(exercise.id(), () -> {
                    // A proof with a detour: the solver drops it and starts again from the premises
                    var begin = start(exercise.premises(), exercise.goal());
                    var steps = new ArrayList<>(begin.steps());
                    steps.add(new StepDto("Q(a)", "Ass", 1, Map.of()));
                    var solved = solver.perform(new ProofDto(steps, LOGIC, exercise.goal()));
                    assertTrue(solved.isDone(), exercise.id());
                    assertEquals(begin.steps(), solved.steps().subList(0, begin.steps().size()));
                }));
    }

    @Test
    void aGroupExerciseThatNeedsNoRewritingInsideATermIsSolved() {
        var identity = new FirstOrderExercises().exercises().stream()
                .filter(exercise -> exercise.id().equals("group-identity-unique")).findFirst().orElseThrow();

        assertSolves(start(identity.premises(), identity.goal()));
    }

    @TestFactory
    Stream<DynamicTest> otherGoalsAreSolved() {
        return Stream.of(
                        // ∃E, then ∃I with the witness
                        start(List.of("exists x. P(x)", "forall x. P(x) -> Q(x)"), "exists y. Q(y)"),
                        // De Morgan for the quantifiers, by contradiction
                        start(List.of("- forall x. P(x)"), "exists x. - P(x)"),
                        // The drinker paradox: by contradiction, with ∃I on a name the premises do not mention
                        start(List.of(), "exists x. P(x) -> (forall y. P(y))"),
                        // A propositional tautology
                        start(List.of(), "((p -> q) -> p) -> p"),
                        // A line that is the goal only up to renaming: the answer is done for the client too
                        start(List.of("(forall x. P(x)) & Q"), "forall y. P(y)"),
                        start(List.of("forall x. P(x)"), "forall y. P(y)"))
                .map(proof -> DynamicTest.dynamicTest(proof.goal(), () -> assertSolves(proof)));
    }

    @TestFactory
    Stream<DynamicTest> unprovableGoalsEndUnsolved() {
        return Stream.of(
                        start(List.of("exists x. P(x)"), "forall x. P(x)"),
                        start(List.of("P(a)"), "P(b)"),
                        start(List.of("forall y. exists x. R(x, y)"), "exists x. forall y. R(x, y)"),
                        start(List.of(), "a = b"))
                .map(proof -> DynamicTest.dynamicTest(proof.goal(), () -> assertNotSolved(proof)));
    }

    private void assertSolves(ProofDto proof) {
        var solved = assertTimeoutPreemptively(Duration.ofSeconds(5), () -> solver.perform(proof));
        assertTrue(solved.isDone(), proof.goal() + ": done");
        var last = solved.steps().getLast();
        assertEquals(proof.goal(), last.expression(), proof.goal() + ": the last line is the goal");
        assertEquals(0, last.assmsLevel());
        assertEquals(proof.steps(), solved.steps().subList(0, proof.steps().size()), proof.goal() + ": the premises stay");
        // The result replays through the transformer, which reads only the rules of the logic, and is done
        var replayed = transformer.from(solved);
        assertTrue(replayed.isDone(), proof.goal() + ": replays and is done");
        assertEquals(solved, transformer.fromProof(replayed));
    }

    private void assertNotSolved(ProofDto proof) {
        var solved = assertTimeoutPreemptively(Duration.ofSeconds(5), () -> solver.perform(proof));
        assertFalse(solved.isDone(), proof.goal());
        assertEquals(proof.steps(), solved.steps(), proof.goal() + ": the proof is left with its premises");
    }
}
