package com.dan323.proof.modal;

import com.dan323.expressions.modal.VariableModal;
import com.dan323.expressions.relation.LessEqual;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;
import com.dan323.proof.modal.proof.ParseModalAction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Modal logic keeps its freshness and its rules now that {@code modal-next-until} extends it.
 */
class ModalFreshStateTest {

    @Test
    void aFreshStateIsOneThatIsNotUsedBefore() {
        var proof = new ModalNaturalDeduction("s0");
        proof.initializeProof(List.of(new VariableModal("p")), new VariableModal("p"));
        new ModalAssume(new LessEqual("s1", "s2")).apply(proof);
        new ModalAssume(new VariableModal("q"), "s3").apply(proof);

        for (var state : List.of("s0", "s1", "s2", "s3", "s4", "s0+1", "s1+1")) {
            for (int k = 0; k <= proof.getSteps().size(); k++) {
                for (var from : List.of("s0", "s1", state)) {
                    assertEquals(!proof.stateIsUsedBefore(state, k), proof.isFreshState(state, from, k), state + " at " + k + " from " + from);
                }
            }
        }
    }

    @Test
    void aStateWrittenAsASuccessorIsJustAnotherName() {
        // In modal logic s0+1 is a name like s1: []I may introduce it.
        var proof = new ModalNaturalDeduction("s0");
        proof.initializeProof(List.of(), new VariableModal("p"));
        new ModalAssume(new LessEqual("s0", "s0+1")).apply(proof);
        proof.getSteps().add(new ProofStepModal("s0+1", 1, new VariableModal("q"), new ProofReason("TST", List.of(), List.of())));

        assertTrue(new ModalBoxI().isValid(proof));
    }

    @Test
    void theNextAndUntilRulesAreNotModalRules() {
        for (var name : List.of("XI", "XE", "Succ", "UI1", "UI2", "UE", "U<>", "Ind")) {
            assertThrows(IllegalArgumentException.class, () -> ParseModalAction.parseAction(name, List.of(1, 1), null, "s0"), name);
        }
        for (var rule : List.of("XI [1]", "XE [1]", "UI [1]", "UE [1]", "Ind [1, 2]")) {
            assertNull(ParseModalAction.parseReason(rule), rule);
        }
    }
}
