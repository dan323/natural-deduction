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
    void replayAValidProof() {
        var dto = proof(new StepDto("P", "Ass", 0, Map.of()), new StepDto("Q", "Ass", 1, Map.of()), new StepDto("P", "Rep [1]", 1, Map.of()));
        assertEquals(3, transformer.from(dto).getSteps().size());
    }

    @Test
    void replayRejectsAStepThatDoesNotFollow() {
        var dto = proof(new StepDto("P", "Ass", 0, Map.of()), new StepDto("P", "->E [1, 1]", 0, Map.of()));
        var exception = assertThrows(InvalidProofException.class, () -> transformer.from(dto));
        assertTrue(exception.getMessage().startsWith("Line 2 "), exception.getMessage());
    }

    @Test
    void replayRejectsALineThatDoesNotExist() {
        var dto = proof(new StepDto("P", "Ass", 0, Map.of()), new StepDto("P", "Rep [7]", 0, Map.of()));
        var exception = assertThrows(InvalidProofException.class, () -> transformer.from(dto));
        assertTrue(exception.getMessage().startsWith("Line 2 "), exception.getMessage());
    }

    @Test
    void replayRejectsGarbage() {
        var badExpression = proof(new StepDto("P Q", "Ass", 0, Map.of()));
        var badRule = proof(new StepDto("P", "Ass", 0, Map.of()), new StepDto("P", "Nope", 0, Map.of()));
        var badGoal = new ProofDto(List.of(), "classical", "P Q");
        assertThrows(InvalidProofException.class, () -> transformer.from(badExpression));
        assertThrows(InvalidProofException.class, () -> transformer.from(badRule));
        assertThrows(InvalidProofException.class, () -> transformer.from(badGoal));
    }

    @Test
    void omittedParametersAreAccepted() {
        var applier = new LogicalApplyAction<>(transformer);
        var dto = new ProofDto(List.of(new StepDto("P", "Ass", 0, null)), "classical", "P");
        var result = applier.perform(new ActionDto("NOTI", null, null), dto);
        assertFalse(result.applied());
        assertEquals("Rule NOTI cannot be applied", result.message());
    }

    @Test
    void rejectedActionsSayWhy() {
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
    void unbuildableActionsAreInvalid() {
        var applier = new LogicalApplyAction<>(transformer);
        var dto = proof(new StepDto("P", "Ass", 0, Map.of()));
        var unknown = new ActionDto("NOPE", List.of(1), Map.of());
        var missingSource = new ActionDto("ANDI", List.of(1), Map.of());
        var badExpression = new ActionDto("ASSUME", List.of(), Map.of("expression", "P Q"));
        assertThrows(InvalidActionException.class, () -> applier.perform(unknown, dto));
        assertThrows(InvalidActionException.class, () -> applier.perform(missingSource, dto));
        assertThrows(InvalidActionException.class, () -> applier.perform(badExpression, dto));
    }
}
