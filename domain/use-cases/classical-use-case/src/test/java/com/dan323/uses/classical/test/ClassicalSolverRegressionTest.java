package com.dan323.uses.classical.test;

import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.model.ProofDto;
import com.dan323.model.StepDto;
import com.dan323.uses.Exercise;
import com.dan323.uses.classical.ClassicalExercises;
import com.dan323.uses.classical.ClassicalProofTransformer;
import com.dan323.uses.intuitionistic.IntuitionisticExercises;
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
 * The classical solver must keep producing exactly the proofs it produced before the goal-directed engine was shared
 * with the intuitionistic solver. The expected proofs in {@code classical-solver-proofs.txt} were written by the
 * original {@code ClassicalAutomate} (origin/master before #185) with {@code -DwriteSolverProofs=true}; each block is
 * the goal, the premises and the proof the solver left, finished or not.
 */
class ClassicalSolverRegressionTest {

    private static final Path EXPECTED = Path.of("src", "test", "resources", "classical-solver-proofs.txt");

    // Left out because the classical solver does not stop on them (it keeps adding steps), so there is no proof to
    // compare: (- p) -> (p -> FALSE), (p | (q & r)) -> ((p | q) & (p | r)), ((p | q) & (p | r)) -> (p | (q & r)) and
    // ((p | q) | r) -> (p | (q | r)).
    private static final List<String> EXTRA_GOALS = List.of(
            "p | (- p)",
            "(- (- p)) -> p",
            "((p -> q) -> p) -> p",
            "((- p) -> q) -> (p | q)",
            "(- (p & q)) -> ((- p) | (- q))",
            "(p -> p) | q",
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
            "(p & q) | ((- p) | (- q))",
            "((p -> q) & (q -> r)) -> (p -> r)",
            "(p | q) -> (q | p)",
            "q | ((- q) & p)",
            "TRUE",
            "FALSE -> p");

    private static final List<List<String>> EXTRA_PREMISES = List.of(
            List.of("- (- p)"),
            List.of("p | q", "- p"),
            List.of("FALSE"),
            List.of("p", "- p"),
            List.of("(p | q) -> r", "q"),
            List.of("(p -> q) -> r", "q"),
            List.of("- (p & q)"),
            List.of("p -> q", "- q"));

    private static final List<String> EXTRA_PREMISE_GOALS = List.of(
            "p", "q", "p & (- q)", "q & r", "r", "r", "(- p) | (- q)", "- p");

    private record Input(List<String> premises, String goal) {
    }

    private static Set<Input> inputs() {
        Set<Input> inputs = new LinkedHashSet<>();
        Stream.concat(new ClassicalExercises().exercises().stream(), new IntuitionisticExercises().exercises().stream())
                .map(ClassicalSolverRegressionTest::input)
                .forEach(inputs::add);
        EXTRA_GOALS.forEach(goal -> inputs.add(new Input(List.of(), goal)));
        for (int i = 0; i < EXTRA_PREMISES.size(); i++) {
            inputs.add(new Input(EXTRA_PREMISES.get(i), EXTRA_PREMISE_GOALS.get(i)));
        }
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
        var steps = input.premises().stream().map(premise -> new StepDto(premise, "Ass", 0, Map.of())).toList();
        NaturalDeduction proof = new ClassicalProofTransformer().from(new ProofDto(steps, "classical", input.goal()));
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
