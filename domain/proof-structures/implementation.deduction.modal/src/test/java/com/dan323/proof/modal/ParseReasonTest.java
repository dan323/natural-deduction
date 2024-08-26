package com.dan323.proof.modal;

import com.dan323.proof.generic.proof.ProofReason;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.dan323.proof.modal.proof.ParseModalAction.parseReason;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParseReasonTest {

    @Test
    public void parseBoxITest() {
        ProofReason reason = parseReason("[]I [1-2]");
        assertEquals(new ProofReason("[]I", List.of(new ProofReason.Range(1,2)), List.of()), reason);
    }

    @Test
    public void parseDiaITest() {
        ProofReason reason = parseReason("<>I [1, 2]");
        assertEquals(new ProofReason("<>I", List.of(), List.of(1, 2)), reason);
    }
}