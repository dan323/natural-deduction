package com.dan323.proof.classic;

import com.dan323.classical.proof.AvailableAction;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AvailableActionTest {

    @Test
    void onlyDoubleNegationEliminationIsClassicalOnly() {
        var classicalOnly = Arrays.stream(AvailableAction.values()).filter(action -> !action.isIntuitionistic()).toList();
        assertEquals(List.of(AvailableAction.NOTE), classicalOnly);
    }
}
