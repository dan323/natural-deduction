package com.dan323.uses.modal.test;

import com.dan323.expressions.relation.RelationOperation;
import com.dan323.model.ProofDto;
import com.dan323.model.StepDto;
import com.dan323.proof.modal.proof.ParseModalAction;
import com.dan323.uses.Exercise;
import com.dan323.uses.LogicalSolver;
import com.dan323.uses.modal.ModalExercises;
import com.dan323.uses.modal.ModalProofTransformer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ModalSolverTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final ModalProofTransformer transformer = new ModalProofTransformer();
    private final LogicalSolver<?, ?, ?, ?> solver = new LogicalSolver<>(transformer, TIMEOUT);

    private static ProofDto start(List<String> premises, String goal) {
        var steps = premises.stream().map(premise -> {
            // A premise is in the initial state, a relation has no state
            Map<String, String> extra = ParseModalAction.parseExpression(premise) instanceof RelationOperation
                    ? Map.of() : Map.of("state", "s0");
            return new StepDto(premise, "Ass", 0, extra);
        }).toList();
        return new ProofDto(steps, "modal", goal);
    }

    @TestFactory
    Stream<DynamicTest> everyExerciseIsSolved() {
        return new ModalExercises().exercises().stream()
                .map(exercise -> DynamicTest.dynamicTest(exercise.id(), () -> assertSolves(exercise)));
    }

    @TestFactory
    Stream<DynamicTest> theSolverStartsFromThePremises() {
        return new ModalExercises().exercises().stream()
                .map(exercise -> DynamicTest.dynamicTest(exercise.id(), () -> {
                    // A proof with a detour: the solver drops it and starts again from the premises
                    var steps = new ArrayList<>(start(exercise.premises(), exercise.goal()).steps());
                    steps.add(new StepDto("q", "Ass", 1, Map.of("state", "s0")));
                    var solved = solver.perform(new ProofDto(steps, "modal", exercise.goal()));
                    assertTrue(solved.isDone(), exercise.id());
                    assertEquals(start(exercise.premises(), exercise.goal()).steps(), solved.steps().subList(0, exercise.premises().size()));
                }));
    }

    @TestFactory
    Stream<DynamicTest> goalsThatAreNotS4TheoremsAreNotSolved() {
        return Stream.of(
                        start(List.of(), "p -> ([] p)"),
                        start(List.of(), "(<> p) -> p"),
                        start(List.of(), "(<> p) -> ([] (<> p))"),
                        start(List.of(), "([] (p | q)) -> (([] p) | ([] q))"),
                        start(List.of(), "([] (<> p)) -> (<> ([] p))"),
                        start(List.of(), "(<> ([] p)) -> ([] (<> p))"),
                        start(List.of(), "([] (([] p) -> p)) -> ([] p)"),
                        start(List.of(), "([] (([] p) -> q)) | ([] (([] q) -> p))"),
                        start(List.of("[] (<> p)", "[] (<> q)"), "<> (p & q)"),
                        start(List.of("[] p", "s0 <= s1"), "q"))
                .map(proof -> DynamicTest.dynamicTest(proof.goal(), () -> {
                    var solved = assertTimeoutPreemptively(Duration.ofSeconds(2), () -> solver.perform(proof));
                    assertFalse(solved.isDone(), proof.goal());
                    assertEquals(proof.steps(), solved.steps(), "the proof is left with its premises");
                }));
    }

    @TestFactory
    Stream<DynamicTest> harderS4TheoremsAreSolved() {
        return Stream.of(
                        start(List.of(), "([] (p -> q)) -> (([] p) -> ([] q))"),
                        start(List.of(), "([] (p -> q)) -> ((<> p) -> (<> q))"),
                        start(List.of(), "(<> (<> p)) -> (<> p)"),
                        start(List.of(), "([] p) -> ([] ([] p))"),
                        start(List.of(), "(- (<> p)) -> ([] (- p))"),
                        start(List.of(), "([] (- p)) -> (- (<> p))"),
                        start(List.of(), "(- ([] p)) -> (<> (- p))"),
                        start(List.of(), "((<> p) & ([] q)) -> (<> (p & q))"),
                        start(List.of(), "(<> (p | q)) -> ((<> p) | (<> q))"),
                        start(List.of(), "(([] p) | ([] q)) -> ([] (p | q))"),
                        start(List.of(), "(<> ([] (<> p))) -> (<> p)"),
                        start(List.of(), "([] (<> ([] (<> p)))) -> ([] (<> p))"),
                        start(List.of(), "([] (<> p)) -> ([] (<> ([] (<> p))))"),
                        start(List.of(), "[] (p | (- p))"),
                        start(List.of("[] p", "s0 <= s1"), "<> p"),
                        start(List.of("<> FALSE"), "q"),
                        start(List.of("p"), "p"))
                .map(proof -> DynamicTest.dynamicTest(proof.goal(), () -> assertSolves(proof)));
    }

    private void assertSolves(Exercise exercise) {
        assertSolves(start(exercise.premises(), exercise.goal()));
    }

    private void assertSolves(ProofDto proof) {
        var solved = solver.perform(proof);
        assertTrue(solved.isDone(), proof.goal() + ": done");
        var last = solved.steps().getLast();
        assertEquals(proof.goal(), last.expression(), proof.goal() + ": the last line is the goal");
        assertEquals("s0", last.extraParameters().get("state"), proof.goal() + ": the goal is in the initial state");
        assertEquals(0, last.assmsLevel());
        assertEquals(proof.steps(), solved.steps().subList(0, proof.steps().size()), proof.goal() + ": the premises stay");
        // The result replays through the transformer and is done
        var replayed = transformer.from(solved);
        assertTrue(replayed.isDone(), proof.goal() + ": replays and is done");
        assertEquals(solved, transformer.fromProof(replayed));
    }
}
