package com.dan323.uses.modal.test;

import com.dan323.model.ProofDto;
import com.dan323.model.StepDto;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.uses.Exercise;
import com.dan323.uses.modal.ModalExercises;
import com.dan323.uses.modal.ModalProofTransformer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The modal solver must keep producing exactly the proofs it produced before #191 was fixed. The expected proofs in
 * {@code modal-solver-proofs.txt} were written by {@code ModalAutomate} before that fix with
 * {@code -DwriteSolverProofs=true}; each block is the goal, the premises (all in {@code s0}) and the proof the solver
 * left, finished or not. The only changes are the {@code FI} lines of {@code ModalOrE2}, which cited the disjunction
 * instead of the negation (so the proof did not replay) until that was fixed in #196.
 */
class ModalSolverRegressionTest {

    private static final Path EXPECTED = Path.of("src", "test", "resources", "modal-solver-proofs.txt");

    // Left out because the modal solver does not finish them: (p -> p) | q, (p & q) | ((- p) | (- q)) and
    // q | ((- q) & p) keep adding steps.
    private static final List<String> EXTRA_GOALS = List.of(
            "p | (- p)",
            "(- (- p)) -> p",
            "((p -> q) -> p) -> p",
            "((- p) -> q) -> (p | q)",
            "(- (p & q)) -> ((- p) | (- q))",
            "- (- (p | (- p)))",
            "- (- ((- (- p)) -> p))",
            "(- (- (- p))) -> (- p)",
            "((p | q) -> r) -> ((p -> r) & (q -> r))",
            "((p -> r) & (q -> r)) -> ((p | q) -> r)",
            "((p & q) -> r) -> (p -> (q -> r))",
            "(p -> (q -> r)) -> ((p & q) -> r)",
            "((((p -> q) -> p) -> p) -> q) -> q",
            "(p -> q) -> ((- q) -> (- p))",
            "((- p) | q) -> (p -> q)",
            "(p -> q) -> ((- p) | q)",
            "(p -> FALSE) -> (- p)",
            "p -> (q -> p)",
            "((p -> q) & (q -> r)) -> (p -> r)",
            "(p | q) -> (q | p)",
            "TRUE",
            "FALSE -> p",
            "([] (p -> q)) -> (([] p) -> ([] q))",
            "([] p) -> p",
            "p -> (<> p)",
            "([] p) -> (<> p)",
            "(- (<> p)) -> ([] (- p))",
            "([] (p & q)) -> (([] p) & ([] q))");

    private static final List<List<String>> EXTRA_PREMISES = List.of(
            List.of("- (- p)"),
            List.of("p | q", "- p"),
            List.of("FALSE"),
            List.of("p", "- p"),
            List.of("(p | q) -> r", "q"),
            List.of("(p -> q) -> r", "q"),
            List.of("- (p & q)"),
            List.of("p -> q", "- q"),
            List.of("[] p", "[] q"),
            List.of("<> p", "[] (p -> q)"));

    private static final List<String> EXTRA_PREMISE_GOALS = List.of(
            "p", "q", "p & (- q)", "q & r", "r", "r", "(- p) | (- q)", "- p", "[] (p & q)", "<> q");

    /**
     * Goals the modal solver did not stop on before #191 was fixed. Their proofs were appended to the end of
     * {@code modal-solver-proofs.txt} after the fix; the proofs above them are still the ones from before it.
     */
    private static final List<String> NO_LONGER_LOOPING = List.of("(- p) -> (p -> FALSE)");

    private static final String SOMETIME_OVER_OR = "(<> (p | q)) -> ((<> p) | (<> q))";

    private record Input(List<String> premises, String goal) {
    }

    private static Set<Input> inputs() {
        Set<Input> inputs = new LinkedHashSet<>();
        new ModalExercises().exercises().stream().map(ModalSolverRegressionTest::input).forEach(inputs::add);
        EXTRA_GOALS.forEach(goal -> inputs.add(new Input(List.of(), goal)));
        for (int i = 0; i < EXTRA_PREMISES.size(); i++) {
            inputs.add(new Input(EXTRA_PREMISES.get(i), EXTRA_PREMISE_GOALS.get(i)));
        }
        NO_LONGER_LOOPING.forEach(goal -> inputs.add(new Input(List.of(), goal)));
        inputs.add(new Input(List.of(), SOMETIME_OVER_OR));
        return inputs;
    }

    private static Input input(Exercise exercise) {
        return new Input(exercise.premises(), exercise.goal());
    }

    private static String header(Input input) {
        StringBuilder sb = new StringBuilder("goal: ").append(input.goal()).append('\n');
        input.premises().forEach(premise -> sb.append("premise: ").append(premise).append('\n'));
        return sb.append("proof:\n").toString();
    }

    private static String solve(Input input) {
        var steps = input.premises().stream().map(premise -> new StepDto(premise, "Ass", 0, Map.of("state", "s0"))).toList();
        ModalNaturalDeduction proof = new ModalProofTransformer().from(new ProofDto(steps, "modal", input.goal()));
        assertTimeoutPreemptively(Duration.ofSeconds(10), proof::automate);
        return header(input) + proof + "end\n";
    }

