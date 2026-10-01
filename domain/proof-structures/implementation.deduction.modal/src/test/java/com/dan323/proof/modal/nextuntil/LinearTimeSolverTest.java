package com.dan323.proof.modal.nextuntil;

import com.dan323.expressions.ModalLogicParser;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.proof.modal.complex.DeMorgan;
import com.dan323.proof.modal.complex.ModalOrE2;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CancellationException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The solver of {@code modal-next-until} on goals of each shape, and the modal fixes it relies on. The use-case module
 * checks it on the exercises and against the semantics; these cover each of its procedures.
 */
class LinearTimeSolverTest {

    private static ModalOperation parse(String formula) {
        return ParseModalNextUntilAction.parseExpression(formula);
    }

    private static ModalNextUntilNaturalDeduction solve(String goal, String... premises) {
        var proof = new ModalNextUntilNaturalDeduction("s0");
        proof.initializeProof(List.of(premises).stream().map(LinearTimeSolverTest::parse).toList(), parse(goal));
        proof.automate();
        return proof;
    }

    /** Solved, with the goal on the last line, in {@code s0}, and every line replays through the rules. */
    private static void assertSolved(String goal, String... premises) {
        var proof = solve(goal, premises);
        assertTrue(proof.isDone(), goal);
        var last = proof.getSteps().getLast();
        assertEquals(parse(goal), last.getStep(), goal);
        assertEquals(0, last.getAssumptionLevel(), goal);
        var actions = proof.parse();
        var replayed = new ModalNextUntilNaturalDeduction("s0");
        replayed.initializeProof(List.of(premises).stream().map(LinearTimeSolverTest::parse).toList(), parse(goal));
        for (int i = premises.length; i < actions.size(); i++) {
            assertTrue(actions.get(i).isValid(replayed), goal + ": line " + (i + 1) + " " + proof.getSteps().get(i));
            actions.get(i).apply(replayed);
        }
        assertTrue(replayed.isDone(), goal + ": the replay is done");
    }

    private static void assertNotSolved(String goal, String... premises) {
        var proof = solve(goal, premises);
        assertFalse(proof.isDone(), goal);
        assertEquals(premises.length, proof.getSteps().size(), goal + ": only the premises are left");
    }

    @Test
    void goalsOfEveryShape() {
        assertSolved("p -> p");
        assertSolved("p | q", "p");
        assertSolved("p | q", "q");
        assertSolved("p | (- p)");
        assertSolved("p & q", "q", "p");
        assertSolved("- (p & (- p))");
        assertSolved("X (p | q)", "X p");
        assertSolved("[] (p | q)", "[] p");
        assertSolved("<> p", "p");
        assertSolved("<> p", "X p");
        assertSolved("p U q", "q");
    }

    @Test
    void eliminations() {
        assertSolved("p", "- (- p)");
        assertSolved("- p", "- (p | q)");
        assertSolved("[] (- p)", "- (<> p)");
        assertSolved("[] p", "- (<> (- p))");
        assertSolved("<> q", "<> p", "[] (p -> q)");
        assertSolved("X q", "X p", "[] (p -> q)");
        assertSolved("<> q", "p U q");
        assertSolved("p", "p U q", "- q");
    }

    @Test
    void relationGoals() {
        assertSolved("s0 <= s0+2");
        assertSolved("s0 <= s0", "p");
        assertNotSolved("s0 <= s1", "p");
        assertNotSolved("s0+1 <= s0", "p");
    }

    @Test
    void linearTimeAndInduction() {
        assertSolved("(p U q) -> (p U (q | r))");
        assertSolved("X (<> p)", "<> (X p)");
        assertSolved("[] p", "p", "[] (p -> (X p))");
        assertNotSolved("p U q", "<> q");
        assertNotSolved("X p", "p");
    }

