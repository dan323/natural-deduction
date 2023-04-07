package com.dan323.proof.modal;

import com.dan323.expressions.modal.ConjunctionModal;
import com.dan323.expressions.modal.NegationModal;
import com.dan323.expressions.modal.VariableModal;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ModalFalseTest {

    @Test
    public void equalsTest() {
        ModalFI modalFI = new ModalFI(1, 2);
        ModalFI modalFI2 = new ModalFI(1, 4);
        ModalFI modalFI3 = new ModalFI(1, 3);
        ModalFI modalFI4 = new ModalFI(1, 2);

        assertEquals(modalFI, modalFI);
        assertEquals(modalFI, modalFI4);
        assertNotEquals(modalFI, modalFI2);
        assertNotEquals(modalFI, modalFI3);
        assertEquals(modalFI.hashCode(), modalFI4.hashCode());

        VariableModal p = new VariableModal("P");
        ModalFE modalFE = new ModalFE(1, p, "j");
        ModalFE modalFE2 = new ModalFE(1, p, "i");
        ModalFE modalFE3 = new ModalFE(2, p, "i");
        ModalFE modalFE4 = new ModalFE(1, p, "j");

        assertEquals(modalFE, modalFE);
        assertEquals(modalFE, modalFE4);
        assertNotEquals(modalFE, modalFE2);
        assertNotEquals(modalFE, modalFE3);
        assertEquals(modalFE.hashCode(), modalFE4.hashCode());
    }

    @Test
    public void modalFIApply() {
        ModalFI modalFI = new ModalFI(1, 2);
        VariableModal variable = new VariableModal("P");
        ModalAssume assume = new ModalAssume(variable, "i");
        ModalAssume assume2 = new ModalAssume(new NegationModal(variable), "i");
        ModalNaturalDeduction pf = new ModalNaturalDeduction("s0");

        assume.apply(pf);
        assume2.apply(pf);
        modalFI.apply(pf);

        Assertions.assertEquals("FALSE", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(2, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("FI [1, 2]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("s0:       FALSE           FI [1, 2]", pf.getSteps().get(pf.getSteps().size() - 1).toString());
    }

    @Test
    public void modalFIValid() {
        var modalFI = new ModalFI(1, 2);
        var p = new VariableModal("P");
        var assume = new ModalAssume(p, "i");
        var assume2 = new ModalAssume(new NegationModal(p), "i");
        var pr = new ModalNaturalDeduction("s0");
        assume.apply(pr);
        assume2.apply(pr);

        assertTrue(modalFI.isValid(pr));

        pr.getSteps().clear();
        assume = new ModalAssume(p, "j");
        assume.apply(pr);
        assume2.apply(pr);

        assertFalse(modalFI.isValid(pr));

        pr.getSteps().clear();
        assume = new ModalAssume(new ConjunctionModal(p, p), "i");
        assume.apply(pr);
        assume2.apply(pr);

        assertFalse(modalFI.isValid(pr));
    }

    @Test
    public void modalFEApply() {
        var modalFI = new ModalFI(1, 2);
        var variable = new VariableModal("P");
        var assume = new ModalAssume(variable, "i");
        var assume2 = new ModalAssume(new NegationModal(variable), "i");
        var pf = new ModalNaturalDeduction("s0");
        var modalFE = new ModalFE(3, variable, "k");

        assume.apply(pf);
        assume2.apply(pf);
        modalFI.apply(pf);
        modalFE.apply(pf);

        Assertions.assertEquals("P", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(2, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("FE [3]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("k:       P           FE [3]", pf.getSteps().get(pf.getSteps().size() - 1).toString());
    }
}
