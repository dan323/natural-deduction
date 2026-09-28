package com.dan323.proof.firstorder;

import org.junit.jupiter.api.Test;

import static com.dan323.proof.firstorder.FirstOrderTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class QuantifierRulesTest {

    // ∀E

    @Test
    void forallEliminationSubstitutesTheTerm() {
        var proof = proof("forall x. P(x) & Q(x, e)");

        applies(proof, new FirstOrderForallE(1, term("f(a, b)")), "P(f(a, b)) & Q(f(a, b), e)");
        assertEquals("∀E [1]", proof.getSteps().getLast().getProof().toString());
        assertEquals(0, proof.getSteps().getLast().getAssumptionLevel());
    }

    @Test
    void forallEliminationAvoidsCapture() {
        var proof = proof("forall x. exists y. R(x, y)");

        applies(proof, new FirstOrderForallE(1, term("y")), "exists z. R(y, z)");
        assertNotEquals(formula("exists y. R(y, y)"), proof.getSteps().getLast().getStep());
    }

    @Test
    void forallEliminationNeedsAForall() {
        var proof = proof("P(a)", "exists x. P(x)");

        assertFalse(new FirstOrderForallE(1, term("a")).isValid(proof), "not a quantifier");
        assertFalse(new FirstOrderForallE(2, term("a")).isValid(proof), "an existential");
        assertFalse(new FirstOrderForallE(3, term("a")).isValid(proof), "no such line");
        assertFalse(new FirstOrderForallE(0, term("a")).isValid(proof), "no such line");
    }

    @Test
    void forallEliminationNeedsATerm() {
        var proof = proof("forall x. P(x)");

        assertFalse(new FirstOrderForallE(1, null).isValid(proof));
    }

    @Test
    void forallEliminationNeedsAnAvailableLine() {
        var proof = proof();
        assume(proof, "forall x. P(x)");
        apply(proof, new FirstOrderDeductionTheorem());

        assertFalse(new FirstOrderForallE(1, term("a")).isValid(proof), "line 1 was discharged");
    }

    // ∀I

    @Test
    void forallIntroductionGeneralizesAnArbitraryName() {
        var proof = proof("forall x. P(x) & Q(x)");
        apply(proof, new FirstOrderForallE(1, term("a")));
        apply(proof, new FirstOrderAndE1(2));

        var action = new FirstOrderForallI(3, forall("forall y. P(y)"));
        assertTrue(action.isValid(proof));
        action.apply(proof);
        assertEquals(formula("forall x. P(x)"), proof.getSteps().getLast().getStep());
        assertEquals("∀I [3]", proof.getSteps().getLast().getProof().toString());
    }

    @Test
    void forallIntroductionRejectsANameFreeInAPremise() {
        var proof = proof("P(a)");

        assertFalse(new FirstOrderForallI(1, forall("forall x. P(x)")).isValid(proof));
    }

    @Test
    void forallIntroductionRejectsANameFreeInAnOpenAssumption() {
        var proof = proof();
        assume(proof, "P(a)");

        assertFalse(new FirstOrderForallI(1, forall("forall x. P(x)")).isValid(proof));

        apply(proof, new FirstOrderDeductionTheorem());
        assertTrue(new FirstOrderForallI(2, forall("forall x. P(x) -> P(x)")).isValid(proof),
                "once discharged, the assumption no longer counts");
    }

    @Test
    void forallIntroductionRejectsANameFreeInAnOuterAssumption() {
        var proof = proof("forall x. P(x)");
        assume(proof, "Q(a)");
        apply(proof, new FirstOrderForallE(1, term("a")));

        assertFalse(new FirstOrderForallI(3, forall("forall x. P(x)")).isValid(proof));
        assertTrue(new FirstOrderForallI(3, forall("forall x. P(a)")).isValid(proof),
                "a vacuous quantifier needs no arbitrary name");
    }

    @Test
    void forallIntroductionRejectsANameInTheTarget() {
        var proof = proof("forall x. R(x, x)");
        apply(proof, new FirstOrderForallE(1, term("a")));

        assertFalse(new FirstOrderForallI(2, forall("forall x. R(x, a)")).isValid(proof));
        assertTrue(new FirstOrderForallI(2, forall("forall x. R(x, x)")).isValid(proof));
    }

    @Test
    void forallIntroductionRejectsATermThatIsNotAName() {
        var proof = proof("forall x. P(f(x))");
        apply(proof, new FirstOrderForallE(1, term("a")));

        assertFalse(new FirstOrderForallI(2, forall("forall x. P(x)")).isValid(proof));
        assertTrue(new FirstOrderForallI(2, forall("forall x. P(f(x))")).isValid(proof));
    }

    @Test
    void forallIntroductionRejectsANonInstance() {
        var proof = proof("forall x. P(x)");
        apply(proof, new FirstOrderForallE(1, term("a")));

        assertFalse(new FirstOrderForallI(2, forall("forall x. Q(x)")).isValid(proof));
        assertFalse(new FirstOrderForallI(2, forall("forall x. P(x) & P(x)")).isValid(proof));
    }

    @Test
    void forallIntroductionRejectsCapture() {
        var proof = proof("forall y. R(y, y)");

        assertFalse(new FirstOrderForallI(1, forall("forall x. forall y. R(x, y)")).isValid(proof),
                "x would have to become y, which the inner quantifier captures");
    }

    @Test
    void forallIntroductionOfAVacuousQuantifier() {
        var proof = proof("Q(a)");

        assertTrue(new FirstOrderForallI(1, forall("forall x. Q(a)")).isValid(proof));
        assertFalse(new FirstOrderForallI(1, forall("forall x. Q(b)")).isValid(proof));
    }

    @Test
    void forallIntroductionNeedsATargetAndALine() {
        var proof = proof("forall x. P(x)");
        apply(proof, new FirstOrderForallE(1, term("a")));

        assertFalse(new FirstOrderForallI(2, null).isValid(proof));
        assertFalse(new FirstOrderForallI(3, forall("forall x. P(x)")).isValid(proof));
        assertFalse(new FirstOrderForallI(0, forall("forall x. P(x)")).isValid(proof));
    }

    // ∃I

    @Test
    void existsIntroductionAbstractsATerm() {
        var proof = proof("P(f(a))");

        assertTrue(new FirstOrderExistsI(1, exists("exists x. P(f(x))")).isValid(proof));
        assertTrue(new FirstOrderExistsI(1, exists("exists x. P(x)")).isValid(proof));
        assertFalse(new FirstOrderExistsI(1, exists("exists x. Q(x)")).isValid(proof));
        assertFalse(new FirstOrderExistsI(1, exists("exists x. P(g(x))")).isValid(proof));

        applies(proof, new FirstOrderExistsI(1, exists("exists y. P(y)")), "exists x. P(x)");
        assertEquals("∃I [1]", proof.getSteps().getLast().getProof().toString());
    }

    @Test
    void existsIntroductionMayAbstractSomeOccurrences() {
        var proof = proof("R(a, a)");

        assertTrue(new FirstOrderExistsI(1, exists("exists x. R(x, a)")).isValid(proof));
        assertTrue(new FirstOrderExistsI(1, exists("exists x. R(x, x)")).isValid(proof));
        assertFalse(new FirstOrderExistsI(1, exists("exists x. R(x, b)")).isValid(proof));
    }

    @Test
    void existsIntroductionNeedsTheSameTermEverywhere() {
        var proof = proof("R(a, b)");

        assertFalse(new FirstOrderExistsI(1, exists("exists x. R(x, x)")).isValid(proof));
    }

    @Test
    void existsIntroductionRejectsCapture() {
        var captured = proof("forall y. R(y, y)");
        assertFalse(new FirstOrderExistsI(1, exists("exists x. forall y. R(x, y)")).isValid(captured));

        var free = proof("forall y. R(a, y)");
        assertTrue(new FirstOrderExistsI(1, exists("exists x. forall y. R(x, y)")).isValid(free));
    }

    @Test
    void existsIntroductionOfAVacuousQuantifier() {
        var proof = proof("Q");

        assertTrue(new FirstOrderExistsI(1, exists("exists x. Q")).isValid(proof));
        assertFalse(new FirstOrderExistsI(1, exists("exists x. R")).isValid(proof));
    }

    @Test
    void existsIntroductionNeedsATargetAndALine() {
        var proof = proof("P(a)");

        assertFalse(new FirstOrderExistsI(1, null).isValid(proof));
        assertFalse(new FirstOrderExistsI(2, exists("exists x. P(x)")).isValid(proof));
    }

    // ∃E

    /**
     * {@code exists x. P(x), forall x. (P(x) -> Q)}, and the subproof {@code P(a)}, {@code P(a) -> Q}, {@code Q}.
     */
    private static com.dan323.proof.firstorder.proof.FirstOrderNaturalDeduction witnessProof(String witness) {
        var proof = proof("exists x. P(x)", "forall x. (P(x) -> Q)");
        assume(proof, "P(" + witness + ")");
        apply(proof, new FirstOrderForallE(2, term(witness)));
        apply(proof, new FirstOrderModusPonens(4, 3));
        return proof;
    }

    @Test
    void existsEliminationDischargesTheSubproof() {
        var proof = witnessProof("a");

        var action = new FirstOrderExistsE(1);
        assertTrue(action.isValid(proof));
        action.apply(proof);

        var last = proof.getSteps().getLast();
        assertEquals(formula("Q"), last.getStep());
        assertEquals(0, last.getAssumptionLevel());
        assertEquals("∃E [1, 3-5]", last.getProof().toString());
        assertTrue(proof.getSteps().subList(2, 5).stream().noneMatch(com.dan323.proof.generic.proof.ProofStep::isValid));
    }

    @Test
    void existsEliminationRejectsANameThatEscapesIntoTheConclusion() {
        var proof = proof("exists x. P(x)");
        assume(proof, "P(a)");

        assertFalse(new FirstOrderExistsE(1).isValid(proof));

        apply(proof, new FirstOrderExistsI(2, exists("exists y. P(y)")));
        assertTrue(new FirstOrderExistsE(1).isValid(proof));
    }

    @Test
    void existsEliminationRejectsANameFreeInAPremise() {
        var proof = proof("exists x. P(x)", "R(a)");
        assume(proof, "P(a)");
        apply(proof, new FirstOrderEqualsI(term("b")));

        assertFalse(new FirstOrderExistsE(1).isValid(proof));
    }

    @Test
    void existsEliminationRejectsANameInTheExistential() {
        var proof = proof("exists x. P(x, a)");
        assume(proof, "P(a, a)");
        apply(proof, new FirstOrderEqualsI(term("b")));

        assertFalse(new FirstOrderExistsE(1).isValid(proof));
    }

    @Test
    void existsEliminationRejectsANameFreeInAnAssumptionOpenOutside() {
        var proof = proof("exists x. P(x)");
        assume(proof, "R(a)");
        assume(proof, "P(a)");
        apply(proof, new FirstOrderEqualsI(term("b")));

        assertFalse(new FirstOrderExistsE(1).isValid(proof));
    }

    @Test
    void existsEliminationInsideAnotherSubproof() {
        var proof = proof("exists x. P(x)");
        assume(proof, "R(c)");
        assume(proof, "P(a)");
        apply(proof, new FirstOrderEqualsI(term("b")));

        apply(proof, new FirstOrderExistsE(1));
        assertEquals(1, proof.getSteps().getLast().getAssumptionLevel());
        assertEquals("∃E [1, 3-4]", proof.getSteps().getLast().getProof().toString());
    }

    @Test
    void existsEliminationIgnoresDischargedAssumptions() {
        var proof = proof("exists x. P(x)");
        assume(proof, "R(a)");
        apply(proof, new FirstOrderDeductionTheorem());
        assume(proof, "P(a)");
        apply(proof, new FirstOrderEqualsI(term("b")));

        assertTrue(new FirstOrderExistsE(1).isValid(proof));
    }

    @Test
    void existsEliminationNeedsAnInstanceWithAName() {
        var other = proof("exists x. P(x)");
        assume(other, "Q(a)");
        apply(other, new FirstOrderEqualsI(term("b")));
        assertFalse(new FirstOrderExistsE(1).isValid(other), "not an instance");

        var function = proof("exists x. P(x)");
        assume(function, "P(f(c))");
        apply(function, new FirstOrderEqualsI(term("b")));
        assertFalse(new FirstOrderExistsE(1).isValid(function), "the witness must be a name");
    }

    @Test
    void existsEliminationRejectsCapture() {
        var captured = proof("exists x. forall y. R(x, y)");
        assume(captured, "forall y. R(y, y)");
        apply(captured, new FirstOrderEqualsI(term("b")));
        assertFalse(new FirstOrderExistsE(1).isValid(captured));

        var renamed = proof("exists x. forall y. R(x, y)");
        assume(renamed, "forall z. R(a, z)");
        apply(renamed, new FirstOrderEqualsI(term("b")));
        assertTrue(new FirstOrderExistsE(1).isValid(renamed));
    }

    @Test
    void existsEliminationNeedsAnExistentialOutsideTheSubproof() {
        var noSubproof = proof("exists x. P(x)");
        assertFalse(new FirstOrderExistsE(1).isValid(noSubproof), "no open subproof");

        var inside = proof();
        assume(inside, "exists x. P(x)");
        assume(inside, "P(a)");
        apply(inside, new FirstOrderEqualsI(term("b")));
        assertTrue(new FirstOrderExistsE(1).isValid(inside));

        var own = proof();
        assume(own, "exists x. P(x)");
        apply(own, new FirstOrderEqualsI(term("b")));
        assertFalse(new FirstOrderExistsE(1).isValid(own), "the existential opens the subproof itself");

        var notExists = proof("P(b)");
        assume(notExists, "P(a)");
        assertFalse(new FirstOrderExistsE(1).isValid(notExists), "not an existential");
        assertFalse(new FirstOrderExistsE(0).isValid(notExists), "no such line");
    }

    @Test
    void existsEliminationOfAVacuousQuantifier() {
        var proof = proof("exists x. Q(a)");
        assume(proof, "Q(a)");

        applies(proof, new FirstOrderExistsE(1), "Q(a)");
        assertEquals(0, proof.getSteps().getLast().getAssumptionLevel());
    }

    @Test
    void existsEliminationNeedsTheSubproofToOpenWithAnAssumption() {
        var proof = proof("exists x. P(x)");
        assume(proof, "P(a)");
        apply(proof, new FirstOrderEqualsI(term("b")));
        // A step of the subproof that is not the assumption, first in its block: not something the rules produce.
        proof.getSteps().set(1, new com.dan323.proof.generic.proof.ProofStep<>(1, formula("P(a)"),
                new com.dan323.proof.generic.proof.ProofReason("Rep", java.util.List.of(), java.util.List.of(1))));

        assertFalse(new FirstOrderExistsE(1).isValid(proof));
    }
}
