package com.dan323.uses.intuitionistic.test;

import com.dan323.classical.proof.AvailableAction;
import com.dan323.model.ActionDescriptorDto;
import com.dan323.model.ActionDto;
import com.dan323.model.ProofDto;
import com.dan323.model.StepDto;
import com.dan323.uses.InvalidActionException;
import com.dan323.uses.InvalidProofException;
import com.dan323.uses.LogicalApplyAction;
import com.dan323.uses.classical.ClassicGetActions;
import com.dan323.uses.classical.ClassicalProofTransformer;
import com.dan323.uses.classical.ParseClassicalProof;
import com.dan323.uses.intuitionistic.IntuitionisticConfiguration;
import com.dan323.uses.intuitionistic.IntuitionisticProofTransformer;
import com.dan323.uses.intuitionistic.ParseIntuitionisticProof;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IntuitionisticUseTest {

    private static final String DOUBLE_NEGATION_ELIMINATION = """
            - (- p)           Ass
            p           -E [1]
            """;

    private final IntuitionisticConfiguration configuration = new IntuitionisticConfiguration();

    private static ProofDto doubleNegationElimination(String logic) {
        return new ProofDto(List.of(new StepDto("- (- p)", "Ass", 0, Map.of()), new StepDto("p", "-E [1]", 0, Map.of())), logic, "p");
    }

    @Test
    void theActionsAreTheClassicalOnesWithoutDoubleNegationElimination() {
        var actions = configuration.intuitionisticActions();
        assertEquals("intuitionistic", actions.getLogicName());
        var names = actions.perform().stream().map(ActionDescriptorDto::name).toList();
        assertFalse(names.contains(AvailableAction.NOTE.name()));
        var classical = new ClassicGetActions().perform().stream()
                .filter(action -> !action.name().equals(AvailableAction.NOTE.name()))
                .toList();
        assertEquals(classical, actions.perform());
    }

    @Test
    void doubleNegationEliminationAsAnActionIsInvalid() {
        var applier = new LogicalApplyAction<>(new IntuitionisticProofTransformer());
        var proof = new ProofDto(List.of(new StepDto("- (- p)", "Ass", 0, Map.of())), "intuitionistic", "p");
        var note = new ActionDto("NOTE", List.of(1), Map.of());
        var exception = assertThrows(InvalidActionException.class, () -> applier.perform(note, proof));
        assertEquals("Rule NOTE is not a rule of intuitionistic logic", exception.getMessage());
        // Classically the same action applies
        var classical = new LogicalApplyAction<>(new ClassicalProofTransformer()).perform(note, proof);
        assertTrue(classical.applied());
        assertTrue(classical.done());
    }

    @Test
    void theOtherActionsApply() {
        var applier = new LogicalApplyAction<>(new IntuitionisticProofTransformer());
        var proof = new ProofDto(List.of(new StepDto("p & q", "Ass", 0, Map.of())), "intuitionistic", "q");
        var result = applier.perform(new ActionDto("ANDE2", List.of(1), Map.of()), proof);
        assertTrue(result.applied());
        assertTrue(result.done());
        assertEquals("intuitionistic", result.proof().logic());
        assertEquals(new StepDto("q", "&E [1]", 0, Map.of()), result.proof().steps().getLast());
        var unknown = new ActionDto("NOPE", List.of(1), Map.of());
        assertThrows(InvalidActionException.class, () -> applier.perform(unknown, proof));
    }

    @Test
    void aClassicalOnlyProofDoesNotReplay() {
        assertTrue(new ClassicalProofTransformer().from(doubleNegationElimination("classical")).isDone());
        var transformer = new IntuitionisticProofTransformer();
        var dto = doubleNegationElimination("intuitionistic");
        var exception = assertThrows(InvalidProofException.class, () -> transformer.from(dto));
        assertEquals("Line 2 is not valid: '-E [1]' is not a rule of intuitionistic logic", exception.getMessage());
    }

    @Test
    void aClassicalOnlyProofDoesNotParse() {
        assertTrue(new ParseClassicalProof().parseProof(DOUBLE_NEGATION_ELIMINATION).isDone());
        var parser = new ParseIntuitionisticProof();
        var exception = assertThrows(InvalidProofException.class, () -> parser.parseProof(DOUBLE_NEGATION_ELIMINATION));
        assertEquals("Line 2 is not valid: '-E [1]' is not a rule of intuitionistic logic", exception.getMessage());
    }

    @Test
    void anIntuitionisticProofRoundTrips() {
        var parser = configuration.intuitionisticProofParser();
        assertEquals("intuitionistic", parser.logic());
        var proof = new ParseIntuitionisticProof().parseProof("""
                p           Ass
                   - p           Ass
                   FALSE           FI [1, 2]
                - (- p)           -I [2-3]
                """);
        assertTrue(proof.isDone());
        var transformer = new IntuitionisticProofTransformer();
        var dto = transformer.fromProof(proof);
        assertEquals("intuitionistic", dto.logic());
        assertEquals("- (- p)", dto.goal());
        assertEquals(4, transformer.from(dto).getSteps().size());
    }

    @Test
    void thereIsNoSolver() {
        var transformer = configuration.intuitionisticTransformer();
        assertEquals("intuitionistic", transformer.logic());
        assertFalse(transformer.hasSolver());
        assertTrue(new ClassicalProofTransformer().hasSolver());
    }

    @Test
    void doubleNegationEliminationByItsRuleNameIsInvalid() {
        var applier = new LogicalApplyAction<>(new IntuitionisticProofTransformer());
        var proof = new ProofDto(List.of(new StepDto("- (- p)", "Ass", 0, Map.of())), "intuitionistic", "p");
        var note = new ActionDto("-E", List.of(1), Map.of());
        var exception = assertThrows(InvalidActionException.class, () -> applier.perform(note, proof));
        assertEquals("Rule -E is not a rule of intuitionistic logic", exception.getMessage());
    }
}
