package com.dan323.proof.modal;

import com.dan323.expressions.modal.ImplicationModal;
import com.dan323.expressions.modal.VariableModal;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModalImpTest {

    @Test
    public void modalDTValid() {
        var deductionTheorem = new ModalDeductionTheorem<String>();
        var pf = new ModalNaturalDeduction<>("s0");
        assertFalse(deductionTheorem.isValid(pf));

        var assume = new ModalAssume<>(new VariableModal("P"), "i");
        assume.apply(pf);
        assertTrue(deductionTheorem.isValid(pf));

        pf.getSteps().add(new ProofStepModal<>("j", 1, new VariableModal("Q"), new ProofReason("TST", List.of())));
        assertFalse(deductionTheorem.isValid(pf));
    }

    @Test
    public void modalDTApply() {
        var state = "i";
        var deductionTheorem = new ModalDeductionTheorem<String>();
        var variable = new VariableModal("P");
        var assume = new ModalAssume<>(variable, state);
        var pf = new ModalNaturalDeduction<>("s0");

        assume.apply(pf);
        deductionTheorem.apply(pf);

        Assertions.assertEquals("P -> P", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(0, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("->I [1, 1]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("i: P -> P           ->I [1, 1]", pf.getSteps().get(pf.getSteps().size() - 1).toString());
    }

    @Test
    public void modalMPValid() {
        var state = "i";
        var pr = new ModalNaturalDeduction<>("s0");
        var mp = new ModalModusPonens<String>(2, 1);
        var p = new VariableModal("P");
        var q = new VariableModal("Q");
        var assume1 = new ModalAssume<>(p, state);
        var assume2 = new ModalAssume<>(new ImplicationModal(p, q), "j");

        assume1.apply(pr);
        assertFalse(mp.isValid(pr));

        assume2.apply(pr);
        assertFalse(mp.isValid(pr));

        pr.getSteps().remove(1);
        assume2 = new ModalAssume<>(new ImplicationModal(p, q), state);
        assume2.apply(pr);
        assertTrue(mp.isValid(pr));
    }

    @Test
    public void modalMPApply() {
        var state = "i";
        var modusPonens = new ModalModusPonens<String>(1, 2);
        var variable = new VariableModal("P");
        var varImpVar = new ImplicationModal(variable, variable);
        var assume = new ModalAssume<>(variable, state);
        var assume2 = new ModalAssume<>(varImpVar, state);
        var pf = new ModalNaturalDeduction<>("s0");

        assume2.apply(pf);
        assume.apply(pf);
        modusPonens.apply(pf);

        Assertions.assertEquals("P", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(2, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("->E [1, 2]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("i:       P           ->E [1, 2]", pf.getSteps().get(pf.getSteps().size() - 1).toString());
    }
}
