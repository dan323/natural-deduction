package com.dan323.proof.modal;

import com.dan323.expressions.modal.DisjunctionModal;
import com.dan323.expressions.modal.ImplicationModal;
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

public class ModalOrTest {

    @Test
    public void modalOrIApply() {
        var variableQ = new VariableModal("Q");
        var pStep = mock(ProofStepModal.class);
        var orI1 = new ModalOrI1<String>(1, variableQ);
        var pf = new ModalNaturalDeduction<>("s0");
        pf.getSteps().add(pStep);
        doReturn(1).when(pStep).getAssumptionLevel();
        doReturn("i").when(pStep).getState();
        VariableModal variableP = new VariableModal("P");
        doReturn(variableP).when(pStep).getStep();

        orI1.apply(pf);

        Assertions.assertEquals("P | Q", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(1, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("|I [1]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("i:    P | Q           |I [1]", pf.getSteps().get(pf.getSteps().size() - 1).toString());

        var orI2 = new ModalOrI2<String>(1, variableQ);
        orI2.apply(pf);

        Assertions.assertEquals("Q | P", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(1, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("|I [1]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("i:    Q | P           |I [1]", pf.getSteps().get(pf.getSteps().size() - 1).toString());
    }

    @Test
    public void modalOrEApply() {
        var state = "i";
        var orE = new ModalOrE<String>(1, 2, 3);
        var p = new VariableModal("P");
        var q = new VariableModal("Q");
        var t = new VariableModal("T");
        var assume1 = new ModalAssume<>(new DisjunctionModal(p, q), state);
        var assume2 = new ModalAssume<>(new ImplicationModal(p, t), state);
        var assume3 = new ModalAssume<>(new ImplicationModal(q, t), state);
        var pf = new ModalNaturalDeduction<>("s0");

        assume1.apply(pf);
        assume2.apply(pf);
        assume3.apply(pf);
        orE.apply(pf);

        Assertions.assertEquals("T", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(3, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("|E [1, 2, 3]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("i:          T           |E [1, 2, 3]", pf.getSteps().get(pf.getSteps().size() - 1).toString());
    }

    @Test
    public void modalOrValid() {
        var state = "i";
        var state1 = "j";
        var orE = new ModalOrE<String>(1, 2, 3);
        var p = new VariableModal("P");
        var q = new VariableModal("Q");
        var t = new VariableModal("T");
        var assume1 = new ModalAssume<>(new DisjunctionModal(p, q), state);
        var assume2 = new ModalAssume<>(new ImplicationModal(p, t), state1);
        var assume3 = new ModalAssume<>(new ImplicationModal(q, t), state1);
        var pf = new ModalNaturalDeduction<>("s0");
        assume1.apply(pf);
        assume2.apply(pf);
        assume3.apply(pf);

        assertFalse(orE.isValid(pf));

        assume2 = new ModalAssume<>(new ImplicationModal(p, t), state);
        assume3 = new ModalAssume<>(new ImplicationModal(t, t), state);
        pf.initializeProof(List.of(), mock(ModalLogicalOperation.class));
        assume1.apply(pf);
        assume2.apply(pf);
        assume3.apply(pf);

        assertFalse(orE.isValid(pf));

        assume3 = new ModalAssume<>(new ImplicationModal(q, t), state);
        pf.initializeProof(List.of(), mock(ModalLogicalOperation.class));
        assume1.apply(pf);
        assume2.apply(pf);
        assume3.apply(pf);

        assertTrue(orE.isValid(pf));

    }
}
