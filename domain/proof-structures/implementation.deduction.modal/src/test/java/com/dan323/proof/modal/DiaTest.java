package com.dan323.proof.modal;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.Sometime;
import com.dan323.expressions.modal.VariableModal;
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

public class DiaTest {

    private static final String STATE_0 = "s0";
    private static final String STATE_1 = "s1";

    @Test
    public void diaEApply() {
        var diaE = new ModalDiaE<String>(1);
        var mlo = new VariableModal("P");
        var conclusion = new VariableModal("Q");
        var initStep = new Sometime(mlo);
        var assume = new ModalAssume<>(mlo, STATE_1);
        var assume2 = new ModalAssume<>(new LessEqual<>(STATE_0, STATE_1));
        var pf = new ModalNaturalDeduction<>(STATE_0);

        pf.initializeProof(Collections.singletonList(initStep), mock(ModalLogicalOperation.class));

        assume.apply(pf);
        assume2.apply(pf);
        pf.getSteps().add(new ProofStepModal<>(STATE_0, 2, conclusion, new ProofReason("TST", List.of())));
        diaE.apply(pf);

        Assertions.assertEquals("Q", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(0, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("<>E [1, 2, 4]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("s0: Q           <>E [1, 2, 4]", pf.getSteps().get(pf.getSteps().size() - 1).toString());
    }

    @Test
    public void diaIApply() {
        // Init variables
        var mlo = new VariableModal("P");
        var diaI = new ModalDiaI<String>(2, 1);
        var pf = new ModalNaturalDeduction<>(STATE_0);
        var initStep = new LessEqual<>(STATE_0, STATE_1);
        var assume = new ModalAssume<>(mlo, STATE_1);

        pf.initializeProof(Collections.singletonList(initStep), mock(ModalLogicalOperation.class));
        assume.apply(pf);
        diaI.apply(pf);

        Assertions.assertEquals("<> P", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(1, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("<>I [2, 1]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("s0:    <> P           <>I [2, 1]", pf.getSteps().get(pf.getSteps().size() - 1).toString());
    }

    @Test
    public void diaEValid() {
        // Init variables
        var mlo = mock(ModalLogicalOperation.class);
        var diaE = new ModalDiaE<String>(1);
        var initStep = new Sometime(mlo);
        var assume = new ModalAssume<>(mock(ModalLogicalOperation.class), STATE_1);
        var assume2 = new ModalAssume<>(mlo, STATE_1);
        var assume3 = new ModalAssume<>(new LessEqual<>(STATE_0, STATE_1));
        var assume4 = new ModalAssume<>(new LessEqual<>("q", STATE_1));
        var proof = new ModalNaturalDeduction<>(STATE_0);

        // Empty proof
        assertFalse(diaE.isValid(proof));

        // Not all assms
        proof.initializeProof(Collections.singletonList(initStep), mock(ModalLogicalOperation.class));

        assume2.apply(proof);
        assertFalse(diaE.isValid(proof));

        // Only assms, no conclusion
        proof.initializeProof(Collections.singletonList(initStep), mock(ModalLogicalOperation.class));

        assume2.apply(proof);
        assume3.apply(proof);
        assertFalse(diaE.isValid(proof));

        // Valid conclusion
        proof.getSteps().add(new ProofStepModal<>(STATE_0, 2, mock(ModalLogicalOperation.class), new ProofReason("TST", List.of())));
        assertTrue(diaE.isValid(proof));

        // assms are not of correct type
        proof.initializeProof(Collections.singletonList(initStep), mock(ModalLogicalOperation.class));

        assume2.apply(proof);
        assume2.apply(proof);
        assertFalse(diaE.isValid(proof));

        // Invalid assumption level of conclusion
        proof.initializeProof(Collections.singletonList(initStep), mock(ModalLogicalOperation.class));

        assume2.apply(proof);
        assume3.apply(proof);
        proof.getSteps().add(new ProofStepModal<String>(3, mock(RelationOperation.class), new ProofReason("TST", List.of())));
        assertFalse(diaE.isValid(proof));

        // Invalid expression before last assumption
        proof.initializeProof(Collections.singletonList(initStep), mock(ModalLogicalOperation.class));

        assume2.apply(proof);
        proof.getSteps().get(proof.getSteps().size() - 1).disable();
        assume3.apply(proof);
        proof.getSteps().add(new ProofStepModal<String>(proof.getSteps().get(proof.getSteps().size() - 1).getAssumptionLevel(), mock(RelationOperation.class), new ProofReason("TST", List.of())));
        assertFalse(diaE.isValid(proof));

        // Invalid assumption level before last assumption
        proof.initializeProof(Collections.singletonList(initStep), mock(ModalLogicalOperation.class));

        assume2.apply(proof);
        assume3.apply(proof);
        proof.getSteps().add(new ProofStepModal<String>(3, mock(RelationOperation.class), new ProofReason("TST", List.of())));
        proof.getSteps().add(new ProofStepModal<String>(3, mock(RelationOperation.class), new ProofReason("TST", List.of())));
        assertFalse(diaE.isValid(proof));

        // Invalid conclusion
        proof.initializeProof(Collections.singletonList(initStep), mock(ModalLogicalOperation.class));

        assume2.apply(proof);
        assume3.apply(proof);
        proof.getSteps().add(new ProofStepModal<>(STATE_1, 2, mock(ModalLogicalOperation.class), new ProofReason("TST", List.of())));
        assertFalse(diaE.isValid(proof));

        // Invalid <= assms
        proof.initializeProof(Collections.singletonList(initStep), mock(ModalLogicalOperation.class));

        assume2.apply(proof);
        assume4.apply(proof);
        proof.getSteps().add(new ProofStepModal<>(STATE_0, 2, mock(ModalLogicalOperation.class), new ProofReason("TST", List.of())));
        assertFalse(diaE.isValid(proof));

        //Invalid logic expression assms
        proof.initializeProof(Collections.singletonList(initStep), mock(ModalLogicalOperation.class));

        assume.apply(proof);
        assume3.apply(proof);
        proof.getSteps().add(new ProofStepModal<>(STATE_0, 2, mock(ModalLogicalOperation.class), new ProofReason("TST", List.of())));
        assertFalse(diaE.isValid(proof));

        // Invalid y in x<=y conclusion
        proof.initializeProof(Collections.singletonList(initStep), mock(ModalLogicalOperation.class));

        var lessEq = new LessEqual<>(STATE_0, STATE_1);
        assume2.apply(proof);
        assume3.apply(proof);
        proof.getSteps().add(new ProofStepModal<>(2, lessEq, new ProofReason("TST", List.of())));
        assertFalse(diaE.isValid(proof));

        // Invalid x in x<=y conclusion
        proof.initializeProof(Collections.singletonList(initStep), mock(ModalLogicalOperation.class));

        lessEq = new LessEqual<>(STATE_1, STATE_0);
        assume2.apply(proof);
        assume3.apply(proof);
        proof.getSteps().add(new ProofStepModal<>(2, lessEq, new ProofReason("TST", List.of())));
        assertFalse(diaE.isValid(proof));

        // Valid x<=y conclusion
        proof.initializeProof(Collections.singletonList(initStep), mock(ModalLogicalOperation.class));

        lessEq = new LessEqual<>(STATE_0, STATE_0);
        assume2.apply(proof);
        assume3.apply(proof);
        proof.getSteps().add(new ProofStepModal<>(2, lessEq, new ProofReason("TST", List.of())));
        assertTrue(diaE.isValid(proof));
    }

    @Test
    public void diaIValid() {
        var diaI = new ModalDiaI<String>(2, 1);
        var proof = new ModalNaturalDeduction<>(STATE_0);
        var initStep = new LessEqual<>(STATE_0, STATE_1);
        var expression = mock(ModalLogicalOperation.class);
        var assume = new ModalAssume<>(expression, STATE_1);
        var assume2 = new ModalAssume<>(expression, "a");

        // No expression valid
        proof.initializeProof(Collections.singletonList(initStep), mock(ModalLogicalOperation.class));

        assertFalse(diaI.isValid(proof));

        // Valid proof state
        assume.apply(proof);
        assertTrue(diaI.isValid(proof));
        // Expression is not longer valid
        proof.getSteps().get(proof.getSteps().size() - 1).disable();
        assertFalse(diaI.isValid(proof));

        // Invalid states to apply the rule
        proof.initializeProof(Collections.singletonList(initStep), mock(ModalLogicalOperation.class));

        assume2.apply(proof);
        assertFalse(diaI.isValid(proof));
    }
}
