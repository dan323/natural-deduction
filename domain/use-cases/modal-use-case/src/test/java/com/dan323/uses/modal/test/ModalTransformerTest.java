package com.dan323.uses.modal.test;

import com.dan323.expressions.modal.Always;
import com.dan323.expressions.modal.ImplicationModal;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.VariableModal;
import com.dan323.expressions.relation.LessEqual;
import com.dan323.expressions.relation.RelationOperation;
import com.dan323.model.ActionDto;
import com.dan323.proof.modal.*;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.relational.Reflexive;
import com.dan323.uses.modal.ModalProofTransformer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ModalTransformerTest {

    private static final ModalLogicalOperation P = new VariableModal("P");
    private static final ModalLogicalOperation Q = new VariableModal("Q");
    private static final RelationOperation s0LessThans1 = new LessEqual("s0", "s1");
    private static final ModalProofTransformer transformer = new ModalProofTransformer();


    @Test
    public void transformSuccessful() {
        var nd = new ModalNaturalDeduction();
        nd.initializeProof(List.of(new Always(P)), new Always(new ImplicationModal(Q, P)));
        new ModalAssume(s0LessThans1).apply(nd);
        new ModalAssume(Q, "s1").apply(nd);
        new ModalBoxE(1, 2).apply(nd);
        new ModalDeductionTheorem().apply(nd);
        new ModalBoxI().apply(nd);
        assertTrue(transformer.fromProof(nd).isDone());
        assertEquals(nd.toString(), transformer.from(transformer.fromProof(nd)).toString());
    }

    @Test
    public void transformFailed() {
        var nd = new ModalNaturalDeduction();
        nd.initializeProof(List.of(new Always(P)), new Always(new ImplicationModal(Q, P)));
        new ModalAssume(s0LessThans1).apply(nd);
        new ModalAssume(Q, "s1").apply(nd);
        new ModalBoxE(1, 2).apply(nd);
        new ModalDeductionTheorem().apply(nd);
        new ModalBoxI().apply(nd);
        assertTrue(transformer.fromProof(nd).isDone());
        assertEquals(nd.toString(), transformer.from(transformer.fromProof(nd)).toString());
    }

    @Test
    public void transformAction() {
        var actionDto = new ActionDto("Ass", List.of(), Map.of("expression", "P", "state", "s0"));
        var action = transformer.from(actionDto);
        assertInstanceOf(ModalAssume.class, action);
        actionDto = new ActionDto("Rep", List.of(1), Map.of());
        action = transformer.from(actionDto);
        assertInstanceOf(ModalCopy.class, action);
        actionDto = new ActionDto("->I", List.of(), Map.of());
        action = transformer.from(actionDto);
        assertInstanceOf(ModalDeductionTheorem.class, action);
        actionDto = new ActionDto("Refl", List.of(1), Map.of());
        action = transformer.from(actionDto);
        assertInstanceOf(Reflexive.class, action);
    }
}
