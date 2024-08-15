package com.dan323.uses.modal.test;

import com.dan323.uses.LogicalGetActions;
import com.dan323.uses.modal.ModalConfiguration;
import com.dan323.uses.modal.ModalProofTransformer;
import com.dan323.uses.modal.mock.ModalProof;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ModalUseTest {

    @Test
    public void modalSolver() {
        var solver = (new ModalConfiguration()).modalSolver();
        var e = assertThrows(UnsupportedOperationException.class, () -> solver.perform(new ModalProofTransformer().fromProof(ModalProof.naturalDeductionNoAssms())));
        assertEquals("This is not yet implemented", e.getMessage());
    }

    @Test
    public void modalActions() {
        LogicalGetActions actions = (new ModalConfiguration()).modalActions();
        assertEquals(20, actions.perform().size());
    }

}
