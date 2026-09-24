package com.dan323.expressions;

import com.dan323.expressions.modal.Always;
import com.dan323.expressions.modal.ConjunctionModal;
import com.dan323.expressions.modal.ConstantModal;
import com.dan323.expressions.modal.DisjunctionModal;
import com.dan323.expressions.modal.ImplicationModal;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.NegationModal;
import com.dan323.expressions.modal.Sometime;
import com.dan323.expressions.modal.Until;
import com.dan323.expressions.modal.VariableModal;
import com.dan323.expressions.relation.LessEqual;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModalUntilParserTest {

    private static final ModalLogicalOperation P = new VariableModal("p");
    private static final ModalLogicalOperation Q = new VariableModal("q");
    private static final ModalLogicalOperation R = new VariableModal("r");

    private final ModalUntilLogicParser parser = new ModalUntilLogicParser();

    @Test
    void untilIsPrintedAndReadTheSameWay() {
        var until = parser.evaluate("p U q");

        assertEquals(new Until(P, Q), until);
        assertEquals("p U q", until.toString());
        assertEquals(new Until(new Until(P, Q), R), parser.evaluate("(p U q) U r"));
        assertEquals(new Until(P, new Until(Q, R)), parser.evaluate("p U (q U r)"));
        assertEquals(new Until(P, Q), parser.evaluate("(p)U(q)"));
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
        assertEquals(new Until(new Always(P), new Sometime(Q)), parser.evaluate("[] p U <> q"));
        assertEquals(new NegationModal(new Until(P, Q)), parser.evaluate("- (p U q)"));
    }

    @Test
    void untilGroupsToTheLeft() {
        assertEquals(new Until(new Until(P, Q), R), parser.evaluate("p U q U r"));
    }

    @Test
    void everyPrintedFormulaIsReadBack() {
        for (var formula : List.of("p U q", "(p U q) U r", "p U (q U r)", "(- p) U q", "p U (- q)", "- (p U q)",
                "(p U q) -> r", "r -> (p U q)", "([] p) U (<> q)", "TRUE U FALSE", "Up U pUq", "[] (p U q)")) {
            var parsed = parser.evaluate(formula);

            assertEquals(formula, parsed.toString());
            assertEquals(parsed, parser.evaluate(parsed.toString()), formula);
        }
    }

    @Test
    void aCapitalUInsideANameIsNotUntil() {
        assertEquals(ConstantModal.TRUE, parser.evaluate("TRUE"));
        assertEquals(new ConjunctionModal(ConstantModal.TRUE, P), parser.evaluate("TRUE & p"));
        assertEquals(new VariableModal("pUq"), parser.evaluate("pUq"));
        assertEquals(new VariableModal("U1"), parser.evaluate("U1"));
        assertEquals(new Until(new VariableModal("Up"), new VariableModal("pU")), parser.evaluate("Up U pU"));
    }

    @Test
    void theModalOperatorsStillWork() {
        assertEquals(new LessEqual("s0", "s1"), parser.evaluate("s0 <= s1"));
        assertEquals(new ConjunctionModal(P, new ImplicationModal(Q, R)), parser.evaluate("p & q -> r"));
        assertEquals(new ImplicationModal(new Always(P), Q), parser.evaluate("[] p -> q"));
    }

    @Test
    void anIncompleteUntilIsRejected() {
        for (var formula : List.of("p U", "U p", "U", "p U U q")) {
            assertThrows(IllegalArgumentException.class, () -> parser.evaluate(formula), formula);
        }
    }

    @Test
    void theModalParserHasNoUntil() {
        var modal = new ModalLogicParser();

        assertFalse(modal.evaluate("p U q") instanceof Until);
        assertFalse(modal.evaluate("(p U q) -> r").toString().contains("(p U q)"));
        assertThrows(IllegalArgumentException.class, () -> modal.evaluate("p U - q"));
    }
}
