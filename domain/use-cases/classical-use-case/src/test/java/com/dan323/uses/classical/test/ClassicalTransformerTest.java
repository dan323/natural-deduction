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

    // The replay takes the subproof structure from the rules, not from the levels a client sends (they only tell the
    // premises apart), so a rejected action answers with the proof as the domain sees it, not the request's levels.
    @Test
    void rejectedActionsAnswerWithTheReplayedLevels() {
        var applier = new LogicalApplyAction<>(transformer);
        var misIndented = proof(new StepDto("P", "Ass", 0, Map.of()), new StepDto("Q", "Ass", 1, Map.of()),
                new StepDto("P", "Rep [1]", 0, Map.of()), new StepDto("Q -> P", "->I [2-3]", 1, Map.of()));
        var result = applier.perform(new ActionDto("COPY", List.of(5), Map.of()), misIndented);
        assertFalse(result.applied());
        assertTrue(result.done());
        assertEquals(List.of(0, 1, 1, 0), result.proof().steps().stream().map(StepDto::assmsLevel).toList());
        assertEquals(List.of("Ass", "Ass", "Rep [1]", "->I [2-3]"), result.proof().steps().stream().map(StepDto::rule).toList());
    }

    @Test
    void resultsCarryTheDomainsDoneVerdict() {
        var applier = new LogicalApplyAction<>(transformer);
        // The goal P is a top level step that is not the last one: the domain's Proof.isDone() is true.
        var goalNotLast = new ProofDto(List.of(new StepDto("P", "Ass", 0, Map.of()), new StepDto("Q", "Ass", 0, Map.of())), "classical", "P");
        var applied = applier.perform(new ActionDto("COPY", List.of(2), Map.of()), goalNotLast);
        assertTrue(applied.applied());
        assertTrue(applied.done());
        assertTrue(applier.perform(new ActionDto("COPY", List.of(9), Map.of()), goalNotLast).done());
        var notYet = new ProofDto(List.of(new StepDto("Q", "Ass", 0, Map.of())), "classical", "P");
        assertFalse(applier.perform(new ActionDto("COPY", List.of(1), Map.of()), notYet).done());
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

    @Test
    void ruleNamesAreAccepted() {
        var applier = new LogicalApplyAction<>(transformer);
        var dto = proof(new StepDto("P", "Ass", 0, Map.of()));
        var result = applier.perform(new ActionDto("Rep", List.of(1), Map.of()), dto);
        assertTrue(result.applied());
        assertEquals("Rep [1]", result.proof().steps().get(1).rule());
        var missingExpression = new ActionDto("Ass", List.of(), Map.of());
        var exception = assertThrows(InvalidActionException.class, () -> transformer.from(missingExpression));
        assertEquals("ASSUME needs an expression", exception.getMessage());
    }
}
