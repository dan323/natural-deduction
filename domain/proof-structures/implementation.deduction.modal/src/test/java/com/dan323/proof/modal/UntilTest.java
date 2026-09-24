package com.dan323.proof.modal;

import com.dan323.expressions.modal.Always;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.modal.NegationModal;
import com.dan323.expressions.modal.Sometime;
import com.dan323.expressions.modal.Until;
import com.dan323.expressions.modal.VariableModal;
import com.dan323.expressions.relation.LessEqual;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ParseModalAction;
import com.dan323.proof.modal.until.ModalUntilE1;
import com.dan323.proof.modal.until.ModalUntilE2;
import com.dan323.proof.modal.until.ModalUntilI1;
import com.dan323.proof.modal.until.ModalUntilI2;
import com.dan323.proof.modal.until.ParseModalUntilAction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UntilTest {

    private static final ModalLogicalOperation P = new VariableModal("p");
    private static final ModalLogicalOperation Q = new VariableModal("q");
    private static final Until P_UNTIL_Q = new Until(P, Q);

    private static ModalNaturalDeduction proof(List<ModalOperation> premises) {
        var pf = ParseModalUntilAction.newProof("s0");
        pf.initializeProof(premises, P);
        return pf;
    }

    @Test
    void untilNow() {
        var pf = proof(List.of(Q));
        var action = new ModalUntilI1(1, P);

        assertTrue(action.isValid(pf));
        action.apply(pf);

        assertEquals("s0: p U q           UI [1]", pf.getSteps().getLast().toString());
        assertEquals(P_UNTIL_Q, pf.getSteps().getLast().getStep());
    }

    @Test
    void untilNowKeepsTheStateAndLevel() {
        var pf = proof(List.of(new LessEqual("s0", "s1")));
        new ModalAssume(Q, "s1").apply(pf);
        new ModalUntilI1(2, P).apply(pf);

        assertEquals("s1", pf.getSteps().getLast().getState());
        assertEquals(1, pf.getSteps().getLast().getAssumptionLevel());
    }

    @Test
    void untilNowIsInvalid() {
        var pf = proof(List.of(Q, new LessEqual("s0", "s1")));

        assertFalse(new ModalUntilI1(1, null).isValid(pf), "no left side");
        assertFalse(new ModalUntilI1(2, P).isValid(pf), "a relation is not a formula");
        assertFalse(new ModalUntilI1(0, P).isValid(pf));
        assertFalse(new ModalUntilI1(3, P).isValid(pf));
        pf.getSteps().getFirst().disable();
        assertFalse(new ModalUntilI1(1, P).isValid(pf), "the line is no longer available");
    }

    @Test
    void untilLater() {
        var pf = proof(List.of(new Always(P), new Sometime(Q)));
        var action = new ModalUntilI2(1, 2);

        assertTrue(action.isValid(pf));
        action.apply(pf);

        assertEquals("s0: p U q           UI [1, 2]", pf.getSteps().getLast().toString());
    }

    @Test
    void untilLaterIsInvalid() {
        var pf = proof(List.of(new Always(P), new Sometime(Q), P, new LessEqual("s0", "s1")));
        new ModalAssume(new Sometime(Q), "s1").apply(pf);

        assertFalse(new ModalUntilI2(2, 1).isValid(pf), "the order of the lines matters");
        assertFalse(new ModalUntilI2(1, 3).isValid(pf), "needs ◇B");
        assertFalse(new ModalUntilI2(3, 2).isValid(pf), "needs □A");
        assertFalse(new ModalUntilI2(1, 5).isValid(pf), "different states");
        assertFalse(new ModalUntilI2(4, 2).isValid(pf), "a relation");
        assertFalse(new ModalUntilI2(1, 9).isValid(pf));
    }

    @Test
    void untilEventually() {
        var pf = proof(List.of(P_UNTIL_Q));
        var action = new ModalUntilE1(1);

        assertTrue(action.isValid(pf));
        action.apply(pf);

        assertEquals("s0: <> q           UE [1]", pf.getSteps().getLast().toString());
    }

    @Test
    void untilEventuallyIsInvalid() {
        var pf = proof(List.of(P, new LessEqual("s0", "s1")));

        assertFalse(new ModalUntilE1(1).isValid(pf));
        assertFalse(new ModalUntilE1(2).isValid(pf));
        assertFalse(new ModalUntilE1(3).isValid(pf));
    }

    @Test
    void untilNotYet() {
        var pf = proof(List.of(P_UNTIL_Q, new NegationModal(Q)));
        var action = new ModalUntilE2(1, 2);

        assertTrue(action.isValid(pf));
        action.apply(pf);

        assertEquals("s0: p           UE [1, 2]", pf.getSteps().getLast().toString());
    }

    @Test
    void untilNotYetIsInvalid() {
        var pf = proof(List.of(P_UNTIL_Q, new NegationModal(P), new NegationModal(Q), new LessEqual("s0", "s1")));
        new ModalAssume(new NegationModal(Q), "s1").apply(pf);

        assertFalse(new ModalUntilE2(1, 2).isValid(pf), "¬A instead of ¬B");
        assertFalse(new ModalUntilE2(3, 1).isValid(pf), "the order of the lines matters");
        assertFalse(new ModalUntilE2(1, 5).isValid(pf), "¬B in another state");
        assertFalse(new ModalUntilE2(1, 4).isValid(pf), "a relation");
        assertFalse(new ModalUntilE2(1, 9).isValid(pf));
    }

    @Test
    void theRulesAreReadBackFromTheirSteps() {
        var pf = proof(List.of(Q, new Always(P), new Sometime(Q), new NegationModal(Q)));
        new ModalUntilI1(1, P).apply(pf);
        new ModalUntilI2(2, 3).apply(pf);
        new ModalUntilE1(5).apply(pf);
        new ModalUntilE2(6, 4).apply(pf);

        assertEquals(List.of(new ModalUntilI1(1, P), new ModalUntilI2(2, 3), new ModalUntilE1(5), new ModalUntilE2(6, 4)),
                pf.parse().subList(4, 8));
        for (int line = 5; line <= 8; line++) {
            var step = pf.getSteps().get(line - 1);
            assertEquals(step.getProof(), ParseModalUntilAction.parseReason(step.getProof().toString()));
        }
    }

    @Test
    void untilActionsAreBuiltByName() {
        assertEquals(new ModalUntilI1(1, P), ParseModalUntilAction.parseAction("UI1", List.of(1), P, null));
        assertEquals(new ModalUntilI2(1, 2), ParseModalUntilAction.parseAction("UI2", List.of(1, 2), null, null));
        assertEquals(new ModalUntilE1(3), ParseModalUntilAction.parseAction("UE1", List.of(3), null, null));
        assertEquals(new ModalUntilE2(3, 4), ParseModalUntilAction.parseAction("UE2", List.of(3, 4), null, null));
        assertEquals(new ModalCopy(1), ParseModalUntilAction.parseAction("Rep", List.of(1), null, null));
        assertThrows(IllegalArgumentException.class, () -> ParseModalUntilAction.parseAction("Nope", List.of(), null, null));
    }

    @Test
    void theModalRulesAreStillRead() {
        assertEquals(new ProofReason("[]E", List.of(), List.of(1, 2)), ParseModalUntilAction.parseReason("[]E [1, 2]"));
        assertEquals(new ProofReason("UE", List.of(), List.of(1, 2)), ParseModalUntilAction.parseReason("UE [1, 2]"));
        assertNull(ParseModalUntilAction.parseReason("Nope [1]"));
        assertEquals(P_UNTIL_Q, ParseModalUntilAction.parseExpression("p U q"));
    }

    @Test
    void modalLogicHasNoUntilRules() {
        assertNull(ParseModalAction.parseReason("UI [1]"));
        assertNull(ParseModalAction.parseReason("UE [1, 2]"));
        assertThrows(IllegalArgumentException.class, () -> ParseModalAction.parseAction("UI1", List.of(1), P, null));
        var pf = new ModalNaturalDeduction("s0");
        pf.initializeProof(List.of(Q), P);
        new ModalUntilI1(1, P).apply(pf);
        assertThrows(IllegalStateException.class, pf::parse);
    }

    @Test
    void untilIsSolvedAsIfItWereAVariable() {
        var pf = ParseModalUntilAction.newProof("s0");
        pf.initializeProof(List.of(P_UNTIL_Q), new NegationModal(new NegationModal(P_UNTIL_Q)));
        pf.automate();
        assertTrue(pf.isDone());

        var unsolved = ParseModalUntilAction.newProof("s0");
        unsolved.initializeProof(List.of(Q), P_UNTIL_Q);
        unsolved.automate();
        assertFalse(unsolved.isDone(), "the solver does not use the Until rules");
    }
}
