package com.dan323.proof.modal;

import com.dan323.expressions.modal.ConjunctionModal;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.VariableModal;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ModalAndTest {

    @Test
    public void modalAndIValid() {
        String state = "i";
        String state2 = "j";
        VariableModal p = new VariableModal("P");
        ModalNaturalDeduction pf = new ModalNaturalDeduction("s0");
        ModalAndI andI = new ModalAndI(1, 2);

        assertFalse(andI.isValid(pf));

        ModalAssume assume = new ModalAssume(p, state);
        ModalAssume assume2 = new ModalAssume(p, state2);
        assume.apply(pf);
        assume2.apply(pf);

        assertFalse(andI.isValid(pf));

        pf.initializeProof(List.of(), mock(ModalLogicalOperation.class));
        assume2 = new ModalAssume(p, state);
        assume.apply(pf);
        assume2.apply(pf);

        assertTrue(andI.isValid(pf));
    }

    @Test
    public void modalAndEApply() {
        ProofStepModal pStep = mock(ProofStepModal.class);
        ModalAndE1 andE1 = new ModalAndE1(1);
        ModalNaturalDeduction pf = new ModalNaturalDeduction("s0");
        pf.getSteps().add(pStep);
        doReturn(1).when(pStep).getAssumptionLevel();
        VariableModal variable = new VariableModal("P");
        VariableModal variable2 = new VariableModal("Q");
        doReturn("i").when(pStep).getState();
        doReturn(new ConjunctionModal(variable, variable2)).when(pStep).getStep();

        andE1.apply(pf);

        Assertions.assertEquals("P", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(1, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("&E [1]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("i:    P           &E [1]", pf.getSteps().get(pf.getSteps().size() - 1).toString());

        ModalAndE2 andE2 = new ModalAndE2(1);
        andE2.apply(pf);

        Assertions.assertEquals("Q", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(1, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("&E [1]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("i:    Q           &E [1]", pf.getSteps().get(pf.getSteps().size() - 1).toString());
    }

    @Test
    public void ModalAndIApply() {
        String state = "i";
        ModalAndI andI = new ModalAndI(1, 2);
        ModalAssume assume1 = new ModalAssume(new VariableModal("P"), state);
        ModalAssume assume2 = new ModalAssume(new VariableModal("Q"), state);
        ModalNaturalDeduction pf = new ModalNaturalDeduction("s0");

        assume1.apply(pf);
        assume2.apply(pf);
        andI.apply(pf);

        Assertions.assertEquals("P & Q", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(2, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("&I [1, 2]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("i:       P & Q           &I [1, 2]", pf.getSteps().get(pf.getSteps().size() - 1).toString());
    }
}
