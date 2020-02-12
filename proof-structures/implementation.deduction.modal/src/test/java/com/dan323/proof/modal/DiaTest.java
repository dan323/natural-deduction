package com.dan323.proof.modal;

import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.expresions.modal.Sometime;
import com.dan323.expresions.relation.LessEqual;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class DiaTest {

    private static final String STATE_0 = "s0";
    private static final String STATE_1 = "s1";

    @Test
    public void diaEApply() {
        ModalLogicalOperation mlo = mock(ModalLogicalOperation.class);
        var diaE = new ModalDiaE<>(1, STATE_0);
        var assume = new ModalAssume<>(new Sometime(mlo), STATE_0);
        var proof = new ModalNaturalDeduction<>(STATE_0);

        assertFalse(diaE.isValid(proof));

        assume.apply(proof);

        assertFalse(diaE.isValid(proof));

        var assume2 = new ModalAssume<>(mlo, STATE_1);
        var assume3 = new ModalAssume<>(new LessEqual<>(STATE_0,STATE_1));
        assume2.apply(proof);
        assertFalse(diaE.isValid(proof));
        assume3.apply(proof);
        proof.getSteps().add(new ProofStepModal<>("j",3,mock(ModalLogicalOperation.class),new ProofReason("TST", List.of())));
        assertTrue(diaE.isValid(proof));
    }

    @Test
    public void diaIApply() {
    }

    @Test
    public void diaEValid() {

    }

    @Test
    public void diaIValid() {

    }
}
