package com.dan323.uses.classical.test;

import com.dan323.model.ProofDto;
import com.dan323.model.StepDto;
import com.dan323.uses.Exercise;
import com.dan323.uses.LogicalSolver;
import com.dan323.uses.classical.ClassicalExercises;
import com.dan323.uses.classical.ClassicalProofTransformer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ClassicalSolverTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final ClassicalProofTransformer transformer = new ClassicalProofTransformer();
    private final LogicalSolver<?, ?, ?, ?> solver = new LogicalSolver<>(transformer, TIMEOUT);

    private static ProofDto start(List<String> premises, String goal) {
        var steps = premises.stream().map(premise -> new StepDto(premise, "Ass", 0, Map.of())).toList();
        return new ProofDto(steps, "classical", goal);
    }

    @TestFactory
    Stream<DynamicTest> everyExerciseIsSolved() {
        return new ClassicalExercises().exercises().stream()
                .map(exercise -> DynamicTest.dynamicTest(exercise.id(), () -> assertSolves(exercise)));
    }

    @TestFactory
    Stream<DynamicTest> theSolverStartsFromThePremises() {
        return new ClassicalExercises().exercises().stream()
                .map(exercise -> DynamicTest.dynamicTest(exercise.id(), () -> {
                    // A proof with a detour: the solver drops it and starts again from the premises
                    var steps = new ArrayList<>(start(exercise.premises(), exercise.goal()).steps());
                    steps.add(new StepDto("q", "Ass", 1, Map.of()));
                    var solved = solver.perform(new ProofDto(steps, "classical", exercise.goal()));
                    assertTrue(solved.isDone(), exercise.id());
                    assertEquals(start(exercise.premises(), exercise.goal()).steps(), solved.steps().subList(0, exercise.premises().size()));
                }));
    }

    @TestFactory
    Stream<DynamicTest> nonTautologiesAreNotSolved() {
        return Stream.of(
                        start(List.of(), "p"),
                        start(List.of(), "p -> q"),
                        start(List.of(), "(p -> q) -> (q -> p)"),
                        start(List.of(), "(p | q) -> (p & q)"),
                        start(List.of(), "((p -> q) -> p) -> q"),
                        start(List.of("p | q"), "p"),
                        start(List.of("p -> q", "- p"), "- q"),
                        start(List.of("TRUE"), "- (- TRUE) & FALSE"),
                        start(List.of("(a | b) & ((c | d) & ((e | f) & (g | h)))"), "(a & c) | (e & g)"))
                .map(proof -> DynamicTest.dynamicTest(proof.goal(), () -> {
                    var solved = assertTimeoutPreemptively(Duration.ofSeconds(2), () -> solver.perform(proof));
                    assertFalse(solved.isDone(), proof.goal());
                    assertEquals(proof.steps(), solved.steps(), "the proof is left with its premises");
                }));
    }

    @TestFactory
    Stream<DynamicTest> harderTautologiesAreSolved() {
        return Stream.of(
                        start(List.of(), "((p -> q) -> p) -> p"),
                        start(List.of(), "(- (p & q)) -> ((- p) | (- q))"),
                        start(List.of(), "((- p) | (- q)) -> (- (p & q))"),
                        start(List.of(), "(- (p | q)) -> ((- p) & (- q))"),
                        start(List.of(), "((- q) -> (- p)) -> (p -> q)"),
                        start(List.of(), "(p -> q) | (q -> p)"),
                        start(List.of(), "(p -> q) -> ((- p) | q)"),
                        start(List.of(), "((p -> q) -> q) -> ((q -> p) -> p)"),
                        start(List.of(), "(p | (q & r)) -> ((p | q) & (p | r))"),
                        start(List.of(), "((p | q) & (p | r)) -> (p | (q & r))"),
                        start(List.of(), "(p & (q | r)) -> ((p & q) | (p & r))"),
                        start(List.of(), "- (- ((- (- p)) -> p))"),
                        start(List.of("p -> (q | r)", "q -> s", "r -> s"), "p -> s"),
                        start(List.of("(p & q) -> r", "- r", "p"), "- q"),
                        start(List.of("FALSE"), "p & (- q)"),
                        start(List.of("p", "- p"), "FALSE"),
                        start(List.of("p"), "p"),
                        start(List.of("p", "q"), "p"),
                        start(List.of(), "TRUE -> TRUE"))
                .map(proof -> DynamicTest.dynamicTest(proof.goal(), () -> assertSolves(proof)));
    }

    private void assertSolves(Exercise exercise) {
        assertSolves(start(exercise.premises(), exercise.goal()));
    }

    private void assertSolves(ProofDto proof) {
        var solved = solver.perform(proof);
        assertTrue(solved.isDone(), proof.goal() + ": done");
        assertEquals(proof.goal(), solved.steps().getLast().expression(), proof.goal() + ": the last line is the goal");
        assertEquals(0, solved.steps().getLast().assmsLevel());
        assertEquals(proof.steps(), solved.steps().subList(0, proof.steps().size()), proof.goal() + ": the premises stay");
        // The result replays through the transformer and is done
        var replayed = transformer.from(solved);
        assertTrue(replayed.isDone(), proof.goal() + ": replays and is done");
        assertEquals(solved, transformer.fromProof(replayed));
    }
}
