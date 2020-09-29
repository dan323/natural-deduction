package com.dan323.proof.modal;

import com.dan323.proof.generic.proof.ProofReason;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.dan323.proof.modal.proof.ParseModalAction.parseReason;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParseReasonTest {

    @Test
    public void parseArrowITest() {
        ProofReason reason = parseReason("->I");
        assertEquals(new ProofReason("->I", List.of()), reason);
    }

    @Test
    public void parseArrowETest() {
        ProofReason reason = parseReason("->E [1,2]");
        assertEquals(new ProofReason("->E", List.of(1, 2)), reason);
    }

    @Test
    public void parseAndETest() {
        ProofReason reason = parseReason("&E [1]");
        assertEquals(new ProofReason("&E", List.of(1)), reason);
    }

    @Test
    public void parseAndITest() {
        ProofReason reason = parseReason("&I [1,5]");
        assertEquals(new ProofReason("&I", List.of(1,5)), reason);
    }

}