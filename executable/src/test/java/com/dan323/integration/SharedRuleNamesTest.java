package com.dan323.integration;

import com.dan323.classical.proof.AvailableAction;
import com.dan323.proof.modal.proof.ParseModalAction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The classical and modal parsers accept each other's names for the rules they share: {@link AvailableAction} knows the
 * modal name of each classical action, and {@link ParseModalAction#ruleName} maps the classical names back. Neither
 * module depends on the other, so this module, which sees both, checks that the two tables agree.
 */
class SharedRuleNamesTest {

    @Test
    void modalAndClassicalNamesAgree() {
        for (AvailableAction action : AvailableAction.values()) {
            assertEquals(action.getRuleName(), ParseModalAction.ruleName(action.name()), action.name());
        }
    }
}
