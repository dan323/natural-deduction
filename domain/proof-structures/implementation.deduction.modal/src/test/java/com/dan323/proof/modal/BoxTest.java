package com.dan323.proof.modal;

import com.dan323.expressions.modal.Always;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.modal.VariableModal;
import com.dan323.expressions.relation.Equals;
import com.dan323.expressions.relation.LessEqual;
import com.dan323.expressions.relation.RelationOperation;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class BoxTest {

    private static final String STATE_0 = "s0";
    private static final String STATE_1 = "s1";

    @Test
    public void boxIValid() {
        var boxI = new ModalBoxI();
        var proof = new ModalNaturalDeduction(STATE_0);
        var prop = mock(ModalLogicalOperation.class);
        var assume = new ModalAssume(new LessEqual(STATE_0, STATE_1));
        var assume2 = new ModalAssume(prop, STATE_0);
        var assume3 = new ModalAssume(mock(ModalLogicalOperation.class), STATE_1);

        // Valid proof
        assume.apply(proof);
        proof.getSteps().add(new ProofStepModal(STATE_1, 1, prop, new ProofReason("TST", Collections.emptyList())));

        assertTrue(boxI.isValid(proof));

        // Empty proof
        proof.initializeProof(Collections.emptyList(), mock(ModalOperation.class));
        assertFalse(boxI.isValid(proof));
        // Conclusion is not valid
        assume.apply(proof);
        proof.getSteps().add(new ProofStepModal(1, mock(RelationOperation.class), new ProofReason("TST", Collections.emptyList())));

        assertFalse(boxI.isValid(proof));

        // No assumption level
        proof.initializeProof(Collections.emptyList(), mock(ModalOperation.class));
        assume.apply(proof);
        proof.getSteps().add(new ProofStepModal(0, mock(RelationOperation.class), new ProofReason("TST", Collections.emptyList())));

        assertFalse(boxI.isValid(proof));

        // Assumption not valid
        proof.initializeProof(Collections.emptyList(), mock(ModalOperation.class));
        assume2.apply(proof);
        proof.getSteps().add(new ProofStepModal(1, mock(RelationOperation.class), new ProofReason("TST", Collections.emptyList())));

        assertFalse(boxI.isValid(proof));

        // Not valid conclusion state
        proof.initializeProof(Collections.emptyList(), mock(ModalOperation.class));
        assume.apply(proof);
        proof.getSteps().add(new ProofStepModal(STATE_0, 1, prop, new ProofReason("TST", List.of())));

        assertFalse(boxI.isValid(proof));

        // Not valid conclusion state
        proof.initializeProof(List.of(), mock(ModalOperation.class));
        assume3.apply(proof);
        assume.apply(proof);
        proof.getSteps().add(new ProofStepModal(STATE_1, 2, prop, new ProofReason("TST", Collections.emptyList())));

        assertFalse(boxI.isValid(proof));
    }

    @Test
    public void boxEValid() {
        var boxE = new ModalBoxE(1, 2);
        var proof = new ModalNaturalDeduction(STATE_0);
        var prop = mock(ModalLogicalOperation.class);
        var assume1 = new ModalAssume(new Always(prop), STATE_0);
        var assume2 = new ModalAssume(new LessEqual(STATE_0, STATE_1));
        var assume3 = new ModalAssume(new Equals("a", STATE_1));

        // Steps not there
        assertFalse(boxE.isValid(proof));

        // All valid
        assume1.apply(proof);
        assume2.apply(proof);
        assertTrue(boxE.isValid(proof));

        // Invalid element (both cases)
        proof.initializeProof(Collections.emptyList(), mock(ModalLogicalOperation.class));
        assume1.apply(proof);
        proof.getSteps().get(proof.getSteps().size() - 1).disable();
        assume2.apply(proof);
        assertFalse(boxE.isValid(proof));

        proof.initializeProof(Collections.emptyList(), mock(ModalLogicalOperation.class));
        assume1.apply(proof);
        assume2.apply(proof);
        proof.getSteps().get(proof.getSteps().size() - 1).disable();
        assertFalse(boxE.isValid(proof));

        // Invalid relation
        proof.initializeProof(Collections.emptyList(), mock(ModalLogicalOperation.class));
        assume1.apply(proof);
        assume3.apply(proof);
        assertFalse(boxE.isValid(proof));
    }

    @Test
    public void boxIApply() {
        var boxI = new ModalBoxI();
        var pf = new ModalNaturalDeduction(STATE_0);
        var prop = new VariableModal("P");
        var assume = new ModalAssume(new LessEqual(STATE_0, STATE_1));

        // Valid proof
        assume.apply(pf);
        pf.getSteps().add(new ProofStepModal(STATE_1, 1, prop, new ProofReason("TST", Collections.emptyList())));
        boxI.apply(pf);

        Assertions.assertEquals("[] P", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(0, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("[]I [1, 2]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("s0: [] P           []I [1, 2]", pf.getSteps().get(pf.getSteps().size() - 1).toString());
    }

    @Test
    public void boxEApply() {
        var boxE = new ModalBoxE(1, 2);
        var pf = new ModalNaturalDeduction(STATE_0);
        var prop = new VariableModal("P");
        var assume1 = new ModalAssume(new Always(prop), STATE_0);
        var assume2 = new ModalAssume(new LessEqual(STATE_0, STATE_1));

        assume1.apply(pf);
        assume2.apply(pf);
        boxE.apply(pf);

        Assertions.assertEquals("P", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(2, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("[]E [1, 2]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("s1:       P           []E [1, 2]", pf.getSteps().get(pf.getSteps().size() - 1).toString());
    }
}
