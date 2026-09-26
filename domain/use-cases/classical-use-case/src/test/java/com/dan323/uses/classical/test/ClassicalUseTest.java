package com.dan323.uses.classical.test;

import com.dan323.classical.proof.AvailableAction;
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
import com.dan323.uses.classical.ClassicalConfiguration;
import com.dan323.uses.classical.ClassicalProofTransformer;
import com.dan323.uses.classical.mock.ClassicalProof;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class ClassicalUseTest {

    @Test
    void classicSolver() {
        var solver = new LogicalSolver<>(new ClassicalProofTransformer(), Duration.ofSeconds(30));
        var proof = solver.perform(new ClassicalProofTransformer().fromProof(ClassicalProof.naturalDeductionNoAssms()));
        assertTrue(proof.isDone());
        proof = solver.perform(new ClassicalProofTransformer().fromProof(ClassicalProof.naturalDeductionWithAssms()));
        assertTrue(proof.isDone());
        proof = solver.perform(new ClassicalProofTransformer().fromProof(ClassicalProof.naturalDeductionNotProvable()));
        assertFalse(proof.isDone());
    }

    @Test
    void classicActions() {
        LogicalGetActions actions = (new ClassicalConfiguration()).classicalActions();
        assertEquals(14, actions.perform().size());
    }

    @Test
    void thereIsOneDescriptorPerAvailableAction() {
        var actions = new ClassicalConfiguration().classicalActions().perform();

        assertEquals(Arrays.stream(AvailableAction.values()).map(Enum::name).toList(),
                actions.stream().map(ActionDescriptorDto::name).toList());
    }

    @Test
    void descriptorsHaveTheParametersOfTheActions() {
        var byName = new ClassicalConfiguration().classicalActions().perform().stream()
                .collect(Collectors.toMap(ActionDescriptorDto::name, ActionDescriptorDto::params));

        assertEquals(List.of(ParamKind.EXPRESSION), byName.get("ASSUME"));
        assertEquals(List.of(ParamKind.INT, ParamKind.EXPRESSION), byName.get("ORI1"));
        assertEquals(List.of(ParamKind.INT, ParamKind.EXPRESSION), byName.get("ORI2"));
        assertEquals(List.of(ParamKind.INT, ParamKind.EXPRESSION), byName.get("FE"));
        assertEquals(List.of(ParamKind.INT, ParamKind.INT, ParamKind.INT), byName.get("ORE"));
        assertEquals(List.of(ParamKind.INT, ParamKind.INT), byName.get("ANDI"));
        assertEquals(List.of(ParamKind.INT, ParamKind.INT), byName.get("MP"));
        assertEquals(List.of(ParamKind.INT, ParamKind.INT), byName.get("FI"));
        assertEquals(List.of(ParamKind.INT), byName.get("ANDE1"));
        assertEquals(List.of(ParamKind.INT), byName.get("ANDE2"));
        assertEquals(List.of(ParamKind.INT), byName.get("COPY"));
        assertEquals(List.of(ParamKind.INT), byName.get("NOTE"));
        assertEquals(List.of(), byName.get("NOTI"));
        assertEquals(List.of(), byName.get("DT"));
    }

    // The symbol of each rule is its rule text in a proof (what the backend writes in StepDto.rule) as the frontend's
    // renderRule shows it; the frontend test of renderRule holds the same table.
    private static final Map<String, List<String>> RULE_TEXT_AND_SYMBOL = Map.ofEntries(
            Map.entry("ASSUME", List.of("Ass", "Ass")), Map.entry("ORI1", List.of("|I", "∨I")),
            Map.entry("ORI2", List.of("|I", "∨I")), Map.entry("ORE", List.of("|E", "∨E")),
            Map.entry("ANDI", List.of("&I", "∧I")), Map.entry("ANDE1", List.of("&E", "∧E")),
            Map.entry("ANDE2", List.of("&E", "∧E")), Map.entry("COPY", List.of("Rep", "Rep")),
            Map.entry("NOTE", List.of("-E", "¬E")), Map.entry("NOTI", List.of("-I", "¬I")),
            Map.entry("DT", List.of("->I", "→I")), Map.entry("MP", List.of("->E", "→E")),
            Map.entry("FE", List.of("FE", "⊥E")), Map.entry("FI", List.of("FI", "⊥I")));

    private static Map<String, ActionDescriptorDto> descriptorsByName() {
        return new ClassicalConfiguration().classicalActions().perform().stream()
                .collect(Collectors.toMap(ActionDescriptorDto::name, descriptor -> descriptor));
    }

    @Test
    void everyDescriptorIsPresentedToTheUser() {
        var descriptors = new ClassicalConfiguration().classicalActions().perform();

        for (var descriptor : descriptors) {
            var name = descriptor.name();
            assertFalse(descriptor.label().isBlank(), name);
            assertFalse(descriptor.symbol().isBlank(), name);
            assertNotNull(descriptor.category(), name);
            assertFalse(descriptor.description().isBlank(), name);
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
        RULE_TEXT_AND_SYMBOL.forEach((name, ruleAndSymbol) -> assertEquals(ruleAndSymbol.get(1), descriptors.get(name).symbol(), name));
    }

    @Test
    void descriptorsGroupIntroductionsAndEliminations() {
        var descriptors = descriptorsByName();

        for (var name : List.of("ASSUME", "COPY")) {
            assertEquals(ActionCategory.OTHER, descriptors.get(name).category(), name);
        }
        for (var name : List.of("ORI1", "ORI2", "ANDI", "NOTI", "DT", "FI")) {
            assertEquals(ActionCategory.INTRODUCTION, descriptors.get(name).category(), name);
        }
        for (var name : List.of("ORE", "ANDE1", "ANDE2", "NOTE", "MP", "FE")) {
            assertEquals(ActionCategory.ELIMINATION, descriptors.get(name).category(), name);
        }
    }

    @Test
    void descriptorLabelsFollowTheOrderTheActionsExpectTheirSources() {
        var descriptors = descriptorsByName();

        var mp = descriptors.get("MP");
        assertEquals("Modus ponens", mp.label());
        assertEquals("From A → B and A, derive B", mp.description());
        assertEquals(List.of("Implication (A → B)", "Antecedent (A)"), mp.paramLabels());
        assertEquals(List.of("Disjunction (A ∨ B)", "Implication (A → C)", "Implication (B → C)"), descriptors.get("ORE").paramLabels());
        assertEquals(List.of("Line with A", "Negation (¬A)"), descriptors.get("FI").paramLabels());
        assertEquals(List.of("Line with A", "Right side (B)"), descriptors.get("ORI1").paramLabels());
        assertEquals(List.of("Line with B", "Left side (A)"), descriptors.get("ORI2").paramLabels());
        assertEquals(List.of("Falsum (⊥)", "Formula to derive (A)"), descriptors.get("FE").paramLabels());
    }

    @Test
    void mpIsAppliedToTheImplicationFirstAndTheAntecedentSecond() {
        var apply = new LogicalApplyAction<>(new ClassicalProofTransformer());
        var proof = new ProofDto(List.of(new StepDto("P -> Q", "Ass", 0, Map.of()), new StepDto("P", "Ass", 0, Map.of())), "classical", "Q");

        assertTrue(apply.perform(new ActionDto("MP", List.of(1, 2), Map.of()), proof).applied());
        assertFalse(apply.perform(new ActionDto("MP", List.of(2, 1), Map.of()), proof).applied());
    }

    @Test
    void everyDescriptorBuildsAnActionFromItsParameters() {
        var transformer = new ClassicalProofTransformer();
        for (var descriptor : new ClassicalConfiguration().classicalActions().perform()) {
            var lines = Collections.nCopies((int) descriptor.params().stream().filter(ParamKind.INT::equals).count(), 1);
            var extra = descriptor.params().contains(ParamKind.EXPRESSION) ? Map.of("expression", "P") : Map.<String, String>of();

            assertNotNull(transformer.from(new ActionDto(descriptor.name(), lines, extra)), descriptor.name());
        }
    }

    @Test
    void theActionListIsBuiltOnce() {
        var actions = new ClassicalConfiguration().classicalActions();

        assertSame(actions.perform(), actions.perform());
    }

    @Test
    void anActionThatNeedsAnExpressionRejectsABlankOne() {
        var transformer = new ClassicalProofTransformer();
        for (var name : List.of("ASSUME", "ORI1", "ORI2", "FE")) {
            for (var extra : List.of(Map.<String, String>of(), Map.of("expression", ""), Map.of("expression", "   "))) {
                var action = new ActionDto(name, List.of(1), extra);

                var exception = assertThrows(InvalidActionException.class, () -> transformer.from(action));
                assertEquals(name + " needs an expression", exception.getMessage());
            }
        }
    }

    @Test
    void anActionWithoutExpressionParameterIgnoresABlankOne() {
        assertNotNull(new ClassicalProofTransformer().from(new ActionDto("COPY", List.of(1), Map.of("expression", ""))));
    }

    @Test
    void applyingAnAssumeWithABlankExpressionIsAnInvalidAction() {
        var apply = new LogicalApplyAction<>(new ClassicalProofTransformer());
        var proof = new ProofDto(List.of(new StepDto("P", "Ass", 0, Map.of())), "classical", "P");

        for (var expression : List.of("", " \t ")) {
            var action = new ActionDto("ASSUME", List.of(), Map.of("expression", expression));

            var exception = assertThrows(InvalidActionException.class, () -> apply.perform(action, proof));
            assertEquals("ASSUME needs an expression", exception.getMessage());
        }
    }

    @Test
    void anUnparsableExpressionNeverLeaksNull() {
        var apply = new LogicalApplyAction<>(new ClassicalProofTransformer());
        var proof = new ProofDto(List.of(new StepDto("P", "Ass", 0, Map.of())), "classical", "P");

        for (var expression : List.of("P ->", "P Q", "(P", "->")) {
            var action = new ActionDto("ASSUME", List.of(), Map.of("expression", expression));

            var exception = assertThrows(InvalidActionException.class, () -> apply.perform(action, proof));
            assertTrue(exception.getMessage().startsWith("Cannot build action 'ASSUME': "), exception.getMessage());
            assertFalse(exception.getMessage().contains("null"), exception.getMessage());
        }
    }
}
