package com.dan323.proof.classic;

import com.dan323.proof.generic.proof.ProofReason;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.dan323.classical.proof.ParseClassicalAction.parseReason;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParseProofReasonTest {

    @Test
    public void parseArrowITest() {
        ProofReason reason = parseReason("->I [2-3]");
        assertEquals(new ProofReason("->I", List.of(new ProofReason.Range(2,3)), List.of()), reason);
    }

    @Test
    public void parseArrowETest() {
        ProofReason reason = parseReason("->E [1,2]");
        assertEquals(new ProofReason("->E", List.of(), List.of(1, 2)), reason);
    }

    @Test
    public void parseAndETest() {
        ProofReason reason = parseReason("&E [1]");
        assertEquals(new ProofReason("&E", List.of(), List.of(1)), reason);
    }

    @Test
    public void parseAndITest() {
        ProofReason reason = parseReason("&I [1,5]");
        assertEquals(new ProofReason("&I", List.of(), List.of(1, 5)), reason);
    }
}
