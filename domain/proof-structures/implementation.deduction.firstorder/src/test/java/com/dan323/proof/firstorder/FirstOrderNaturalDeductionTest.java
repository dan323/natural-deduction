package com.dan323.proof.firstorder;

import com.dan323.expressions.firstorder.FirstOrderOperation;
import com.dan323.proof.firstorder.proof.FirstOrderNaturalDeduction;
import com.dan323.proof.firstorder.proof.ParseFirstOrderAction;
import com.dan323.proof.generic.proof.ProofStep;
import org.junit.jupiter.api.Test;

import static com.dan323.proof.firstorder.FirstOrderTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class FirstOrderNaturalDeductionTest {

    @Test
    void forallGivesExists() {
        var proof = proofWithGoal("exists x. P(x)", "forall x. P(x)");
        assertFalse(proof.isDone());

        apply(proof, new FirstOrderForallE(1, term("a")));
        apply(proof, new FirstOrderExistsI(2, exists("exists y. P(y)")));

        assertTrue(proof.isDone(), "the goal is equal up to renaming of the bound variable");
        assertTrue(replay(proof).isDone());
    }

    @Test
    void leibnizGivesTheOtherSide() {
        var proof = proofWithGoal("P(b)", "a = b", "P(a)");

        apply(proof, new FirstOrderEqualsE(1, 2, formula("P(b)")));

        assertTrue(proof.isDone());
        assertTrue(replay(proof).isDone());
    }

    @Test
    void quantifierSwap() {
        // exists x. forall y. R(x, y) |- forall y. exists x. R(x, y)
        var proof = proofWithGoal("forall y. exists x. R(x, y)", "exists x. forall y. R(x, y)");
        assume(proof, "forall y. R(a, y)");
        apply(proof, new FirstOrderForallE(2, term("b")));
        apply(proof, new FirstOrderExistsI(3, exists("exists x. R(x, b)")));
        apply(proof, new FirstOrderExistsE(1));
        apply(proof, new FirstOrderForallI(5, forall("forall y. exists x. R(x, y)")));

        assertTrue(proof.isDone());
        assertTrue(replay(proof).isDone());
    }

    @Test
    void forbiddenSwapIsRejected() {
        // forall y. exists x. R(x, y) does not give exists x. forall y. R(x, y)
        var proof = proof("forall y. exists x. R(x, y)");
        apply(proof, new FirstOrderForallE(1, term("b")));
        assume(proof, "R(a, b)");

        assertFalse(new FirstOrderForallI(3, forall("forall y. R(a, y)")).isValid(proof), "b is free in an open assumption");
    }

    @Test
    void propositionalRulesReplay() {
        var proof = proof("P(a) & Q", "P(a) -> R", "--S", "A | B", "A -> C", "B -> C");
        applies(proof, new FirstOrderAndE1(1), "P(a)");
        applies(proof, new FirstOrderAndE2(1), "Q");
        applies(proof, new FirstOrderAndI(7, 8), "P(a) & Q");
        applies(proof, new FirstOrderModusPonens(2, 7), "R");
        applies(proof, new FirstOrderCopy(10), "R");
        applies(proof, new FirstOrderNotE(3), "S");
        applies(proof, new FirstOrderOrI1(12, formula("T")), "S | T");
        applies(proof, new FirstOrderOrI2(12, formula("T")), "T | S");
        applies(proof, new FirstOrderOrE(4, 5, 6), "C");
        assume(proof, "-R");
        applies(proof, new FirstOrderFI(10, 16), "FALSE");
        applies(proof, new FirstOrderNotI(), "--R");
        assume(proof, "FALSE");
        applies(proof, new FirstOrderFE(19, formula("forall x. Z(x)")), "forall x. Z(x)");
        applies(proof, new FirstOrderDeductionTheorem(), "FALSE -> forall x. Z(x)");

        var copy = replay(proof);
        assertEquals(proof.getSteps().getLast().getStep(), copy.getSteps().getLast().getStep());
    }

    @Test
    void propositionalRulesKeepTheirConditions() {
        var proof = proof("P(a)", "-Q");

        assertFalse(new FirstOrderAndE1(1).isValid(proof));
        assertFalse(new FirstOrderModusPonens(1, 2).isValid(proof));
        assertFalse(new FirstOrderNotE(2).isValid(proof));
        assertFalse(new FirstOrderFI(1, 2).isValid(proof));
        assertFalse(new FirstOrderFE(1, formula("Q")).isValid(proof));
        assertFalse(new FirstOrderNotI().isValid(proof));
        assertFalse(new FirstOrderDeductionTheorem().isValid(proof));
        assertFalse(new FirstOrderOrE(1, 1, 1).isValid(proof));
        assertFalse(new FirstOrderAssume(null).isValid(proof));
        assertTrue(new FirstOrderAssume(formula("Q")).isValid(proof));
    }

    @Test
    void fullProofReplaysThroughParse() {
        var proof = proofWithGoal("forall x. (P(x) -> exists y. y = x)", "exists x. Q(x)", "forall x. (Q(x) -> R)");
        assume(proof, "P(a)");
        apply(proof, new FirstOrderEqualsI(term("a")));
        apply(proof, new FirstOrderExistsI(4, exists("exists y. y = a")));
        apply(proof, new FirstOrderDeductionTheorem());
        apply(proof, new FirstOrderForallI(6, forall("forall x. (P(x) -> exists y. y = x)")));
        assume(proof, "Q(c)");
        apply(proof, new FirstOrderForallE(2, term("c")));
        apply(proof, new FirstOrderModusPonens(9, 8));
        apply(proof, new FirstOrderExistsE(1));
        apply(proof, new FirstOrderEqualsI(term("f(c)")));
        apply(proof, new FirstOrderEqualsE(12, 12, formula("f(c) = f(c)")));

        assertTrue(proof.isDone());
        var copy = replay(proof);
        assertTrue(copy.isDone());
    }

    @Test
    void printsTheUsualTextLayout() {
        var proof = proof("exists x. P(x)");
        assume(proof, "P(a)");
        apply(proof, new FirstOrderExistsI(2, exists("exists y. P(y)")));
        apply(proof, new FirstOrderExistsE(1));
        apply(proof, new FirstOrderEqualsI(term("a")));

        assertEquals("""
                exists x. P(x)           Ass
                   P(a)           Ass
                   exists y. P(y)           ∃I [2]
                exists y. P(y)           ∃E [1, 2-3]
                a = a           =I
                """, proof.toString());
    }

    @Test
    void theRulesOfEveryStepReadBack() {
        var proof = proof("exists x. P(x)", "forall x. P(x) -> x = x");
        assume(proof, "P(a)");
        apply(proof, new FirstOrderForallE(2, term("a")));
        apply(proof, new FirstOrderModusPonens(4, 3));
        apply(proof, new FirstOrderEqualsE(5, 3, formula("P(a)")));
        apply(proof, new FirstOrderExistsI(6, exists("exists y. P(y)")));
        apply(proof, new FirstOrderExistsE(1));
        apply(proof, new FirstOrderForallI(8, forall("forall z. exists y. P(y)")));
        apply(proof, new FirstOrderEqualsI(term("b")));

        for (ProofStep<FirstOrderOperation> step : proof.getSteps()) {
            String rule = step.getProof().toString();
            assertEquals(step.getProof(), ParseFirstOrderAction.parseReason(rule), rule);
            assertEquals(step.getProof(), ParseFirstOrderAction.parseReason(" " + rule + " "), rule);
        }
    }

    @Test
    void automateIsUnsupported() {
        var proof = proof("P(a)");

        assertThrows(UnsupportedOperationException.class, proof::automate);
    }

    @Test
    void isFreeInOpenAssumptionOnlyLooksBefore() {
        var proof = proof("P(a)");
        assume(proof, "Q(b)");

        assertTrue(proof.isFreeInOpenAssumption("a", 1));
        assertFalse(proof.isFreeInOpenAssumption("b", 1));
        assertTrue(proof.isFreeInOpenAssumption("b", 2));
        assertFalse(proof.isFreeInOpenAssumption("c", 2));
    }

    @Test
    void reinitializing() {
        var proof = new FirstOrderNaturalDeduction();
        proof.initializeProof(java.util.List.of(formula("P(a)")), formula("P(a)"));
        apply(proof, new FirstOrderCopy(1));
        proof.reset();

        assertEquals(1, proof.getSteps().size());
        assertTrue(proof.isDone());
    }
}
