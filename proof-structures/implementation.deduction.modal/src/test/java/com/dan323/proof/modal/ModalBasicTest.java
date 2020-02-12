package com.dan323.proof.modal;

import com.dan323.expresions.modal.VariableModal;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ModalBasicTest {

    @Test
    public void copyTest() {
        ProofStepModal<String> pStep = mock(ProofStepModal.class);
        ModalCopy<String> copy = new ModalCopy<>(1);
        ModalNaturalDeduction<String> pf = new ModalNaturalDeduction<>("s0");
        pf.getSteps().add(pStep);
        doReturn(1).when(pStep).getAssumptionLevel();
        doReturn("i").when(pStep).getState();
        VariableModal variable = new VariableModal("P");
        doReturn(variable).when(pStep).getStep();

        copy.apply(pf);

        Assertions.assertEquals("P", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(1, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("Rep [1]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("i:    P           Rep [1]", pf.getSteps().get(pf.getSteps().size() - 1).toString());
    }

    @Test
    public void assmsTest() {
        VariableModal variable = new VariableModal("P");
        ModalAssume<String> assms = new ModalAssume<>(variable, "i");
        ModalNaturalDeduction<String> pf = new ModalNaturalDeduction<>("s0");

        assms.apply(pf);

        Assertions.assertEquals("P", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(1, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("Ass", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("i:    P           Ass", pf.getSteps().get(pf.getSteps().size() - 1).toString());
    }
}
