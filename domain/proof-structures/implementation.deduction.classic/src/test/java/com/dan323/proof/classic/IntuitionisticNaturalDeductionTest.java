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
    void casesOnADisjunction() {
        // p | q, p -> r, q -> r ⊢ r
        var proof = solve(List.of(new DisjunctionClassic(P, Q), new ImplicationClassic(P, R), new ImplicationClassic(Q, R)), R);
        assertSolvedIntuitionistically(proof);
        assertTrue(proof.parse().stream().anyMatch(action -> action.getAction() == AvailableAction.ORE));
    }

    @Test
    void exFalso() {
        var proof = solve(List.of(P, new NegationClassic(P)), new ConjunctionClassic(Q, R));
        assertSolvedIntuitionistically(proof);
        assertTrue(proof.parse().stream().anyMatch(action -> action.getAction() == AvailableAction.FE));
    }

    @Test
    void nestedImplicationsOnTheLeft() {
        // The G4ip rule for (C -> D) -> B: (p -> q) -> r, q ⊢ r
        var proof = solve(List.of(new ImplicationClassic(new ImplicationClassic(P, Q), R), Q), R);
        assertSolvedIntuitionistically(proof);
        // - (- (p | - p)) needs the (C -> D) -> B rule on a negation
        assertSolvedIntuitionistically(solve(List.of(),
                new NegationClassic(new NegationClassic(new DisjunctionClassic(P, new NegationClassic(P))))));
        // (p | q) -> r, (p & q) -> r are unfolded
        assertSolvedIntuitionistically(solve(List.of(new ImplicationClassic(new DisjunctionClassic(P, Q), R),
                new ImplicationClassic(new ConjunctionClassic(P, Q), R), Q), R));
        assertSolvedIntuitionistically(solve(List.of(new ImplicationClassic(new ConjunctionClassic(P, Q), R)),
                new ImplicationClassic(P, new ImplicationClassic(Q, R))));
        assertSolvedIntuitionistically(solve(List.of(new NegationClassic(new ConjunctionClassic(P, Q))),
                new ImplicationClassic(P, new NegationClassic(Q))));
        assertSolvedIntuitionistically(solve(List.of(new NegationClassic(new DisjunctionClassic(P, Q))),
                new ConjunctionClassic(new NegationClassic(P), new NegationClassic(Q))));
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
