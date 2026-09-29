package com.dan323.proof.classic;

import com.dan323.classical.proof.AvailableAction;
import com.dan323.classical.proof.IntuitionisticNaturalDeduction;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.expressions.classical.*;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CancellationException;

import static org.junit.jupiter.api.Assertions.*;

class IntuitionisticNaturalDeductionTest {

    private static final VariableClassic P = new VariableClassic("p");
    private static final VariableClassic Q = new VariableClassic("q");
    private static final VariableClassic R = new VariableClassic("r");

    private static IntuitionisticNaturalDeduction solve(List<ClassicalLogicOperation> premises, ClassicalLogicOperation goal) {
        var proof = new IntuitionisticNaturalDeduction();
        proof.initializeProof(premises, goal);
        assertTimeoutPreemptively(Duration.ofSeconds(2), proof::automate);
        return proof;
    }

    private static void assertSolvedIntuitionistically(NaturalDeduction proof) {
        assertTrue(proof.isDone(), proof::toString);
        assertEquals(proof.getGoal(), proof.getSteps().getLast().getStep());
        var actions = proof.parse();
        assertEquals(proof.getSteps().size(), actions.size());
        actions.forEach(action -> assertTrue(action.getAction().isIntuitionistic(), proof::toString));
        // Replaying the parsed actions from the premises gives the same proof
        var replayed = new IntuitionisticNaturalDeduction();
        replayed.initializeProof(proof.getAssms(), proof.getGoal());
        actions.stream().skip(proof.getAssms().size()).forEach(action -> {
            assertTrue(action.isValid(replayed), proof::toString);
            action.apply(replayed);
        });
        assertEquals(proof.toString(), replayed.toString());
    }

    @Test
    void implicationIntroduction() {
        var proof = solve(List.of(), new ImplicationClassic(P, P));
        assertSolvedIntuitionistically(proof);
        assertEquals(2, proof.getSteps().size());
    }

    @Test
    void aPremiseIsTheGoal() {
        var proof = solve(List.of(P, Q), P);
        assertSolvedIntuitionistically(proof);
        assertEquals("Rep [1]", proof.getSteps().getLast().getProof().toString());
        assertEquals(1, solve(List.of(P), P).getSteps().size());
    }

    @Test
    void negationsUseNotIntroductionAndFalseIntroduction() {
        // p -> - (- p)
        var proof = solve(List.of(), new ImplicationClassic(P, new NegationClassic(new NegationClassic(P))));
        assertSolvedIntuitionistically(proof);
        var rules = proof.parse().stream().map(action -> action.getAction()).toList();
        assertTrue(rules.contains(AvailableAction.NOTI));
        assertTrue(rules.contains(AvailableAction.FI));
    }

    @Test
    void theEliminationRulesAreIntuitionistic() {
        // p | q, - p ⊢ q: OrE1 is built from Ass, FI, FE, ->I and |E
        var proof = solve(List.of(new DisjunctionClassic(P, Q), new NegationClassic(P)), Q);
        assertSolvedIntuitionistically(proof);
        assertTrue(proof.parse().stream().anyMatch(action -> action.getAction() == AvailableAction.ORE));
        // - (p | q) ⊢ (- p) & (- q): DeMorgan is built from Ass, |I, FI and -I
        assertSolvedIntuitionistically(solve(List.of(new NegationClassic(new DisjunctionClassic(P, Q))),
                new ConjunctionClassic(new NegationClassic(P), new NegationClassic(Q))));
        // - (- (p | - p)) needs DeMorgan inside the -I subproof
        assertSolvedIntuitionistically(solve(List.of(),
                new NegationClassic(new NegationClassic(new DisjunctionClassic(P, new NegationClassic(P))))));
    }

    @Test
    void aDisjunctionGoalTriesItsLeftSideFirst() {
        // (p -> p) | q
        var proof = solve(List.of(), new DisjunctionClassic(new ImplicationClassic(P, P), Q));
        assertSolvedIntuitionistically(proof);
        assertEquals(List.of(AvailableAction.ASSUME, AvailableAction.DT, AvailableAction.ORI1), rules(proof));
    }