    @Test
    void anInterruptedSolverStops() {
        var proof = new ModalNextUntilNaturalDeduction("s0");
        proof.initializeProof(List.of(), parse("(p U q) -> (p U (q | r))"));
        Thread.currentThread().interrupt();
        try {
            assertThrows(CancellationException.class, proof::automate);
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void theClientNamesTheNewRules() {
        var relation = parse("s0 <= s1");
        assertEquals(new ModalNegatedUntil(1), ParseModalNextUntilAction.parseAction("-U", List.of(1), null, null));
        assertEquals(new ModalUntilWitness(1, "s1"), ParseModalNextUntilAction.parseAction("UW", List.of(1), null, "s1"));
        assertEquals(new ModalUntilWitnessRight(2), ParseModalNextUntilAction.parseAction("UB", List.of(2), null, null));
        assertEquals(new ModalUntilWitnessLeft(2, "s0"), ParseModalNextUntilAction.parseAction("UA", List.of(2), null, "s0"));
        assertEquals(new ModalOrder(relation), ParseModalNextUntilAction.parseAction("Ord", List.of(), relation, null));
        assertEquals(new ModalEqualState(1, "s1"), ParseModalNextUntilAction.parseAction("Eq", List.of(1), null, "s1"));
        assertEquals(new ModalLinearity(), ParseModalNextUntilAction.parseAction("Lin", List.of(), null, null));
    }

    @Test
    void aStepWithTheWrongShapeIsNotRead() {
        var proof = new ModalNextUntilNaturalDeduction("s0");
        var formula = parse("p");
        var witness = ParseModalNextUntilAction.parseReason("UW [1]");
        assertThrows(IllegalArgumentException.class, () -> ParseModalNextUntilAction.parseWithReason(proof, formula, witness, "s0"));
        var noLines = ParseModalNextUntilAction.parseReason("Ord");
        var equal = new com.dan323.proof.generic.proof.ProofReason("Eq", List.of(), List.of());
        assertEquals(new ModalOrder(List.of(), formula), ParseModalNextUntilAction.parseWithReason(proof, formula, noLines, null));
        assertThrows(IllegalArgumentException.class, () -> ParseModalNextUntilAction.parseWithReason(proof, formula, equal, "s0"));
    }

    // ---------------------------------------------------------------- the modal fixes the solver relies on

    private static final ModalLogicParser MODAL = new ModalLogicParser();

    private static ModalNaturalDeduction modal(String goal, String... premises) {
        var proof = new ModalNaturalDeduction("s0");
        proof.initializeProof(List.of(premises).stream().map(p -> (ModalOperation) MODAL.evaluate(p)).toList(),
                MODAL.evaluate(goal));
        return proof;
    }

    @Test
    void deMorganOnTheNegationOfAPossibleNegationGivesAlways() {
        var proof = modal("[] p", "- (<> (- p))");
        var rule = new DeMorgan(1);
        assertTrue(rule.isValid(proof));
        rule.apply(proof);
        assertEquals(MODAL.evaluate("[] p"), proof.getSteps().getLast().getStep());
        assertEquals("-E [6]", proof.getSteps().get(proof.getSteps().size() - 2).getProof().toString());
    }

    @Test
    void theRightSideOfADisjunctionRefutedGivesItsLeftSide() {
        var proof = modal("p", "p | q", "- q");
        var rule = new ModalOrE2(1, 2);
        assertTrue(rule.isValid(proof));
        rule.apply(proof);
        assertEquals(MODAL.evaluate("p"), proof.getSteps().getLast().getStep());
        // Every line it wrote replays: the FI line cites the negation it contradicts
        var actions = proof.parse();
        var replayed = modal("p", "p | q", "- q");
        for (int i = 2; i < actions.size(); i++) {
            assertTrue(actions.get(i).isValid(replayed), proof.getSteps().get(i).toString());
            actions.get(i).apply(replayed);
        }
    }

    @Test
    void theModalSolverLeavesARelationGoalItCannotReach() {
        var proof = modal("s0 <= s1", "p");
        assertDoesNotThrow(proof::automate);
        assertFalse(proof.isDone());
        assertInstanceOf(ModalLogicalOperation.class, proof.getSteps().getFirst().getStep());
    }
}
