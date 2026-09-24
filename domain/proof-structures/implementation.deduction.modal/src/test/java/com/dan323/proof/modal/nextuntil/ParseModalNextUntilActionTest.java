package com.dan323.proof.modal.nextuntil;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.modal.Next;
import com.dan323.expressions.modal.Until;
import com.dan323.expressions.modal.VariableModal;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.modal.ModalAndE1;
import com.dan323.proof.modal.ModalAssume;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParseModalNextUntilActionTest {

    private static final VariableModal P = new VariableModal("p");
    private static final VariableModal Q = new VariableModal("q");

    private static ModalOperation parse(String formula) {
        return ParseModalNextUntilAction.parseExpression(formula);
    }

    private static ModalNextUntilNaturalDeduction proof(String... premises) {
        var proof = new ModalNextUntilNaturalDeduction("s0");
        proof.initializeProof(List.of(premises).stream().map(ParseModalNextUntilActionTest::parse).toList(), parse("p"));
        return proof;
    }

    @Test
    void expressionsAreReadWithNextAndUntil() {
        assertEquals(new Next(P), parse("X p"));
        assertEquals(new Until(P, Q), parse("p U q"));
    }

    @Test
    void theRuleTextsAreRead() {
        assertEquals(new ProofReason("XI", List.of(), List.of(1)), ParseModalNextUntilAction.parseReason("XI [1]"));
        assertEquals(new ProofReason("XE", List.of(), List.of(2)), ParseModalNextUntilAction.parseReason("XE [2]"));
        assertEquals(new ProofReason("Succ", List.of(), List.of(3)), ParseModalNextUntilAction.parseReason("Succ [3]"));
        assertEquals(new ProofReason("UI", List.of(), List.of(1)), ParseModalNextUntilAction.parseReason("UI [1]"));
        assertEquals(new ProofReason("UI", List.of(), List.of(1, 2)), ParseModalNextUntilAction.parseReason("UI [1, 2]"));
        assertEquals(new ProofReason("UE", List.of(), List.of(4)), ParseModalNextUntilAction.parseReason("UE [4]"));
        assertEquals(new ProofReason("U<>", List.of(), List.of(5)), ParseModalNextUntilAction.parseReason("U<> [5]"));
        assertEquals(new ProofReason("Ind", List.of(), List.of(1, 2)), ParseModalNextUntilAction.parseReason("Ind [1,2]"));
    }

    @Test
    void theModalRuleTextsAreStillRead() {
        assertEquals(new ProofReason("->E", List.of(), List.of(1, 2)), ParseModalNextUntilAction.parseReason("->E [1, 2]"));
        assertEquals(new ProofReason("Ass", List.of(), List.of()), ParseModalNextUntilAction.parseReason("Ass"));
        assertNull(ParseModalNextUntilAction.parseReason("XI 1"), "no brackets");
    }

    @Test
    void theActionsAreBuiltByName() {
        var left = (ModalLogicalOperation) parse("r");

        assertEquals(new ModalNextI(1), ParseModalNextUntilAction.parseAction("XI", List.of(1), null, null));
        assertEquals(new ModalNextE(1), ParseModalNextUntilAction.parseAction("XE", List.of(1), null, null));
        assertEquals(new ModalSuccessor(1), ParseModalNextUntilAction.parseAction("Succ", List.of(1), null, null));
        assertEquals(new ModalUntilI1(1, left), ParseModalNextUntilAction.parseAction("UI1", List.of(1), left, null));
        assertEquals(new ModalUntilI2(1, 2), ParseModalNextUntilAction.parseAction("UI2", List.of(1, 2), null, null));
        assertEquals(new ModalUntilE(1), ParseModalNextUntilAction.parseAction("UE", List.of(1), null, null));
        assertEquals(new ModalUntilSometime(1), ParseModalNextUntilAction.parseAction("U<>", List.of(1), null, null));
        assertEquals(new ModalInduction(1, 2), ParseModalNextUntilAction.parseAction("Ind", List.of(1, 2), null, null));
        assertEquals(new ModalAndE1(1), ParseModalNextUntilAction.parseAction("&E1", List.of(1), null, null));
    }

    @Test
    void aProofIsReadBackIntoItsActions() {
        var proof = proof("X p", "q", "p", "X (p U q)", "p U q", "[] (p -> X p)");
        var actions = List.of(
                new ModalNextE(1),
                new ModalSuccessor(1),
                new ModalUntilI1(2, P),
                new ModalUntilI2(3, 4),
                new ModalUntilE(5),
                new ModalUntilSometime(5),
                new ModalInduction(3, 6));
        for (var action : actions) {
            assertTrue(action.isValid(proof), action.getClass().getSimpleName());
            action.apply(proof);
        }
        new ModalNextI(7).apply(proof);

        var parsed = proof.parse();

        assertEquals(new ModalAssume(new Next(P), "s0"), parsed.getFirst());
        assertEquals(actions, parsed.subList(6, 13));
        assertEquals(new ModalNextI(7), parsed.getLast());
    }

    @Test
    void aRuleWithTheWrongNumberOfLinesIsRejected() {
        var proof = new ModalNaturalDeduction("s0");
        var until = new Until(P, Q);
        var nextE = new ProofReason("XE", List.of(), List.of(1, 2));
        var untilI = new ProofReason("UI", List.of(), List.of(1, 2, 3));
        var induction = new ProofReason("Ind", List.of(), List.of(1));

        var nextEError = assertThrows(IllegalArgumentException.class,
                () -> ParseModalNextUntilAction.parseWithReason(proof, P, nextE, "s0"));
        assertEquals("XE takes 1 line", nextEError.getMessage());
        var untilError = assertThrows(IllegalArgumentException.class,
                () -> ParseModalNextUntilAction.parseWithReason(proof, until, untilI, "s0"));
        assertEquals("UI takes one or two lines", untilError.getMessage());
        var inductionError = assertThrows(IllegalArgumentException.class,
                () -> ParseModalNextUntilAction.parseWithReason(proof, P, induction, "s0"));
        assertEquals("Ind takes 2 lines", inductionError.getMessage());
    }
}
