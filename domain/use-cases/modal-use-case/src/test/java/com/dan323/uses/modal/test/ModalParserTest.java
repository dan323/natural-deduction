package com.dan323.uses.modal.test;

import com.dan323.expressions.modal.Always;
import com.dan323.expressions.modal.ImplicationModal;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.VariableModal;
import com.dan323.expressions.relation.LessEqual;
import com.dan323.expressions.relation.RelationOperation;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.modal.ModalAssume;
import com.dan323.proof.modal.ModalCopy;
import com.dan323.proof.modal.ModalDeductionTheorem;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;
import com.dan323.uses.modal.ModalProofParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ModalParserTest {

    private static final ModalLogicalOperation P = new VariableModal("P");
    private static final ModalLogicalOperation Q = new VariableModal("Q");
    private static final ModalLogicalOperation QimpP = new ImplicationModal(Q, P);
    private static final RelationOperation s0LessS1 = new LessEqual("s0", "s1");

    @Test
    public void parseProofSuccessfully() {
        var parser = new ModalProofParser();
        var nd = new ModalNaturalDeduction();
        nd.initializeProof(List.of(P), QimpP);
        var assmsAction = new ModalAssume(Q, "s0");
        var copyAction = new ModalCopy(1);
        var deductionTheoremAction = new ModalDeductionTheorem();
        assmsAction.apply(nd);
        copyAction.apply(nd);
        deductionTheoremAction.apply(nd);
        var parsedProof = parser.parseProof(nd.toString());
        assertEquals(4, parsedProof.getSteps().size());
        assertEquals(nd.toString(), parsedProof.toString());
    }

    @Test
    public void parseProofRelationSuccessfully() {
        var parser = new ModalProofParser();
        var nd = new ModalNaturalDeduction();
        nd.initializeProof(List.of(new Always(P), s0LessS1), P);
        var parsedProof = parser.parseProof(nd.toString());
        assertEquals(2, parsedProof.getSteps().size());
        assertEquals(nd.toString(), parsedProof.toString());
    }

    @Test
    public void parseProofSuccessfulFailed() {
        var parser = new ModalProofParser();
        var nd = new ModalNaturalDeduction();
        nd.initializeProof(List.of(P), QimpP);
        var parsedProof = parser.parseProof(nd.toString());
        assertEquals(1, parsedProof.getSteps().size());
        assertNotEquals(nd.getGoal(), parsedProof.getGoal());
    }


    @Test
    public void parseProofFailed() {
        var parser = new ModalProofParser();
        var nd = new ModalNaturalDeduction();
        nd.initializeProof(List.of(P), QimpP);
        nd.getSteps().add(new ProofStepModal("s0", 1, Q, new ProofReason("Ass", List.of())));
        var exception = assertThrowsExactly(IllegalArgumentException.class, () -> parser.parseProof(nd.toString()));
        assertTrue(exception.getMessage().contains("invalid"));
    }
}
