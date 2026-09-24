package com.dan323.uses.modal.until.test;

import com.dan323.expressions.modal.Until;
import com.dan323.expressions.modal.VariableModal;
import com.dan323.model.ActionCategory;
import com.dan323.model.ActionDescriptorDto;
import com.dan323.model.ActionDto;
import com.dan323.model.ParamKind;
import com.dan323.model.ProofDto;
import com.dan323.model.StepDto;
import com.dan323.uses.InvalidActionException;
import com.dan323.uses.InvalidProofException;
import com.dan323.uses.LogicalApplyAction;
import com.dan323.uses.LogicalSolver;
import com.dan323.uses.modal.ModalConfiguration;
import com.dan323.uses.modal.ModalProofParser;
import com.dan323.uses.modal.ModalProofTransformer;
import com.dan323.uses.modal.until.AvailableUntilAction;
import com.dan323.uses.modal.until.ModalUntilConfiguration;
import com.dan323.uses.modal.until.ModalUntilProofParser;
import com.dan323.uses.modal.until.ModalUntilProofTransformer;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ModalUntilUseTest {

    private static final String GAP = " ".repeat(11);
    private static final Map<String, String> S0 = Map.of("state", "s0");

    private static final String NOT_YET = "s0: p U q" + GAP + "Ass\n"
            + "s0: - q" + GAP + "Ass\n"
            + "s0: p" + GAP + "UE [1, 2]\n";

    private final ModalUntilConfiguration configuration = new ModalUntilConfiguration();

    private static List<ActionDescriptorDto> actions() {
        return new ModalUntilConfiguration().modalUntilActions().perform();
    }

    @Test
    void theBeansServeTheModalUntilLogic() {
        assertEquals("modal-until", configuration.modalUntilActions().getLogicName());
        assertEquals("modal-until", configuration.modalUntilTransformer().logic());
        assertEquals("modal-until", configuration.modalUntilProofParser().logic());
        assertEquals("modal-until", configuration.modalUntilExercises().logic());
        assertTrue(configuration.modalUntilTransformer().hasSolver());
    }

    @Test
    void theActionsAreTheModalOnesPlusTheUntilRules() {
        var modal = new ModalConfiguration().modalActions().perform();
        var actions = actions();

        assertEquals(modal.size() + AvailableUntilAction.values().length, actions.size());
        assertEquals(modal, actions.subList(0, modal.size()));
        assertEquals(List.of("UI1", "UI2", "UE1", "UE2"), actions.subList(modal.size(), actions.size()).stream().map(ActionDescriptorDto::name).toList());
        var names = actions.stream().map(ActionDescriptorDto::name).toList();
        assertEquals(names.size(), new HashSet<>(names).size(), "names are unique");
        assertEquals(names.size(), actions.stream().map(ActionDescriptorDto::label).distinct().count(), "labels are unique");
        var bean = configuration.modalUntilActions();
        assertSame(bean.perform(), bean.perform(), "built once");
    }

    @Test
    void theUntilRulesAreDescribed() {
        var byName = actions().stream().collect(Collectors.toMap(ActionDescriptorDto::name, descriptor -> descriptor));

        for (var action : AvailableUntilAction.values()) {
            var descriptor = action.descriptor();
            var name = descriptor.name();
            assertFalse(descriptor.label().isBlank(), name);
            assertFalse(descriptor.description().isBlank(), name);
            assertFalse(descriptor.description().endsWith("."), name + ": the client adds the full stop");
            assertEquals(descriptor.params().size(), descriptor.paramLabels().size(), name);
            assertTrue(descriptor.paramLabels().stream().noneMatch(String::isBlank), name);
        }
        assertEquals(List.of(ParamKind.INT, ParamKind.EXPRESSION), byName.get("UI1").params());
        assertEquals(List.of("Line with B", "Left side (A)"), byName.get("UI1").paramLabels());
        assertEquals(List.of(ParamKind.INT, ParamKind.INT), byName.get("UI2").params());
        assertEquals(List.of(ParamKind.INT), byName.get("UE1").params());
        assertEquals(List.of(ParamKind.INT, ParamKind.INT), byName.get("UE2").params());
        // The symbol is the rule text of the step (the frontend's renderRule leaves UI and UE as they are)
        assertEquals("UI", byName.get("UI1").symbol());
        assertEquals("UI", byName.get("UI2").symbol());
        assertEquals("UE", byName.get("UE1").symbol());
        assertEquals("UE", byName.get("UE2").symbol());
        assertEquals(ActionCategory.INTRODUCTION, byName.get("UI2").category());
        assertEquals(ActionCategory.ELIMINATION, byName.get("UE1").category());
    }

    @Test
    void everyDescriptorNameBuildsAnAction() {
        var transformer = new ModalUntilProofTransformer();
        for (var descriptor : actions()) {
            var lines = Collections.nCopies((int) descriptor.params().stream().filter(ParamKind.INT::equals).count(), 1);
            var extra = Map.of("expression", "P", "state", "s0");

            assertNotNull(transformer.from(new ActionDto(descriptor.name(), lines, extra)), descriptor.name());
        }
    }

    @Test
    void untilOnlyExistsInModalUntil() {
        var modalActions = new ModalConfiguration().modalActions().perform().stream().map(ActionDescriptorDto::name).toList();
        assertFalse(modalActions.contains("UI1"));

        var apply = new LogicalApplyAction<>(new ModalProofTransformer());
        var proof = new ProofDto(List.of(new StepDto("q", "Ass", 0, S0)), "modal", "p");
        var untilInModal = new ActionDto("UI1", List.of(1), Map.of("expression", "p"));
        assertThrows(InvalidActionException.class, () -> apply.perform(untilInModal, proof));

        var untilStep = new ProofDto(List.of(new StepDto("q", "Ass", 0, S0), new StepDto("p U q", "UI [1]", 0, S0)), "modal", "p U q");
        assertThrows(InvalidProofException.class, () -> new ModalProofTransformer().from(untilStep));
        assertThrows(InvalidProofException.class, () -> new ModalProofParser().parseProof(NOT_YET));
    }

    @Test
    void anUntilActionIsApplied() {
        var apply = new LogicalApplyAction<>(new ModalUntilProofTransformer());
        var proof = new ProofDto(List.of(new StepDto("q", "Ass", 0, S0)), "modal-until", "p U q");

        var result = apply.perform(new ActionDto("UI1", List.of(1), Map.of("expression", "p")), proof);

        assertTrue(result.applied());
        assertTrue(result.done());
        var last = result.proof().steps().getLast();
        assertEquals(new StepDto("p U q", "UI [1]", 0, S0), last);
        assertEquals("modal-until", result.proof().logic());

        var wrongLines = apply.perform(new ActionDto("UE2", List.of(1, 1), Map.of()), proof);
        assertFalse(wrongLines.applied());
        assertEquals("Rule UE2 cannot be applied to lines [1, 1]", wrongLines.message());
    }

    @Test
    void aProofWithUntilStepsIsReplayed() {
        var transformer = new ModalUntilProofTransformer();
        var dto = new ProofDto(List.of(new StepDto("p U q", "Ass", 0, S0), new StepDto("- q", "Ass", 0, S0),
                new StepDto("p", "UE [1, 2]", 0, S0), new StepDto("<> q", "UE [1]", 0, S0)), "modal-until", "p");

        var proof = transformer.from(dto);

        assertEquals(4, proof.getSteps().size());
        assertTrue(proof.isDone());
        assertEquals(new Until(new VariableModal("p"), new VariableModal("q")), proof.getAssms().getFirst());
        assertEquals(dto, transformer.fromProof(proof));

        var wrong = new ProofDto(List.of(new StepDto("p U q", "Ass", 0, S0), new StepDto("p", "UE [1, 1]", 0, S0)), "modal-until", "p");
        var exception = assertThrows(InvalidProofException.class, () -> transformer.from(wrong));
        assertTrue(exception.getMessage().startsWith("Line 2 "), exception.getMessage());
    }

    @Test
    void aProofTextWithUntilIsParsed() {
        var proof = new ModalUntilProofParser().parseProof(NOT_YET);

        assertTrue(proof.isDone());
        assertEquals(NOT_YET, proof.toString());
        var exception = assertThrows(InvalidProofException.class, () -> new ModalUntilProofParser().parseProof("s0: p" + GAP + "UX [1]"));
        assertTrue(exception.getMessage().startsWith("Line 1 "), exception.getMessage());
    }

    @Test
    void theSolverIsTheModalOne() {
        var solver = new LogicalSolver<>(new ModalUntilProofTransformer(), Duration.ofSeconds(30));

        var solved = solver.perform(new ProofDto(List.of(new StepDto("[] (p U q)", "Ass", 0, S0)), "modal-until", "p U q"));
        assertTrue(solved.isDone());
        assertEquals("modal-until", solved.logic());

        var needsUntilRules = solver.perform(new ProofDto(List.of(new StepDto("q", "Ass", 0, S0)), "modal-until", "p U q"));
        assertFalse(needsUntilRules.isDone());
    }
}
