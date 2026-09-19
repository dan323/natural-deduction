package com.dan323.uses.classical.test;

import com.dan323.model.ActionDto;
import com.dan323.model.ProofDto;
import com.dan323.model.StepDto;
import com.dan323.uses.InvalidActionException;
import com.dan323.uses.InvalidProofException;
import com.dan323.uses.LogicalApplyAction;
import com.dan323.uses.classical.ClassicalProofTransformer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ClassicalTransformerTest {

    private static final ClassicalProofTransformer transformer = new ClassicalProofTransformer();

    private static ProofDto proof(StepDto... steps) {
        return new ProofDto(List.of(steps), "classical", "Q -> P");
    }

    @Test
    public void replayAValidProof() {
        var dto = proof(new StepDto("P", "Ass", 0, Map.of()), new StepDto("Q", "Ass", 1, Map.of()), new StepDto("P", "Rep [1]", 1, Map.of()));
        assertEquals(3, transformer.from(dto).getSteps().size());
    }

    @Test
    public void replayRejectsAStepThatDoesNotFollow() {
        var dto = proof(new StepDto("P", "Ass", 0, Map.of()), new StepDto("P", "->E [1, 1]", 0, Map.of()));
        var exception = assertThrows(InvalidProofException.class, () -> transformer.from(dto));
        assertTrue(exception.getMessage().startsWith("Line 2 "), exception.getMessage());
    }

    @Test
    public void replayRejectsALineThatDoesNotExist() {
        var dto = proof(new StepDto("P", "Ass", 0, Map.of()), new StepDto("P", "Rep [7]", 0, Map.of()));
        var exception = assertThrows(InvalidProofException.class, () -> transformer.from(dto));
        assertTrue(exception.getMessage().startsWith("Line 2 "), exception.getMessage());
    }

    @Test
    public void replayRejectsGarbage() {
        assertThrows(InvalidProofException.class, () -> transformer.from(proof(new StepDto("P Q", "Ass", 0, Map.of()))));
        assertThrows(InvalidProofException.class, () -> transformer.from(proof(new StepDto("P", "Ass", 0, Map.of()), new StepDto("P", "Nope", 0, Map.of()))));
        assertThrows(InvalidProofException.class, () -> transformer.from(new ProofDto(List.of(), "classical", "P Q")));
    }

    @Test
    public void omittedParametersAreAccepted() {
        var applier = new LogicalApplyAction<>(transformer);
        var dto = new ProofDto(List.of(new StepDto("P", "Ass", 0, null)), "classical", "P");
        var result = applier.perform(new ActionDto("NOTI", null, null), dto);
        assertFalse(result.applied());
        assertEquals("Rule NOTI cannot be applied", result.message());
    }

    @Test
    public void rejectedActionsSayWhy() {
        var applier = new LogicalApplyAction<>(transformer);
        var dto = proof(new StepDto("P", "Ass", 0, Map.of()));
        var outOfRange = applier.perform(new ActionDto("COPY", List.of(4), Map.of()), dto);
        assertFalse(outOfRange.applied());
        assertEquals("Line 4 does not exist, the proof has 1 lines", outOfRange.message());
        var notApplicable = applier.perform(new ActionDto("ANDE1", List.of(1), Map.of()), dto);
        assertFalse(notApplicable.applied());
        assertEquals("Rule ANDE1 cannot be applied to lines [1]", notApplicable.message());
        assertEquals(dto, notApplicable.proof());
    }

    @Test
    public void unbuildableActionsAreInvalid() {
        var applier = new LogicalApplyAction<>(transformer);
        var dto = proof(new StepDto("P", "Ass", 0, Map.of()));
        assertThrows(InvalidActionException.class, () -> applier.perform(new ActionDto("NOPE", List.of(1), Map.of()), dto));
        assertThrows(InvalidActionException.class, () -> applier.perform(new ActionDto("ANDI", List.of(1), Map.of()), dto));
        assertThrows(InvalidActionException.class, () -> applier.perform(new ActionDto("ASSUME", List.of(), Map.of("expression", "P Q")), dto));
    }
}
