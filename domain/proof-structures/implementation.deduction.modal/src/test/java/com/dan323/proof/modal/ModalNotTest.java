package com.dan323.proof.modal;

import com.dan323.expressions.modal.ConstantModal;
import com.dan323.expressions.modal.NegationModal;
import com.dan323.expressions.modal.VariableModal;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ModalNotTest {

    @Test
    public void modalNotIApply() {
        var modalnoti = new ModalNotI();
        String state = "i";
        var variable = new VariableModal("P");
        var variable2 = new VariableModal("Q");
        var variable3 = new VariableModal("R");
        var bottom = ConstantModal.FALSE;
        var assume = new ModalAssume(variable, state);
        var assume2 = new ModalAssume(variable2, "w");
        var assume3 = new ModalAssume(variable3, "w");
        var pf = new ModalNaturalDeduction("s0");

        assume.apply(pf);
        pf.getSteps().add(new ProofStepModal(state, 1, bottom, new ProofReason("TST", List.of())));
        modalnoti.apply(pf);

        Assertions.assertEquals("- P", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(0, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("-I [1, 2]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("i: - P           -I [1, 2]", pf.getSteps().get(pf.getSteps().size() - 1).toString());

        assume2.apply(pf);
        assume3.apply(pf);
        assume.apply(pf);
        pf.getSteps().add(new ProofStepModal(state, 3, bottom, new ProofReason("TST", List.of())));
        modalnoti.apply(pf);

        Assertions.assertEquals("- P", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(2, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("-I [6, 7]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("i:       - P           -I [6, 7]", pf.getSteps().get(pf.getSteps().size() - 1).toString());
    }

    @Test
    public void ModalNotEApply() {
        var state = "i";
        var notE = new ModalNotE(1);
        var negneg = new NegationModal(new NegationModal(new VariableModal("P")));
        var assume = new ModalAssume(negneg, state);
        var pf = new ModalNaturalDeduction("s0");

        assume.apply(pf);
        notE.apply(pf);

        Assertions.assertEquals("P", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(1, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("-E [1]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("i:    P           -E [1]", pf.getSteps().get(pf.getSteps().size() - 1).toString());
    }
}
