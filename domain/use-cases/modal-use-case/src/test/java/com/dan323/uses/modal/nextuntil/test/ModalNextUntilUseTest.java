package com.dan323.uses.modal.nextuntil.test;

import com.dan323.model.ActionDescriptorDto;
import com.dan323.model.ActionDto;
import com.dan323.model.ParamKind;
import com.dan323.model.ProofDto;
import com.dan323.model.StepDto;
import com.dan323.uses.InvalidActionException;
import com.dan323.uses.InvalidProofException;
import com.dan323.uses.LogicalApplyAction;
import com.dan323.uses.modal.ModalConfiguration;
import com.dan323.uses.modal.ModalProofParser;
import com.dan323.uses.modal.ModalProofTransformer;
import com.dan323.uses.modal.nextuntil.AvailableNextUntilAction;
import com.dan323.uses.modal.nextuntil.ModalNextUntilConfiguration;
import com.dan323.uses.modal.nextuntil.ModalNextUntilProofParser;
import com.dan323.uses.modal.nextuntil.ModalNextUntilProofTransformer;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ModalNextUntilUseTest {

    private static final String LOGIC = "modal-next-until";
    private final ModalNextUntilProofTransformer transformer = new ModalNextUntilProofTransformer();

    private static StepDto step(String expression, String rule, int level, String state) {
        return new StepDto(expression, rule, level, state == null ? Map.of() : Map.of("state", state));
    }

    private static ActionDto action(String name, List<Integer> sources, Map<String, String> extra) {
        return new ActionDto(name, sources, extra);
    }

    @Test
    void theBeansAreKeyedByTheLogicName() {
        var configuration = new ModalNextUntilConfiguration();
        assertEquals(LOGIC, configuration.modalNextUntilActions().getLogicName());
        assertEquals(LOGIC, configuration.modalNextUntilTransformer().logic());
        assertEquals(LOGIC, configuration.modalNextUntilProofParser().logic());
        assertFalse(configuration.modalNextUntilTransformer().hasSolver());
        assertTrue(new ModalConfiguration().modalTransformer().hasSolver(), "modal keeps its solver");
    }

    @Test
    void theActionsAreTheModalOnesThenNextAndUntil() {
        var actions = new ModalNextUntilConfiguration().modalNextUntilActions().perform();
        var modal = new ModalConfiguration().modalActions().perform();

        assertEquals(modal, actions.subList(0, modal.size()));
        assertEquals(20, modal.size(), "modal keeps its actions");
        assertEquals(modal.size() + AvailableNextUntilAction.values().length, actions.size());
        var names = actions.stream().map(ActionDescriptorDto::name).toList();
        assertEquals(names.size(), new HashSet<>(names).size());
        assertTrue(names.containsAll(List.of("XI", "XE", "Succ", "UI1", "UI2", "UE", "U<>", "Ind")));

        var byName = actions.stream().collect(Collectors.toMap(ActionDescriptorDto::name, ActionDescriptorDto::params));
        assertEquals(List.of(ParamKind.INT, ParamKind.EXPRESSION), byName.get("UI1"));
        assertEquals(List.of(ParamKind.INT, ParamKind.INT), byName.get("UI2"));
        assertEquals(List.of(ParamKind.INT, ParamKind.INT), byName.get("Ind"));
        assertEquals(List.of(ParamKind.INT), byName.get("XI"));
        actions.forEach(descriptor -> assertEquals(descriptor.params().size(), descriptor.paramLabels().size(), descriptor.name()));
    }

    @Test
    void everyDescriptorNameBuildsAnAction() {
        for (var descriptor : new ModalNextUntilConfiguration().modalNextUntilActions().perform()) {
            var lines = Collections.nCopies((int) descriptor.params().stream().filter(ParamKind.INT::equals).count(), 1);
            var extra = Map.of("expression", "P", "state", "s0");

            assertNotNull(transformer.from(action(descriptor.name(), lines, extra)), descriptor.name());
        }
    }

    @Test
    void modalDoesNotKnowTheNewRules() {
        var modal = new ModalProofTransformer();
        for (var next : AvailableNextUntilAction.values()) {
            var name = next.descriptor().name();
            var action = action(name, List.of(1, 1), Map.of("expression", "P"));
            assertThrows(RuntimeException.class, () -> modal.from(action), name);
        }
        var proof = new ProofDto(List.of(step("X p", "Ass", 0, "s0")), "modal", "p");
        var apply = new LogicalApplyAction<>(modal);
        var nextE = action("XE", List.of(1), Map.of());
        assertThrows(RuntimeException.class, () -> apply.perform(nextE, proof));
    }

    @Test
    void aSmallProofIsReplayedAndApplied() {
        var proof = new ProofDto(List.of(
                step("p", "Ass", 0, "s0"),
                step("X q", "Ass", 0, "s0"),
                step("q", "XE [2]", 0, "s0 + 1"),
                step("p U q", "UI [3]", 0, "s0+1+0")), LOGIC, "p U q");

        var result = new LogicalApplyAction<>(transformer).perform(action("XI", List.of(4), Map.of()), proof);

        assertTrue(result.applied());
        var steps = result.proof().steps();
        assertEquals(LOGIC, result.proof().logic());
        assertEquals("s0+1", steps.get(2).extraParameters().get("state"));
        assertEquals("X (p U q)", steps.get(4).expression());
        assertEquals("XI [4]", steps.get(4).rule());
        assertEquals("s0", steps.get(4).extraParameters().get("state"));

        var done = new LogicalApplyAction<>(transformer).perform(action("UI2", List.of(1, 5), Map.of()), result.proof());
        assertTrue(done.applied());
        assertTrue(done.done());
        assertEquals("UI [1, 5]", done.proof().steps().getLast().rule());
    }

    @Test
    void aRuleInTheWrongStateIsRejected() {
        var proof = new ProofDto(List.of(step("p", "Ass", 0, "s0")), LOGIC, "X p");

        var result = new LogicalApplyAction<>(transformer).perform(action("XI", List.of(1), Map.of()), proof);

        assertFalse(result.applied(), "s0 is not the successor of a state");
        // As with assumption levels, the replay puts a derived line in the state its rule gives, whatever was sent.
        var wrongState = new ProofDto(List.of(step("X p", "Ass", 0, "s0"), step("p", "XE [1]", 0, "s1")), LOGIC, "p");
        assertEquals("s0+1", transformer.from(wrongState).getSteps().getLast().getState());
    }

    @Test
    void aStateThatIsNotATermIsRejected() {
        var proof = new ProofDto(List.of(step("p", "Ass", 0, "s0")), LOGIC, "p");
        var apply = new LogicalApplyAction<>(transformer);

        assertThrows(InvalidActionException.class, () -> apply.perform(action("Ass", List.of(), Map.of("expression", "q", "state", "s0 - 1")), proof));
        assertThrows(InvalidProofException.class, () -> transformer.from(new ProofDto(List.of(step("p", "Ass", 0, "s0+")), LOGIC, "p")));
        assertThrows(InvalidProofException.class, () -> transformer.from(new ProofDto(List.of(step("p", "Ass", 0, "s0"),
                step("q", "Ass", 1, "1")), LOGIC, "p")));
    }

    @Test
    void aStateIsKeptNormalized() {
        var proof = new ProofDto(List.of(step("p", "Ass", 0, "s0 + 0")), LOGIC, "p");

        var result = new LogicalApplyAction<>(transformer).perform(action("Ass", List.of(), Map.of("expression", "q", "state", "s1 + 1 + 1")), proof);

        assertEquals("s0", result.proof().steps().get(0).extraParameters().get("state"));
        assertEquals("s1+2", result.proof().steps().get(1).extraParameters().get("state"));
    }

    @Test
    void aProofTextRoundTrips() {
        var text = """
                s0: X p           Ass
                s0 <= s0+1           Succ [1]
                s0+1: p           XE [1]
                s0+1: p | q           |I [3]
                s0: X (p | q)           XI [4]
                """;

        var proof = new ModalNextUntilProofParser().parseProof(text);

        assertTrue(proof.isDone());
        assertEquals(text, proof.getSteps().stream().map(Object::toString).collect(Collectors.joining("\n", "", "\n")));
        assertThrows(InvalidProofException.class, () -> new ModalProofParser().parseProof(text), "modal has no X");
    }

    @Test
    void aProofTextWithAWrongStateIsRejected() {
        var parser = new ModalNextUntilProofParser();
        var replayed = parser.parseProof("""
                s0: X p           Ass
                s1: p           XE [1]
                """);
        assertEquals("s0+1", replayed.getSteps().getLast().getState(), "the rule gives the state");
        assertFalse(replayed.isDone(), "p is in s0+1, not in s0");
        assertThrows(InvalidProofException.class, () -> parser.parseProof("""
                s0: X p           Ass
                s0+: p           XE [1]
                """));
    }
}