    @Test
    void aFailedAttemptLeavesNoSteps() {
        // (p -> q) | (p -> p): the attempt at p -> q assumes p and gets stuck on q, so its assumption is removed
        var proof = solve(List.of(), new DisjunctionClassic(new ImplicationClassic(P, Q), new ImplicationClassic(P, P)));
        assertSolvedIntuitionistically(proof);
        assertEquals(List.of(AvailableAction.ASSUME, AvailableAction.DT, AvailableAction.ORI2), rules(proof));
        // Nested: r | ((p -> q) | (p -> p))
        var nested = solve(List.of(), new DisjunctionClassic(R,
                new DisjunctionClassic(new ImplicationClassic(P, Q), new ImplicationClassic(P, P))));
        assertSolvedIntuitionistically(nested);
        assertEquals(List.of(AvailableAction.ASSUME, AvailableAction.DT, AvailableAction.ORI2, AvailableAction.ORI2), rules(nested));
    }

    @Test
    void goalsWithoutAnIntroductionRuleAreNotReachedByContradiction() {
        // Classical logic reaches these by contradiction; without it the solver gives up
        var exFalso = solve(List.of(P, new NegationClassic(P)), Q);
        assertFalse(exFalso.isDone());
        assertEquals(2, exFalso.getSteps().size());
        var cases = solve(List.of(new DisjunctionClassic(P, Q), new ImplicationClassic(P, R), new ImplicationClassic(Q, R)), R);
        assertFalse(cases.isDone());
        assertEquals(3, cases.getSteps().size());
        assertFalse(solve(List.of(new ImplicationClassic(new ImplicationClassic(P, Q), R), Q), R).isDone());
    }

    @Test
    void theSearchIsBounded() {
        // OrE1 leaves q -> q among the steps, and ->E with it and the newest q keeps adding copies of q, a new action
        // each time: the size bound stops it
        var proof = solve(List.of(new DisjunctionClassic(P, Q), new NegationClassic(P)), R);
        assertFalse(proof.isDone());
        assertEquals(2, proof.getSteps().size());
    }

    @Test
    void falseOnTheRightOfAnImplicationGoalIsReachedWithImplicationIntroduction() {
        // -I on FALSE would give - p, not p -> FALSE, and the goal was lost (#191)
        var proof = solve(List.of(), new ImplicationClassic(new NegationClassic(P), new ImplicationClassic(P, ConstantClassic.FALSE)));
        assertTrue(proof.isDone());
        assertEquals(5, proof.getSteps().size());
    }

    private static List<AvailableAction> rules(NaturalDeduction proof) {
        return proof.parse().stream().map(action -> action.getAction()).toList();
    }

    @Test
    void classicalOnlyGoalsAreLeftWithThePremises() {
        var excludedMiddle = solve(List.of(), new DisjunctionClassic(P, new NegationClassic(P)));
        assertFalse(excludedMiddle.isDone());
        assertTrue(excludedMiddle.getSteps().isEmpty());
        var doubleNegation = solve(List.of(new NegationClassic(new NegationClassic(P))), P);
        assertFalse(doubleNegation.isDone());
        assertEquals(1, doubleNegation.getSteps().size());
        var peirce = solve(List.of(), new ImplicationClassic(new ImplicationClassic(new ImplicationClassic(P, Q), P), P));
        assertFalse(peirce.isDone());
        assertFalse(solve(List.of(), ConstantClassic.TRUE).isDone());
        // The classical solver does prove them
        var classical = new NaturalDeduction();
        classical.initializeProof(List.of(new NegationClassic(new NegationClassic(P))), P);
        classical.automate();
        assertTrue(classical.isDone());
    }

    @Test
    void theSolverStartsAgainFromThePremises() {
        var proof = new IntuitionisticNaturalDeduction();
        proof.initializeProof(List.of(P), new DisjunctionClassic(Q, P));
        proof.getSteps().add(new ProofStep<>(1, Q,
                new ProofReason("Ass", List.of(), List.of())));
        proof.automate();
        assertSolvedIntuitionistically(proof);
        assertEquals(2, proof.getSteps().size());
    }

    @Test
    void automateStopsWhenTheThreadIsInterrupted() {
        var proof = new IntuitionisticNaturalDeduction();
        proof.initializeProof(List.of(), new ImplicationClassic(P, P));
        Thread.currentThread().interrupt();
        try {
            assertThrows(CancellationException.class, proof::automate);
        } finally {
            // clear the flag so it does not leak into other tests
            assertTrue(Thread.interrupted());
        }
    }
}
