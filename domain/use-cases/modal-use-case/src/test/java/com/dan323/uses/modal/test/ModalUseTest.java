package com.dan323.uses.modal.test;

import com.dan323.model.ActionCategory;
import com.dan323.model.ActionDescriptorDto;
import com.dan323.model.ActionDto;
import com.dan323.model.ParamKind;
import com.dan323.model.ProofDto;
import com.dan323.model.StepDto;
import com.dan323.uses.InvalidActionException;
import com.dan323.uses.LogicalApplyAction;
import com.dan323.uses.LogicalGetActions;
import com.dan323.uses.LogicalSolver;
import com.dan323.uses.modal.AvailableModalAction;
import com.dan323.uses.modal.ModalConfiguration;
import com.dan323.uses.modal.ModalProofTransformer;
import com.dan323.uses.modal.mock.ModalProof;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModalUseTest {

    @Test
    public void modalSolver() {
        var solver = new LogicalSolver<>(new ModalProofTransformer(), Duration.ofSeconds(30));
        var e = solver.perform(new ModalProofTransformer().fromProof(ModalProof.naturalDeductionNoAssms()));
        assertTrue(e.isDone());
    }

    @Test
    public void modalActions() {
        LogicalGetActions actions = (new ModalConfiguration()).modalActions();
        assertEquals(20, actions.perform().size());
    }

    @Test
    void actionNamesAreUnique() {
        var names = new ModalConfiguration().modalActions().perform().stream().map(ActionDescriptorDto::name).toList();

        assertEquals(names.size(), new HashSet<>(names).size());
    }

    @Test
    void descriptorsHaveTheParametersOfTheActions() {
        var byName = new ModalConfiguration().modalActions().perform().stream()
                .collect(Collectors.toMap(ActionDescriptorDto::name, ActionDescriptorDto::params));

        assertEquals(List.of(ParamKind.EXPRESSION, ParamKind.STATE), byName.get("Ass"));
        assertEquals(List.of(ParamKind.INT, ParamKind.EXPRESSION, ParamKind.STATE), byName.get("FE"));
        assertEquals(List.of(ParamKind.INT, ParamKind.EXPRESSION), byName.get("|I1"));
        assertEquals(List.of(ParamKind.INT, ParamKind.INT, ParamKind.INT), byName.get("|E"));
        assertEquals(List.of(ParamKind.INT, ParamKind.INT), byName.get("[]E"));
        assertEquals(List.of(ParamKind.INT), byName.get("Refl"));
        assertEquals(List.of(), byName.get("[]I"));
        assertEquals(List.of(), byName.get("->I"));
    }

    @Test
    void everyDescriptorNameBuildsAnAction() {
        var transformer = new ModalProofTransformer();
        for (var action : AvailableModalAction.values()) {
            var descriptor = action.descriptor();
            var lines = Collections.nCopies((int) descriptor.params().stream().filter(ParamKind.INT::equals).count(), 1);
            var extra = Map.of("expression", "P", "state", "s0");

            assertNotNull(transformer.from(new ActionDto(descriptor.name(), lines, extra)), descriptor.name());
        }
    }

    // The rule text each modal step carries (what the backend writes in StepDto.rule, see ParseModalAction.parseWithReason)
    // and its symbol, which is that text as the frontend's renderRule shows it.
    private static final Map<String, List<String>> RULE_TEXT_AND_SYMBOL = Map.ofEntries(
            Map.entry("Ass", List.of("Ass", "Ass")), Map.entry("|I1", List.of("|I", "∨I")),
            Map.entry("|I2", List.of("|I", "∨I")), Map.entry("|E", List.of("|E", "∨E")),
            Map.entry("&I", List.of("&I", "∧I")), Map.entry("&E1", List.of("&E", "∧E")),
            Map.entry("&E2", List.of("&E", "∧E")), Map.entry("Rep", List.of("Rep", "Rep")),
            Map.entry("-E", List.of("-E", "¬E")), Map.entry("-I", List.of("-I", "¬I")),
            Map.entry("->I", List.of("->I", "→I")), Map.entry("->E", List.of("->E", "→E")),
            Map.entry("FE", List.of("FE", "⊥E")), Map.entry("FI", List.of("FI", "⊥I")),
            Map.entry("[]I", List.of("[]I", "□I")), Map.entry("[]E", List.of("[]E", "□E")),
            Map.entry("<>I", List.of("<>I", "◇I")), Map.entry("<>E", List.of("<>E", "◇E")),
            Map.entry("Refl", List.of("Refl", "Refl")), Map.entry("Trans", List.of("Trans", "Trans")));

    // The replacements of the frontend's renderRule (service/utils.ts), in the same order.
    private static String renderRule(String rule) {
        return rule.replace("->", "→")
                .replaceAll("(?<!\\d)-(?!\\d)", "¬")
                .replace("&", "∧")
                .replace("|", "∨")
                .replace("[]", "□")
                .replace("<>", "◇")
                .replaceFirst("^F(?=[EI])", "⊥");
    }

    private static Map<String, ActionDescriptorDto> descriptorsByName() {
        return new ModalConfiguration().modalActions().perform().stream()
                .collect(Collectors.toMap(ActionDescriptorDto::name, descriptor -> descriptor));
    }

    @Test
    void everyDescriptorIsPresentedToTheUser() {
        var descriptors = new ModalConfiguration().modalActions().perform();

        for (var descriptor : descriptors) {
            var name = descriptor.name();
            assertFalse(descriptor.label() == null || descriptor.label().isBlank(), name);
            assertFalse(descriptor.symbol() == null || descriptor.symbol().isBlank(), name);
            assertNotNull(descriptor.category(), name);
            assertFalse(descriptor.description() == null || descriptor.description().isBlank(), name);
            assertFalse(descriptor.description().endsWith("."), name + ": the client adds the full stop");
            assertEquals(descriptor.params().size(), descriptor.paramLabels().size(), name);
            assertTrue(descriptor.paramLabels().stream().noneMatch(String::isBlank), name);
        }
        assertEquals(descriptors.size(), descriptors.stream().map(ActionDescriptorDto::label).distinct().count(), "labels are unique");
    }

    @Test
    void descriptorSymbolsAreTheProofRuleTexts() {
        var descriptors = descriptorsByName();

        assertEquals(RULE_TEXT_AND_SYMBOL.keySet(), descriptors.keySet());
        RULE_TEXT_AND_SYMBOL.forEach((name, ruleAndSymbol) -> {
            assertEquals(ruleAndSymbol.get(1), renderRule(ruleAndSymbol.get(0)), name);
            assertEquals(ruleAndSymbol.get(1), descriptors.get(name).symbol(), name);
        });
    }

    @Test
    void descriptorsGroupIntroductionsAndEliminations() {
        var descriptors = descriptorsByName();

        for (var name : List.of("Ass", "Rep", "Refl", "Trans")) {
            assertEquals(ActionCategory.OTHER, descriptors.get(name).category(), name);
        }
        for (var name : List.of("|I1", "|I2", "&I", "-I", "->I", "FI", "[]I", "<>I")) {
            assertEquals(ActionCategory.INTRODUCTION, descriptors.get(name).category(), name);
        }
        for (var name : List.of("|E", "&E1", "&E2", "-E", "->E", "FE", "[]E", "<>E")) {
            assertEquals(ActionCategory.ELIMINATION, descriptors.get(name).category(), name);
        }
    }

    @Test
    void descriptorLabelsFollowTheOrderTheActionsExpectTheirInputs() {
        var descriptors = descriptorsByName();

        var boxI = descriptors.get("[]I");
        assertEquals("Box introduction", boxI.label());
        assertEquals(List.of(), boxI.paramLabels());
        assertEquals("Diamond elimination", descriptors.get("<>E").label());
        assertEquals(List.of("Assumption (A or s0 <= s1)", "State of A (e.g. s1)"), descriptors.get("Ass").paramLabels());
        assertEquals(List.of("Falsum (⊥)", "Formula to derive (A)", "State of A (e.g. s1)"), descriptors.get("FE").paramLabels());
        assertEquals(List.of("Necessity (□A in state s)", "Relation (s <= t)"), descriptors.get("[]E").paramLabels());
        assertEquals(List.of("Line with A (in state t)", "Relation (s <= t)"), descriptors.get("<>I").paramLabels());
        assertEquals(List.of("Relation (s <= t)", "Relation (t <= u)"), descriptors.get("Trans").paramLabels());
        assertEquals(List.of("Implication (A → B)", "Antecedent (A)"), descriptors.get("->E").paramLabels());
    }

    @Test
    void theActionListIsBuiltOnce() {
        var actions = new ModalConfiguration().modalActions();

        assertSame(actions.perform(), actions.perform());
    }

    @Test
    void anActionThatNeedsAnExpressionRejectsABlankOne() {
        var transformer = new ModalProofTransformer();
        for (var name : List.of("Ass", "|I1", "|I2", "FE")) {
            for (var extra : List.of(Map.of("state", "s0"), Map.of("expression", "", "state", "s0"), Map.of("expression", "  ", "state", "s0"))) {
                var action = new ActionDto(name, List.of(1), extra);

                var exception = assertThrows(InvalidActionException.class, () -> transformer.from(action));
                assertEquals(name + " needs an expression", exception.getMessage());
            }
        }
    }

    @Test
    void applyingAModalAssumeWithoutParametersIsAnInvalidAction() {
        var apply = new LogicalApplyAction<>(new ModalProofTransformer());
        var proof = new ProofDto(List.of(new StepDto("P", "Ass", 0, Map.of("state", "s0"))), "modal", "P");

        var noParameters = new ActionDto("Ass", List.of(), null);
        assertEquals("Ass needs an expression", assertThrows(InvalidActionException.class, () -> apply.perform(noParameters, proof)).getMessage());
        var blank = new ActionDto("Ass", List.of(), Map.of("expression", "", "state", "s0"));
        assertEquals("Ass needs an expression", assertThrows(InvalidActionException.class, () -> apply.perform(blank, proof)).getMessage());
    }

    @Test
    void anUnparsableModalExpressionNeverLeaksNull() {
        var apply = new LogicalApplyAction<>(new ModalProofTransformer());
        var proof = new ProofDto(List.of(new StepDto("P", "Ass", 0, Map.of("state", "s0"))), "modal", "P");

        for (var expression : List.of("P ->", "(P", "->")) {
            var action = new ActionDto("Ass", List.of(), Map.of("expression", expression, "state", "s0"));

            var exception = assertThrows(InvalidActionException.class, () -> apply.perform(action, proof));
            assertFalse(exception.getMessage().contains("null"), exception.getMessage());
        }
    }
}
