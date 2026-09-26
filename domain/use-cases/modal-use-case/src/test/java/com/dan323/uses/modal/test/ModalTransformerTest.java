package com.dan323.uses.modal.test;

import com.dan323.expressions.modal.Always;
import com.dan323.expressions.modal.ImplicationModal;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.VariableModal;
import com.dan323.expressions.relation.LessEqual;
import com.dan323.expressions.relation.RelationOperation;
import com.dan323.model.ActionDto;
import com.dan323.uses.InvalidProofException;
import com.dan323.model.ProofDto;
import com.dan323.model.StepDto;
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

        var nd3 = new ProofDto(List.of(new StepDto("P", "Ass", 0, Map.of("state", "s0")), new StepDto("P", "Rep [1]", 0, Map.of())), "modal", "P -> P");
        assertEquals(2, transformer.from(nd3).getSteps().size());
    }

    @Test
    public void transformFailed() {
        var nd = new ProofDto(List.of(new StepDto("->P", "Ass", 0, Map.of())), "modal", "P->P");
        var ex = assertThrows(IllegalArgumentException.class, () -> transformer.from(nd));
        var nd2 = new ProofDto(List.of(new StepDto("P", "Ass", 0, Map.of())), "modal", "P -> P");
        ex = assertThrows(IllegalArgumentException.class, () -> transformer.from(nd2));
        assertTrue(ex.getMessage().contains("not in a valid state"));
        var nd3 = new ProofDto(List.of(new StepDto("P", "Ass", 0, Map.of("state", "s0")), new StepDto("P", "Rep", 0, Map.of())), "modal", "P -> P");
        assertThrows(InvalidProofException.class, () -> transformer.from(nd3));
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
        actionDto = new ActionDto("->E", List.of(1, 2), Map.of());
        action = transformer.from(actionDto);
        assertInstanceOf(ModalModusPonens.class, action);
        var actionInError = new ActionDto("->E", List.of(), Map.of());
        assertThrows(IndexOutOfBoundsException.class, () -> transformer.from(actionInError));
    }

    @Test
    public void actionsWithAnExpressionKeepTheParsedExpression() {
        var nd = new ModalNaturalDeduction();
        nd.initializeProof(List.of(), P);
        var assume = transformer.from(new ActionDto("Ass", List.of(), Map.of("expression", " P -> Q ", "state", "s0")));
        assertNotNull(assume);
        assume.apply(nd);
        assertEquals(new ImplicationModal(P, Q), nd.getSteps().getLast().getStep());

        var relation = transformer.from(new ActionDto("Ass", List.of(), Map.of("expression", "s0 <= s1")));
        assertNotNull(relation);
        relation.apply(nd);
        assertEquals(s0LessThans1, nd.getSteps().getLast().getStep());

        var orI = transformer.from(new ActionDto("|I1", List.of(1), Map.of("expression", "Q", "state", "s0")));
        assertInstanceOf(ModalOrI1.class, orI);
    }

    @Test
    public void omittedExtraParametersAreAccepted() {
        var relation = new ProofDto(List.of(new StepDto("s0 <= s1", "Ass", 0, null)), "modal", "P");
        assertEquals(1, transformer.from(relation).getSteps().size());
        var noState = new ProofDto(List.of(new StepDto("P", "Ass", 0, null)), "modal", "P");
        var exception = assertThrows(InvalidProofException.class, () -> transformer.from(noState));
        assertTrue(exception.getMessage().contains("not in a valid state"));
        assertInstanceOf(ModalCopy.class, transformer.from(new ActionDto("Rep", List.of(1), null)));
    }

    @Test
    public void replayRejectsAStepThatDoesNotFollow() {
        var dto = new ProofDto(List.of(new StepDto("P", "Ass", 0, Map.of("state", "s0")), new StepDto("P", "->E [1, 1]", 0, Map.of("state", "s0"))), "modal", "P");
        var exception = assertThrows(InvalidProofException.class, () -> transformer.from(dto));
        assertTrue(exception.getMessage().startsWith("Line 2 "), exception.getMessage());
    }

    @Test
    public void classicalNamesAreAccepted() {
        assertInstanceOf(ModalCopy.class, transformer.from(new ActionDto("COPY", List.of(1), Map.of())));
        assertInstanceOf(ModalModusPonens.class, transformer.from(new ActionDto("MP", List.of(1, 2), Map.of())));
        var missingExpression = new ActionDto("ASSUME", List.of(), Map.of("state", "s0"));
        var exception = assertThrows(com.dan323.uses.InvalidActionException.class, () -> transformer.from(missingExpression));
        assertEquals("ASSUME needs an expression", exception.getMessage());
    }
}
