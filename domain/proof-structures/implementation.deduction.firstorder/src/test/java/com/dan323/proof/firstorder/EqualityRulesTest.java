package com.dan323.proof.firstorder;

import org.junit.jupiter.api.Test;

import static com.dan323.proof.firstorder.FirstOrderTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class EqualityRulesTest {

    // =I

    @Test
    void equalityIntroductionGivesReflexivity() {
        var proof = proof();

        applies(proof, new FirstOrderEqualsI(term("m(x, i(x))")), "m(x, i(x)) = m(x, i(x))");
        assertEquals("=I", proof.getSteps().getLast().getProof().toString());
        assertEquals(0, proof.getSteps().getLast().getAssumptionLevel());
    }

    @Test
    void equalityIntroductionInsideASubproof() {
        var proof = proof();
        assume(proof, "P(a)");

        applies(proof, new FirstOrderEqualsI(term("a")), "a = a");
        assertEquals(1, proof.getSteps().getLast().getAssumptionLevel());
    }

    @Test
    void equalityIntroductionNeedsATerm() {
        assertFalse(new FirstOrderEqualsI(null).isValid(proof()));
    }

    // =E

    @Test
    void equalityEliminationReplacesOccurrences() {
        var proof = proofWithGoal("P(b)", "a = b", "P(a)");

        applies(proof, new FirstOrderEqualsE(1, 2, formula("P(b)")), "P(b)");
        assertEquals("=E [1, 2]", proof.getSteps().getLast().getProof().toString());
        assertTrue(proof.isDone());
    }

    @Test
    void equalityEliminationMayReplaceSomeOccurrences() {
        var proof = proof("a = b", "R(a, a)");

        assertTrue(new FirstOrderEqualsE(1, 2, formula("R(b, a)")).isValid(proof));
        assertTrue(new FirstOrderEqualsE(1, 2, formula("R(a, b)")).isValid(proof));
        assertTrue(new FirstOrderEqualsE(1, 2, formula("R(b, b)")).isValid(proof));
        assertTrue(new FirstOrderEqualsE(1, 2, formula("R(a, a)")).isValid(proof), "no occurrence replaced");
    }

    @Test
    void equalityEliminationRejectsANonSubstitutionInstance() {
        var proof = proof("a = b", "R(a, c)");

        assertFalse(new FirstOrderEqualsE(1, 2, formula("R(b, b)")).isValid(proof));
        assertFalse(new FirstOrderEqualsE(1, 2, formula("Q(b, c)")).isValid(proof));
        assertFalse(new FirstOrderEqualsE(1, 2, formula("R(b, c) & R(b, c)")).isValid(proof));
        assertFalse(new FirstOrderEqualsE(1, 2, formula("R(b)")).isValid(proof));
    }

    @Test
    void equalityEliminationGoesLeftToRight() {
        var proof = proof("a = b", "P(b)");

        assertFalse(new FirstOrderEqualsE(1, 2, formula("P(a)")).isValid(proof));
    }

    @Test
    void equalityEliminationInsideTerms() {
        var proof = proof("f(a) = c", "P(g(f(a)), f(b))");

        assertTrue(new FirstOrderEqualsE(1, 2, formula("P(g(c), f(b))")).isValid(proof));
        assertFalse(new FirstOrderEqualsE(1, 2, formula("P(g(c), c)")).isValid(proof));
        assertFalse(new FirstOrderEqualsE(1, 2, formula("P(h(c), f(b))")).isValid(proof));
    }

    @Test
    void equalityEliminationThroughConnectivesAndEquations() {
        var proof = proof("a = b", "-(P(a) | a = c) -> (Q & FALSE)");

        assertTrue(new FirstOrderEqualsE(1, 2, formula("-(P(b) | b = c) -> (Q & FALSE)")).isValid(proof));
        assertFalse(new FirstOrderEqualsE(1, 2, formula("-(P(b) & b = c) -> (Q & FALSE)")).isValid(proof));
        assertFalse(new FirstOrderEqualsE(1, 2, formula("-(P(b) | b = c) -> (Q & TRUE)")).isValid(proof));
    }

    @Test
    void equalityEliminationUnderAQuantifier() {
        var proof = proof("a = b", "forall x. R(a, x)");

        assertTrue(new FirstOrderEqualsE(1, 2, formula("forall y. R(b, y)")).isValid(proof));
        assertFalse(new FirstOrderEqualsE(1, 2, formula("exists y. R(b, y)")).isValid(proof));
    }

    @Test
    void equalityEliminationRejectsCapture() {
        var proof = proof("a = y", "forall y. R(a, y)");

        assertFalse(new FirstOrderEqualsE(1, 2, formula("forall y. R(y, y)")).isValid(proof),
                "the quantifier would capture y");
        assertTrue(new FirstOrderEqualsE(1, 2, formula("forall z. R(y, z)")).isValid(proof));
    }

    @Test
    void equalityEliminationDoesNotReplaceABoundOccurrence() {
        var proof = proof("x = b", "forall x. P(x)");

        assertFalse(new FirstOrderEqualsE(1, 2, formula("forall x. P(b)")).isValid(proof));
        assertTrue(new FirstOrderEqualsE(1, 2, formula("forall x. P(x)")).isValid(proof));
    }

    @Test
    void equalityEliminationNeedsAnEquationAndATarget() {
        var proof = proof("a = b", "P(a)");

        assertFalse(new FirstOrderEqualsE(2, 1, formula("P(b)")).isValid(proof), "line 2 is not an equation");
        assertFalse(new FirstOrderEqualsE(1, 2, null).isValid(proof));
        assertFalse(new FirstOrderEqualsE(1, 3, formula("P(b)")).isValid(proof));
        assertFalse(new FirstOrderEqualsE(0, 2, formula("P(b)")).isValid(proof));
    }

    @Test
    void symmetryOfEquality() {
        var proof = proofWithGoal("b = a", "a = b");
        apply(proof, new FirstOrderEqualsI(term("a")));
        applies(proof, new FirstOrderEqualsE(1, 2, formula("b = a")), "b = a");

        assertTrue(proof.isDone());
    }
}
