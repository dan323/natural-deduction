package com.dan323.expressions;

import com.dan323.expressions.modal.Always;
import com.dan323.expressions.modal.ConjunctionModal;
import com.dan323.expressions.modal.ConstantModal;
import com.dan323.expressions.modal.DisjunctionModal;
import com.dan323.expressions.modal.ImplicationModal;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.NegationModal;
import com.dan323.expressions.modal.Next;
import com.dan323.expressions.modal.Sometime;
import com.dan323.expressions.modal.Until;
import com.dan323.expressions.modal.VariableModal;
import com.dan323.expressions.relation.Equals;
import com.dan323.expressions.relation.LessEqual;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModalNextUntilParserTest {

    private static final ModalLogicalOperation P = new VariableModal("p");
    private static final ModalLogicalOperation Q = new VariableModal("q");
    private static final ModalLogicalOperation R = new VariableModal("r");

    private final ModalNextUntilLogicParser parser = new ModalNextUntilLogicParser();

    @Test
    void nextAndUntilArePrintedAndReadTheSameWay() {
        assertEquals(new Next(P), parser.evaluate("X p"));
        assertEquals("X p", parser.evaluate("X p").toString());
        assertEquals(new Next(P), parser.evaluate("X(p)"));
        assertEquals(new Until(P, Q), parser.evaluate("p U q"));
        assertEquals("p U q", parser.evaluate("p U q").toString());
        assertEquals(new Until(P, Q), parser.evaluate("(p)U(q)"));
        assertEquals(new Next(new Until(P, Q)), parser.evaluate("X (p U q)"));
        assertEquals("X (p U q)", new Next(new Until(P, Q)).toString());
    }

    @Test
    void nextIsAUnaryConnective() {
        assertEquals(new Next(new Next(P)), parser.evaluate("X X p"));
        assertEquals(new NegationModal(new Next(P)), parser.evaluate("- X p"));
        assertEquals(new Next(new NegationModal(P)), parser.evaluate("X - p"));
        assertEquals(new Always(new Next(P)), parser.evaluate("[] X p"));
        assertEquals(new Next(new Always(P)), parser.evaluate("X [] p"));
        assertEquals(new Next(new Sometime(P)), parser.evaluate("X <> p"));
        assertEquals(new ImplicationModal(new Next(P), Q), parser.evaluate("X p -> q"));
        assertEquals(new ConjunctionModal(P, new Next(Q)), parser.evaluate("p & X q"));
    }

    @Test
    void untilBindsTighterThanTheBinaryConnectives() {
        assertEquals(new ImplicationModal(new Until(P, Q), R), parser.evaluate("p U q -> r"));
        assertEquals(new ImplicationModal(R, new Until(P, Q)), parser.evaluate("r -> p U q"));
        assertEquals(new ConjunctionModal(new Until(P, Q), R), parser.evaluate("p U q & r"));
        assertEquals(new ConjunctionModal(P, new Until(Q, R)), parser.evaluate("p & q U r"));
        assertEquals(new DisjunctionModal(P, new Until(Q, R)), parser.evaluate("p | q U r"));
    }

    @Test
    void untilBindsLooserThanTheUnaryConnectives() {
        assertEquals(new Until(new NegationModal(P), Q), parser.evaluate("- p U q"));
        assertEquals(new Until(P, new NegationModal(Q)), parser.evaluate("p U - q"));
        assertEquals(new Until(new Next(P), Q), parser.evaluate("X p U q"));
        assertEquals(new Until(P, new Next(Q)), parser.evaluate("p U X q"));
        assertEquals(new Until(new Always(P), new Sometime(Q)), parser.evaluate("[] p U <> q"));
        assertEquals(new NegationModal(new Until(P, Q)), parser.evaluate("- (p U q)"));
        assertEquals(new ImplicationModal(new Until(new NegationModal(P), new Next(Q)), R), parser.evaluate("- p U X q -> r"));
    }

    @Test
    void untilGroupsToTheLeft() {
        assertEquals(new Until(new Until(P, Q), R), parser.evaluate("p U q U r"));
    }

    @Test
    void everyPrintedFormulaIsReadBack() {
        for (var formula : List.of("p U q", "(p U q) U r", "p U (q U r)", "(- p) U q", "p U (- q)", "- (p U q)",
                "(p U q) -> r", "r -> (p U q)", "([] p) U (<> q)", "TRUE U FALSE", "Up U pUq", "[] (p U q)",
                "X p", "X (X p)", "X (- p)", "- (X p)", "(X p) U q", "q | (p & (X (p U q)))", "Xp U pX",
                "([] (p -> (X p))) -> (p -> ([] p))")) {
            var parsed = parser.evaluate(formula);

            assertEquals(formula, parsed.toString());
            assertEquals(parsed, parser.evaluate(parsed.toString()), formula);
        }
    }

    @Test
    void aCapitalUOrXInsideANameIsNotAnOperator() {
        assertEquals(ConstantModal.TRUE, parser.evaluate("TRUE"));
        assertEquals(new ConjunctionModal(ConstantModal.TRUE, P), parser.evaluate("TRUE & p"));
        assertEquals(new VariableModal("pUq"), parser.evaluate("pUq"));
        assertEquals(new VariableModal("U1"), parser.evaluate("U1"));
        assertEquals(new VariableModal("Xp"), parser.evaluate("Xp"));
        assertEquals(new VariableModal("x_X"), parser.evaluate("x_X"));
        assertEquals(new Until(new VariableModal("Up"), new VariableModal("pU")), parser.evaluate("Up U pU"));
        assertEquals(new Next(new VariableModal("Xq")), parser.evaluate("X Xq"));
    }

    @Test
    void relationsAreBetweenStates() {
        assertEquals(new LessEqual("s0", "s1"), parser.evaluate("s0 <= s1"));
        assertEquals(new LessEqual("s0", "s0+1"), parser.evaluate("s0 <= s0+1"));
        assertEquals(new LessEqual("s0+1", "s1+2"), parser.evaluate("s0 + 1 <= s1+1+1"));
        assertEquals(new LessEqual("s0", "s0"), parser.evaluate("s0+0 <= s0"));
        assertEquals(new Equals("s1", "s0+1"), parser.evaluate("s1 = s0+1"));
        assertEquals("s0 <= s0+1", parser.evaluate("s0 <= s0 + 1").toString());
        for (var formula : List.of("(p & q) <= s1", "s0 <= TRUE", "s0 <= s1+", "s0 <= s1+a", "s0 <= 1", "X s0 <= s1")) {
            assertThrows(IllegalArgumentException.class, () -> parser.evaluate(formula), formula);
        }
    }

    @Test
    void theModalOperatorsStillWork() {
        assertEquals(new ConjunctionModal(P, new ImplicationModal(Q, R)), parser.evaluate("p & q -> r"));
        assertEquals(new ImplicationModal(new Always(P), Q), parser.evaluate("[] p -> q"));
        assertEquals(new Sometime(new NegationModal(P)), parser.evaluate("<> - p"));
    }

    @Test
    void anIncompleteFormulaIsRejected() {
        for (var formula : List.of("p U", "U p", "U", "p U U q", "X", "p X q", "X U p")) {
            assertThrows(IllegalArgumentException.class, () -> parser.evaluate(formula), formula);
        }
    }

    @Test
    void nextIsEqualOnlyToTheNextOfAnEqualFormula() {
        var next = new Next(P);

        assertEquals(new Next(P).hashCode(), next.hashCode());
        assertNotEquals(new Next(Q), next);
        assertNotEquals(new Always(P), next);
        assertNotEquals(null, next);
    }

    @Test
    void theModalParserHasNeitherNextNorUntil() {
        var modal = new ModalLogicParser();

        assertFalse(modal.evaluate("p U q") instanceof Until);
        assertFalse(modal.evaluate("X p") instanceof Next);
        assertEquals(new VariableModal("X p"), modal.evaluate("X p"));
        assertThrows(IllegalArgumentException.class, () -> modal.evaluate("p U - q"));
    }
}
