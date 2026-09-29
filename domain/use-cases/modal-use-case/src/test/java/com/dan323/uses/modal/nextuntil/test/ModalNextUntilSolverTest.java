package com.dan323.uses.modal.nextuntil.test;

import com.dan323.model.ProofDto;
import com.dan323.model.StepDto;
import com.dan323.uses.Exercise;
import com.dan323.uses.LogicalSolver;
import com.dan323.uses.modal.nextuntil.ModalNextUntilExercises;
import com.dan323.uses.modal.nextuntil.ModalNextUntilProofTransformer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ModalNextUntilSolverTest {

    private static final String LOGIC = "modal-next-until";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final ModalNextUntilProofTransformer transformer = new ModalNextUntilProofTransformer();
    private final LogicalSolver<?, ?, ?, ?> solver = new LogicalSolver<>(transformer, TIMEOUT);

    private static ProofDto start(List<String> premises, String goal) {
        var steps = premises.stream().map(premise -> new StepDto(premise, "Ass", 0, Map.of("state", "s0"))).toList();
        return new ProofDto(steps, LOGIC, goal);
    }

    private static boolean usesInduction(Exercise exercise) {
        return exercise.solution().lines().anyMatch(line -> line.trim().endsWith("]") && line.contains(" Ind ["));
    }

    private static Stream<Exercise> exercises() {
        return new ModalNextUntilExercises().exercises().stream();
    }

    @Test
    void theCatalogHasExercisesWithAndWithoutInduction() {
        assertTrue(exercises().anyMatch(ModalNextUntilSolverTest::usesInduction));
        assertTrue(exercises().anyMatch(exercise -> !usesInduction(exercise)));
    }

    @TestFactory
    Stream<DynamicTest> everyExerciseWithoutInductionIsSolved() {
        return exercises()
                .filter(exercise -> !usesInduction(exercise))
                .map(exercise -> DynamicTest.dynamicTest(exercise.id(),
                        () -> assertSolves(start(exercise.premises(), exercise.goal()))));
    }

    @TestFactory
    Stream<DynamicTest> anExerciseThatNeedsInductionIsNotSolved() {
        return exercises()
                .filter(ModalNextUntilSolverTest::usesInduction)
                .map(exercise -> DynamicTest.dynamicTest(exercise.id(),
                        () -> assertNotSolved(start(exercise.premises(), exercise.goal()))));
    }

    @TestFactory
    Stream<DynamicTest> theSolverStartsFromThePremises() {
        return exercises()
                .filter(exercise -> !usesInduction(exercise))
                .map(exercise -> DynamicTest.dynamicTest(exercise.id(), () -> {
                    // A proof with a detour: the solver drops it and starts again from the premises
                    var begin = start(exercise.premises(), exercise.goal());
                    var steps = new ArrayList<>(begin.steps());
                    steps.add(new StepDto("r", "Ass", 1, Map.of("state", "s0")));
                    var solved = solver.perform(new ProofDto(steps, LOGIC, exercise.goal()));
                    assertTrue(solved.isDone(), exercise.id());
                    assertEquals(begin.steps(), solved.steps().subList(0, begin.steps().size()));
                }));
    }

    @TestFactory
    Stream<DynamicTest> otherGoalsAreSolved() {
        return Stream.of(
                        // Until: UI1 is tried first and fails, then UI2 through two successors
                        start(List.of("p", "X p", "X (X q)"), "p U q"),
                        // Until inside Next, and Next inside Until
                        start(List.of("X q"), "X (p U q)"),
                        start(List.of("q"), "(X p) U q"),
                        // Until whose right side is reached with an introduction rule
                        start(List.of("q", "r"), "p U (q & r)"),
                        // An implication goal and Next inside it
                        start(List.of(), "(X p) -> (X (p | q))"),
                        start(List.of(), "(X (p & q)) -> (X p)"),
                        // Until elimination: now, or later
                        start(List.of("p U q", "- q"), "X (p U q)"),
                        // A modal goal, solved as the modal solver does
                        start(List.of("[] (p -> q)", "[] p"), "[] q"),
                        start(List.of("[] p"), "X (X p)"),
                        // DeMorgan on a negated <> of a negation, then []E and -E
                        start(List.of("- (<> (- p))"), "[] p"),
                        // By contradiction: the negated goal is split by De Morgan, and FALSE comes from - A, not from
                        // the negated goal itself
                        start(List.of(), "(([] p) -> (p U q)) | (- q)"))
                .map(proof -> DynamicTest.dynamicTest(proof.goal(), () -> assertSolves(proof)));
    }

    @TestFactory
    Stream<DynamicTest> unprovableGoalsEndUnsolved() {
        return Stream.of(
                        start(List.of("X p"), "p"),
                        start(List.of("p"), "X p"),
                        start(List.of("p U q"), "q"),
                        start(List.of("p"), "p U q"),
                        start(List.of("<> q"), "p U q"))
                .map(proof -> DynamicTest.dynamicTest(proof.goal(), () -> assertNotSolved(proof)));
    }

    @TestFactory
    Stream<DynamicTest> theSharedModalRulesDoNotBreakTheSolver() {
        return Stream.of(
                        // DeMorgan on a negated <> that is not the last line
                        start(List.of("- (<> p)", "p -> q"), "- r"),
                        // ModalOrE2: the right side of the disjunction is refuted
                        start(List.of("q", "(r | q) & (- q)"), "(r | r) U (p U (r U r))"),
                        // A relation goal
                        start(List.of(), "s0 <= s0+1"))
                .map(proof -> DynamicTest.dynamicTest(proof.goal(), () -> {
                    var solved = assertTimeoutPreemptively(Duration.ofSeconds(5), () -> solver.perform(proof));
                    assertEquals(proof.steps(), solved.steps().subList(0, proof.steps().size()), proof.goal());
                    // Whatever the solver found replays through the transformer
                    var replayed = transformer.from(solved);
                    assertEquals(solved.isDone(), replayed.isDone(), proof.goal());
                    assertEquals(solved, transformer.fromProof(replayed));
                }));
    }

    private void assertSolves(ProofDto proof) {
        var solved = assertTimeoutPreemptively(Duration.ofSeconds(5), () -> solver.perform(proof));
        assertTrue(solved.isDone(), proof.goal() + ": done");
        var last = solved.steps().getLast();
        assertEquals(proof.goal(), last.expression(), proof.goal() + ": the last line is the goal");
        assertEquals(0, last.assmsLevel());
        assertEquals("s0", last.extraParameters().get("state"), proof.goal() + ": in s0");
        assertEquals(proof.steps(), solved.steps().subList(0, proof.steps().size()), proof.goal() + ": the premises stay");
        solved.steps().forEach(step -> assertFalse(step.rule().startsWith("Ind"), proof.goal() + ": no Ind"));
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
