package com.dan323.integration;

import com.dan323.classical.proof.AvailableAction;
import com.dan323.proof.firstorder.proof.ParseFirstOrderAction;
import com.dan323.proof.modal.proof.ParseModalAction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The classical, modal and first-order parsers accept each other's names for the rules they share: {@link AvailableAction}
 * knows the modal name of each classical action, and {@link ParseModalAction#ruleName} and
 * {@link ParseFirstOrderAction#ruleName} map the classical names back. None of these modules depends on the others, so
 * this module, which sees them all, checks that the tables agree.
 */
class SharedRuleNamesTest {

    @Test
    void modalAndClassicalNamesAgree() {
        for (AvailableAction action : AvailableAction.values()) {
            assertEquals(action.getRuleName(), ParseModalAction.ruleName(action.name()), action.name());
        }
    }

    @Test
    void firstOrderAndClassicalNamesAgree() {
        for (AvailableAction action : AvailableAction.values()) {
            assertEquals(action.getRuleName(), ParseFirstOrderAction.ruleName(action.name()), action.name());
        }
    }
}