    private static List<String> expectedBlocks() throws IOException {
        List<String> blocks = new ArrayList<>();
        StringBuilder block = new StringBuilder();
        for (String line : Files.readAllLines(EXPECTED, StandardCharsets.UTF_8)) {
            block.append(line).append('\n');
            if (line.equals("end")) {
                blocks.add(block.toString());
                block = new StringBuilder();
            }
        }
        return blocks;
    }

    @Test
    void writeTheExpectedProofs() throws IOException {
        if (Boolean.getBoolean("writeSolverProofs")) {
            StringBuilder sb = new StringBuilder();
            inputs().forEach(input -> sb.append(solve(input)));
            Files.writeString(EXPECTED, sb.toString(), StandardCharsets.UTF_8);
        }
        assertTrue(Files.exists(EXPECTED));
    }

    @Test
    void everyInputHasAnExpectedProof() throws IOException {
        var blocks = expectedBlocks();
        for (Input input : inputs()) {
            assertTrue(blocks.stream().anyMatch(block -> block.startsWith(header(input))), header(input));
        }
    }

    @TestFactory
    Stream<DynamicTest> theSolverNoLongerLoops() {
        var transformer = new ModalProofTransformer();
        return NO_LONGER_LOOPING.stream().map(goal -> DynamicTest.dynamicTest(goal, () -> {
            ModalNaturalDeduction proof = transformer.from(new ProofDto(List.of(), "modal", goal));
            assertTimeoutPreemptively(Duration.ofSeconds(2), proof::automate);
            assertTrue(proof.isDone(), goal);
            var last = proof.getSteps().getLast();
            assertEquals(goal, last.getStep().toString());
            assertEquals(0, last.getAssumptionLevel());
            assertEquals(proof.getState0(), last.getState(), goal + ": derived in the initial state");
            assertTrue(transformer.from(transformer.fromProof(proof)).isDone(), goal + ": replays and is done");
        }));
    }

    /**
     * The solver used to throw a ClassCastException here (#193): it took a line for a relation without checking it.
     */
    @Test
    void sometimeOverOrDoesNotThrow() {
        assertProved(List.of(), SOMETIME_OVER_OR);
    }

    /**
     * {@code Refl} was applied again and again when the line after its source line was a discharged step (here the
     * {@code s0 <= s1} that {@code DeMorgan} opens), so the solver never stopped; and {@code DeMorgan} on
     * {@code - (<> (- p))} gave {@code [] (- (- p))} instead of {@code [] p}.
     */
    @Test
    void theDualOfSometimeIsProved() {
        assertProved(List.of("- (<> (- p))"), "[] p");
    }

    /**
     * The negated goal assumed for a proof by contradiction is split by De Morgan; FALSE must then come from the
     * lines that gave, not from aiming for the goal itself again.
     */
    @Test
    void aDeMorganedLineIsNotAimedAt() {
        assertProved(List.of(), "(([] p) -> (<> q)) | (- q)");
    }

    /**
     * Like {@link #aDeMorganedLineIsNotAimedAt()}, for a {@code - (<> A)} line that {@code DeMorgan} turned into
     * {@code [] (- A)}: without skipping it the solver aims for {@code <> q} again and does not finish.
     */
    @Test
    void aLineDeMorganedIntoAlwaysIsNotAimedAt() {
        assertProved(List.of(), "(<> q) | (([] p) -> ([] (- q)))");
    }

    private static void assertProved(List<String> premises, String goal) {
        var transformer = new ModalProofTransformer();
        var steps = premises.stream().map(premise -> new StepDto(premise, "Ass", 0, Map.of("state", "s0"))).toList();
        ModalNaturalDeduction proof = transformer.from(new ProofDto(steps, "modal", goal));
        assertTimeoutPreemptively(Duration.ofSeconds(2), proof::automate);
        assertTrue(proof.isDone(), goal);
        var last = proof.getSteps().getLast();
        assertEquals(goal, last.getStep().toString());
        assertEquals(0, last.getAssumptionLevel());
        assertEquals(proof.getState0(), last.getState());
        assertTrue(transformer.from(transformer.fromProof(proof)).isDone(), goal + ": replays and is done");
    }

    @TestFactory
    Stream<DynamicTest> theSolverGivesTheSameProofs() throws IOException {
        return expectedBlocks().stream().map(block -> DynamicTest.dynamicTest(block.lines().findFirst().orElseThrow(), () -> {
            var lines = block.lines().toList();
            String goal = lines.get(0).substring("goal: ".length());
            List<String> premises = lines.stream().takeWhile(line -> !line.equals("proof:"))
                    .filter(line -> line.startsWith("premise: ")).map(line -> line.substring("premise: ".length())).toList();
            assertEquals(block, solve(new Input(premises, goal)));
        }));
    }
}
