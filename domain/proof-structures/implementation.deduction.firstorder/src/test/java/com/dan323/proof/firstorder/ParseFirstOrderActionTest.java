package com.dan323.proof.firstorder;

import com.dan323.proof.firstorder.proof.ParseFirstOrderAction;
import com.dan323.proof.generic.proof.ProofReason;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.dan323.proof.firstorder.FirstOrderTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class ParseFirstOrderActionTest {

    @Test
    void parseActionReadsTheSharedRules() {
        var p = formula("P(a)");
        assertEquals(new FirstOrderAssume(p), ParseFirstOrderAction.parseAction("Ass", List.of(), p, null));
        assertEquals(new FirstOrderOrI1(1, p), ParseFirstOrderAction.parseAction("|I1", List.of(1), p, null));
        assertEquals(new FirstOrderOrI2(1, p), ParseFirstOrderAction.parseAction("|I2", List.of(1), p, null));
        assertEquals(new FirstOrderOrE(1, 2, 3), ParseFirstOrderAction.parseAction("|E", List.of(1, 2, 3), null, null));
        assertEquals(new FirstOrderAndI(1, 2), ParseFirstOrderAction.parseAction("&I", List.of(1, 2), null, null));
        assertEquals(new FirstOrderAndE1(1), ParseFirstOrderAction.parseAction("&E1", List.of(1), null, null));
        assertEquals(new FirstOrderAndE2(1), ParseFirstOrderAction.parseAction("&E2", List.of(1), null, null));
        assertEquals(new FirstOrderCopy(1), ParseFirstOrderAction.parseAction("Rep", List.of(1), null, null));
        assertEquals(new FirstOrderNotE(1), ParseFirstOrderAction.parseAction("-E", List.of(1), null, null));
        assertEquals(new FirstOrderNotI(), ParseFirstOrderAction.parseAction("-I", List.of(), null, null));
        assertEquals(new FirstOrderDeductionTheorem(), ParseFirstOrderAction.parseAction("->I", List.of(), null, null));
        assertEquals(new FirstOrderModusPonens(1, 2), ParseFirstOrderAction.parseAction("->E", List.of(1, 2), null, null));
        assertEquals(new FirstOrderFE(1, p), ParseFirstOrderAction.parseAction("FE", List.of(1), p, null));
        assertEquals(new FirstOrderFI(1, 2), ParseFirstOrderAction.parseAction("FI", List.of(1, 2), null, null));
    }

    @Test
    void parseActionTakesTheClassicalNames() {
        assertEquals(new FirstOrderCopy(1), ParseFirstOrderAction.parseAction("COPY", List.of(1), null, null));
        assertEquals(new FirstOrderModusPonens(1, 2), ParseFirstOrderAction.parseAction("MP", List.of(1, 2), null, null));
        assertEquals(new FirstOrderDeductionTheorem(), ParseFirstOrderAction.parseAction("DT", List.of(), null, null));
        assertEquals("Rep", ParseFirstOrderAction.ruleName("COPY"));
        assertEquals("∀E", ParseFirstOrderAction.ruleName("∀E"));
    }

    @Test
    void parseActionReadsTheFirstOrderRules() {
        var t = term("f(a)");
        assertEquals(new FirstOrderForallE(1, t), ParseFirstOrderAction.parseAction("∀E", List.of(1), null, t));
        assertEquals(new FirstOrderForallI(1, forall("forall x. P(x)")),
                ParseFirstOrderAction.parseAction("∀I", List.of(1), formula("forall y. P(y)"), null));
        assertEquals(new FirstOrderExistsI(1, exists("exists x. P(x)")),
                ParseFirstOrderAction.parseAction("∃I", List.of(1), formula("exists x. P(x)"), null));
        assertEquals(new FirstOrderExistsE(2), ParseFirstOrderAction.parseAction("∃E", List.of(2), null, null));
        assertEquals(new FirstOrderEqualsI(t), ParseFirstOrderAction.parseAction("=I", List.of(), null, t));
        assertEquals(new FirstOrderEqualsE(1, 2, formula("P(b)")),
                ParseFirstOrderAction.parseAction("=E", List.of(1, 2), formula("P(b)"), null));
    }

    @Test
    void aTargetOfTheWrongKindIsNotValid() {
        var proof = proof("P(a)");

        assertFalse(ParseFirstOrderAction.parseAction("∀I", List.of(1), formula("exists x. P(x)"), null).isValid(proof));
        assertFalse(ParseFirstOrderAction.parseAction("∃I", List.of(1), formula("forall x. P(x)"), null).isValid(proof));
        assertFalse(ParseFirstOrderAction.parseAction("∀E", List.of(1), null, null).isValid(proof));
    }

    @Test
    void parseActionRejectsUnknownRules() {
        List<Integer> none = List.of();
        assertThrows(IllegalArgumentException.class, () -> ParseFirstOrderAction.parseAction("[]I", none, null, null));
    }

    @Test
    void parseReasonReadsTheFirstOrderRules() {
        assertEquals(new ProofReason("∀E", List.of(), List.of(1)), ParseFirstOrderAction.parseReason("∀E [1]"));
        assertEquals(new ProofReason("∀I", List.of(), List.of(3)), ParseFirstOrderAction.parseReason("∀I [3]"));
        assertEquals(new ProofReason("∃I", List.of(), List.of(2)), ParseFirstOrderAction.parseReason("∃I [2]"));
        assertEquals(new ProofReason("∃E", List.of(new ProofReason.Range(2, 4)), List.of(1)),
                ParseFirstOrderAction.parseReason("∃E [1, 2-4]"));
        assertEquals(new ProofReason("=I", List.of(), List.of()), ParseFirstOrderAction.parseReason("=I"));
        assertEquals(new ProofReason("=E", List.of(), List.of(1, 2)), ParseFirstOrderAction.parseReason("=E [1, 2]"));
        assertEquals(new ProofReason("->I", List.of(new ProofReason.Range(1, 3)), List.of()),
                ParseFirstOrderAction.parseReason("->I [1-3]"));
    }

    @Test
    void parseReasonRejectsUnknownRules() {
        assertThrows(IllegalArgumentException.class, () -> ParseFirstOrderAction.parseReason("=X"));
        assertThrows(IllegalArgumentException.class, () -> ParseFirstOrderAction.parseReason("Nope [1]"));
        assertThrows(IllegalArgumentException.class, () -> ParseFirstOrderAction.parseReason("[]I [1-2]"));
    }

    @Test
    void parseWithReasonRejectsWrongSteps() {
        var proof = proof("forall x. P(x)", "P(a)");
        var forallE = new ProofReason("∀E", List.of(), List.of(1));
        var badForallE = formula("Q(a)");
        assertThrows(IllegalArgumentException.class, () -> ParseFirstOrderAction.parseWithReason(proof, badForallE, forallE));

        var forallI = new ProofReason("∀I", List.of(), List.of(2));
        var notForall = formula("P(a)");
        assertThrows(IllegalArgumentException.class, () -> ParseFirstOrderAction.parseWithReason(proof, notForall, forallI));

        var unknown = new ProofReason("[]I", List.of(), List.of());
        assertThrows(IllegalArgumentException.class, () -> ParseFirstOrderAction.parseWithReason(proof, notForall, unknown));
    }

    @Test
    void parseWithReasonOfAVacuousForallElimination() {
        var proof = proof("forall x. Q");
        var action = ParseFirstOrderAction.parseWithReason(proof, formula("Q"), new ProofReason("∀E", List.of(), List.of(1)));

        assertTrue(action.isValid(proof));
        action.apply(proof);
        assertEquals(formula("Q"), proof.getSteps().getLast().getStep());
    }

    @Test
    void parseExpressionAndTermRejectBadInput() {
        assertThrows(IllegalArgumentException.class, () -> ParseFirstOrderAction.parseExpression("P("));
        assertThrows(IllegalArgumentException.class, () -> ParseFirstOrderAction.parseTerm("P(a) &"));
    }
}
