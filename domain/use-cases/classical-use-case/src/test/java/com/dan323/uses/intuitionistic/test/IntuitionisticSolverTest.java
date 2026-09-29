package com.dan323.uses.intuitionistic.test;

import com.dan323.model.ProofDto;
import com.dan323.model.StepDto;
import com.dan323.uses.Exercise;
import com.dan323.uses.LogicalSolver;
import com.dan323.uses.intuitionistic.IntuitionisticExercises;
import com.dan323.uses.intuitionistic.IntuitionisticProofTransformer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class IntuitionisticSolverTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    /**
     * Exercises the solver does not finish: the goal-directed algorithm reaches a variable only with a rule for it,
     * and without proof by contradiction it has none that does a case analysis on {@code p | q} or uses {@code FE}.
     */
    private static final Set<String> UNSOLVED_EXERCISES = Set.of("or-commutes", "ex-falso");

    private final IntuitionisticProofTransformer transformer = new IntuitionisticProofTransformer();
    private final LogicalSolver<?, ?, ?, ?> solver = new LogicalSolver<>(transformer, TIMEOUT);

    private static ProofDto start(List<String> premises, String goal) {
        var steps = premises.stream().map(premise -> new StepDto(premise, "Ass", 0, Map.of())).toList();
        return new ProofDto(steps, "intuitionistic", goal);
    }

    private static Stream<Exercise> exercises(boolean solved) {
        return new IntuitionisticExercises().exercises().stream()
                .filter(exercise -> UNSOLVED_EXERCISES.contains(exercise.id()) != solved);
    }

    @TestFactory
    Stream<DynamicTest> everyExerciseIsSolvedWithoutDoubleNegationElimination() {
        return exercises(true).map(exercise -> DynamicTest.dynamicTest(exercise.id(), () -> assertSolves(exercise)));
    }

    @TestFactory
    Stream<DynamicTest> theUnsolvedExercisesStopWithTheirPremises() {
        assertEquals(UNSOLVED_EXERCISES.size(), exercises(false).count());
        return exercises(false).map(exercise -> DynamicTest.dynamicTest(exercise.id(),
                () -> assertNotSolved(start(exercise.premises(), exercise.goal()))));
    }

    @TestFactory
    Stream<DynamicTest> theSolverStartsFromThePremises() {
        return exercises(true)
                .map(exercise -> DynamicTest.dynamicTest(exercise.id(), () -> {
                    // A proof with a detour: the solver drops it and starts again from the premises
                    var steps = new ArrayList<>(start(exercise.premises(), exercise.goal()).steps());
                    steps.add(new StepDto("q", "Ass", 1, Map.of()));
                    var solved = solver.perform(new ProofDto(steps, "intuitionistic", exercise.goal()));
                    assertTrue(solved.isDone(), exercise.id());
                    assertEquals(start(exercise.premises(), exercise.goal()).steps(), solved.steps().subList(0, exercise.premises().size()));
                }));
    }

    @TestFactory
    Stream<DynamicTest> classicalOnlyGoalsAreNotSolved() {
        return Stream.of(
                        start(List.of(), "p | (- p)"),
                        start(List.of(), "(- (- p)) -> p"),
                        start(List.of(), "((p -> q) -> p) -> p"),
                        start(List.of("- (- p)"), "p"),
                        start(List.of(), "((- p) -> q) -> (p | q)"),
                        start(List.of(), "(- (p & q)) -> ((- p) | (- q))"),
                        start(List.of(), "(p -> q) -> ((- p) | q)"),
                        start(List.of(), "(p & q) | ((- p) | (- q))"))
                .map(proof -> DynamicTest.dynamicTest(proof.goal(), () -> assertNotSolved(proof)));
    }

    @TestFactory
    Stream<DynamicTest> intuitionisticGoalsAreSolved() {
        return Stream.of(
                        // A disjunction goal: its left side is attempted and reached
                        start(List.of(), "(p -> p) | q"),
                        // The left side fails, its steps are removed and the right side is reached
                        start(List.of(), "q | (p -> p)"),
                        start(List.of("p"), "(q & r) | (q | p)"),
                        start(List.of(), "- (- (p | (- p)))"),
                        start(List.of(), "(- (- (- p))) -> (- p)"),
                        start(List.of(), "(p -> (q -> r)) -> ((p & q) -> r)"),
                        start(List.of(), "(p -> q) -> ((- q) -> (- p))"),
                        start(List.of(), "(p -> FALSE) -> (- p)"),
                        // FALSE is the right side of an implication goal: ->I reaches it, not -I (#191)
                        start(List.of(), "(- p) -> (p -> FALSE)"),
                        start(List.of(), "((p -> q) & (q -> r)) -> (p -> r)"),
                        start(List.of(), "(p | q) -> (- ((- p) & (- q)))"),
                        start(List.of("p | q", "- p"), "q"),
                        start(List.of("- (p | q)"), "(- p) & (- q)"),
                        start(List.of("p", "p -> q", "q -> r"), "r"),
                        start(List.of("p"), "p"),
                        start(List.of("p", "q"), "p"),
                        start(List.of(), "p -> p"),
                        start(List.of(), "p -> (q -> p)"))
                .map(proof -> DynamicTest.dynamicTest(proof.goal(), () -> assertSolves(proof)));
    }

    /**
     * Intuitionistic theorems the solver gives up on, like the unsolved exercises. The sequent calculus solver that
     * came before it did prove them.
     */
    @TestFactory
    Stream<DynamicTest> knownLimitations() {
        return Stream.of(
                        start(List.of(), "- (- ((- (- p)) -> p))"),
                        start(List.of(), "((p | q) -> r) -> ((p -> r) & (q -> r))"),
                        start(List.of(), "((p -> r) & (q -> r)) -> ((p | q) -> r)"),
                        start(List.of(), "((p & q) -> r) -> (p -> (q -> r))"),
                        start(List.of(), "((((p -> q) -> p) -> p) -> q) -> q"),
                        start(List.of(), "(p | (q & r)) -> ((p | q) & (p | r))"),
                        start(List.of(), "((p | q) & (p | r)) -> (p | (q & r))"),
                        start(List.of(), "((p | q) | r) -> (p | (q | r))"),
                        start(List.of(), "((- p) | q) -> (p -> q)"),
                        start(List.of("FALSE"), "p & (- q)"))
                .map(proof -> DynamicTest.dynamicTest(proof.goal(), () -> assertNotSolved(proof)));
    }

    private void assertSolves(Exercise exercise) {
        assertSolves(start(exercise.premises(), exercise.goal()));
    }

    private void assertSolves(ProofDto proof) {
        var solved = assertTimeoutPreemptively(Duration.ofSeconds(2), () -> solver.perform(proof));
        assertTrue(solved.isDone(), proof.goal() + ": done");
        assertEquals(proof.goal(), solved.steps().getLast().expression(), proof.goal() + ": the last line is the goal");
        assertEquals(0, solved.steps().getLast().assmsLevel());
        assertEquals(proof.steps(), solved.steps().subList(0, proof.steps().size()), proof.goal() + ": the premises stay");
        solved.steps().forEach(step -> assertFalse(step.rule().startsWith("-E"), proof.goal() + ": no -E"));
        // The result replays through the intuitionistic transformer, which rejects -E, and is done
        var replayed = transformer.from(solved);
        assertTrue(replayed.isDone(), proof.goal() + ": replays and is done");
        assertEquals(solved, transformer.fromProof(replayed));
    }

    private void assertNotSolved(ProofDto proof) {
        var solved = assertTimeoutPreemptively(Duration.ofSeconds(2), () -> solver.perform(proof));
        assertFalse(solved.isDone(), proof.goal());
        assertEquals(proof.steps(), solved.steps(), proof.goal() + ": the proof is left with its premises");
    }
}
