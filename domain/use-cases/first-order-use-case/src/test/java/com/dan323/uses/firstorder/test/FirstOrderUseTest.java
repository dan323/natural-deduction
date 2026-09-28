package com.dan323.uses.firstorder.test;

import com.dan323.model.ActionCategory;
import com.dan323.model.ActionDescriptorDto;
import com.dan323.model.ActionDto;
import com.dan323.model.ParamKind;
import com.dan323.model.ProofDto;
import com.dan323.model.StepDto;
import com.dan323.uses.InvalidActionException;
import com.dan323.uses.InvalidProofException;
import com.dan323.uses.LogicalApplyAction;
import com.dan323.uses.firstorder.AvailableFirstOrderAction;
import com.dan323.uses.firstorder.FirstOrderConfiguration;
import com.dan323.uses.firstorder.FirstOrderProofParser;
import com.dan323.uses.firstorder.FirstOrderProofTransformer;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class FirstOrderUseTest {

    private static final String LOGIC = "first-order";
    private static final String GAP = " ".repeat(11);
    private final FirstOrderProofTransformer transformer = new FirstOrderProofTransformer();
    private final LogicalApplyAction<?, ?, ?, ?> apply = new LogicalApplyAction<>(transformer);

    private static StepDto step(String expression, String rule, int level) {
        return new StepDto(expression, rule, level, Map.of());
    }

    private static ActionDto action(String name, List<Integer> sources, Map<String, String> extra) {
        return new ActionDto(name, sources, extra);
    }

    @Test
    void theBeansAreKeyedByTheLogicName() {
        var configuration = new FirstOrderConfiguration();
        assertEquals(LOGIC, configuration.firstOrderActions().getLogicName());
        assertEquals(LOGIC, configuration.firstOrderTransformer().logic());
        assertEquals(LOGIC, configuration.firstOrderProofParser().logic());
        assertFalse(configuration.firstOrderTransformer().hasSolver());
    }

    @Test
    void everyDescriptorIsDescribed() {
        var actions = new FirstOrderConfiguration().firstOrderActions().perform();

        assertEquals(AvailableFirstOrderAction.values().length, actions.size());
        var names = actions.stream().map(ActionDescriptorDto::name).toList();
        assertEquals(names.size(), new HashSet<>(names).size());
        for (var descriptor : actions) {
            assertFalse(descriptor.label().isBlank(), descriptor.name());
            assertFalse(descriptor.symbol().isBlank(), descriptor.name());
            assertFalse(descriptor.description().isBlank(), descriptor.name());
            assertNotNull(descriptor.category(), descriptor.name());
            assertEquals(descriptor.params().size(), descriptor.paramLabels().size(), descriptor.name());
        }
    }

    @Test
    void theQuantifierAndEqualityRulesTakeTheirInputs() {
        var byName = new FirstOrderConfiguration().firstOrderActions().perform().stream()
                .collect(Collectors.toMap(ActionDescriptorDto::name, descriptor -> descriptor));

        assertEquals(List.of(ParamKind.INT, ParamKind.TERM), byName.get("∀E").params());
        assertEquals(List.of(ParamKind.TERM), byName.get("=I").params());
        assertEquals(List.of(ParamKind.INT, ParamKind.EXPRESSION), byName.get("∀I").params());
        assertEquals(List.of(ParamKind.INT, ParamKind.EXPRESSION), byName.get("∃I").params());
        assertEquals(List.of(ParamKind.INT), byName.get("∃E").params());
        assertEquals(List.of(ParamKind.INT, ParamKind.INT, ParamKind.EXPRESSION), byName.get("=E").params());
        assertEquals("For-all introduction", byName.get("∀I").label());
        assertEquals("∀I", byName.get("∀I").symbol());
        assertEquals(ActionCategory.INTRODUCTION, byName.get("∀I").category());
        assertEquals(ActionCategory.ELIMINATION, byName.get("∃E").category());
        assertEquals(List.of(), byName.get("->I").params());
    }

    @Test
    void everyDescriptorNameBuildsAnAction() {
        for (var descriptor : new FirstOrderConfiguration().firstOrderActions().perform()) {
            var lines = Collections.nCopies((int) descriptor.params().stream().filter(ParamKind.INT::equals).count(), 1);
            var extra = Map.of("expression", "P(a)", "term", "a");

            assertNotNull(transformer.from(action(descriptor.name(), lines, extra)), descriptor.name());
        }
    }

    @Test
    void aTermRoundTrips() {
        var proof = new ProofDto(List.of(step("forall x. P(x)", "Ass", 0)), LOGIC, "exists x. P(x)");

        var result = apply.perform(action("∀E", List.of(1), Map.of("term", "f(a, g(b))")), proof);

        assertTrue(result.applied());
        assertEquals(step("P(f(a, g(b)))", "∀E [1]", 0), result.proof().steps().getLast());
        // The step is read back from the proof: the term is recovered from the formula.
        var replayed = transformer.fromProof(transformer.from(result.proof()));
        assertEquals(result.proof(), replayed);

        var equality = apply.perform(action("=I", List.of(), Map.of("term", "f(a, g(b))")), result.proof());
        assertEquals(step("f(a, g(b)) = f(a, g(b))", "=I", 0), equality.proof().steps().getLast());
        assertEquals(equality.proof(), transformer.fromProof(transformer.from(equality.proof())));
    }

    @Test
    void provesForallGivesExists() {
        var proof = new ProofDto(List.of(step("forall x. P(x)", "Ass", 0)), LOGIC, "exists x. P(x)");

        var instance = apply.perform(action("∀E", List.of(1), Map.of("term", "a")), proof);
        assertFalse(instance.done());
        var result = apply.perform(action("∃I", List.of(2), Map.of("expression", "exists y. P(y)")), instance.proof());

        assertTrue(result.applied());
        assertTrue(result.done(), "the goal is equal up to renaming of the bound variable");
        assertEquals(LOGIC, result.proof().logic());
        assertEquals(step("exists y. P(y)", "∃I [2]", 0), result.proof().steps().getLast());
    }

    @Test
    void aForbiddenGeneralizationIsRejected() {
        var proof = new ProofDto(List.of(step("P(a)", "Ass", 0)), LOGIC, "forall x. P(x)");

        var result = apply.perform(action("∀I", List.of(1), Map.of("expression", "forall x. P(x)")), proof);

        assertFalse(result.applied());
        assertEquals("Rule ∀I cannot be applied to lines [1]", result.message());
    }

    @Test
    void theClassicalNamesOfTheSharedRulesAreAccepted() {
        var proof = new ProofDto(List.of(step("P(a)", "Ass", 0)), LOGIC, "P(a)");

        for (var name : List.of("COPY", "Rep")) {
            var result = apply.perform(action(name, List.of(1), Map.of()), proof);
            assertEquals(step("P(a)", "Rep [1]", 0), result.proof().steps().getLast(), name);
        }
    }

    @Test
    void anActionWithoutItsTermIsInvalid() {
        var proof = new ProofDto(List.of(step("forall x. P(x)", "Ass", 0)), LOGIC, "P(a)");

        var noTerm = assertThrows(InvalidActionException.class, () -> apply.perform(action("∀E", List.of(1), Map.of()), proof));
        assertEquals("∀E needs a term", noTerm.getMessage());
        assertThrows(InvalidActionException.class, () -> apply.perform(action("=I", List.of(), Map.of("term", " ")), proof));
        assertThrows(InvalidActionException.class, () -> apply.perform(action("=I", List.of(), Map.of("term", "P(")), proof));
        assertThrows(InvalidActionException.class, () -> apply.perform(action("∀I", List.of(1), Map.of()), proof));
        assertThrows(InvalidActionException.class, () -> apply.perform(action("NOPE", List.of(1), Map.of()), proof));
    }

    @Test
    void aTamperedProofIsInvalid() {
        var badRule = new ProofDto(List.of(step("forall x. P(x)", "Ass", 0), step("Q(a)", "∀E [1]", 0)), LOGIC, "Q(a)");
        var ex = assertThrows(InvalidProofException.class, () -> transformer.from(badRule));
        assertTrue(ex.getMessage().startsWith("Line 2 "), ex.getMessage());

        var generalization = new ProofDto(List.of(step("P(a)", "Ass", 0), step("forall x. P(x)", "∀I [1]", 0)), LOGIC, "forall x. P(x)");
        assertThrows(InvalidProofException.class, () -> transformer.from(generalization));

        assertThrows(InvalidProofException.class, () -> transformer.from(new ProofDto(List.of(), LOGIC, "forall x.")));
    }

    @Test
    void anEmptyProofKeepsItsGoal() {
        var proof = transformer.from(new ProofDto(List.of(), LOGIC, "forall x. x = x"));

        assertEquals(new ProofDto(List.of(), LOGIC, "forall x. x = x"), transformer.fromProof(proof));
    }

    @Test
    void parsesAProofText() {
        var text = "exists x. P(x)" + GAP + "Ass\n"
                + "   P(a)" + GAP + "Ass\n"
                + "   exists y. P(y)" + GAP + "∃I [2]\n"
                + "exists y. P(y)" + GAP + "∃E [1, 2-3]\n";

        var proof = new FirstOrderProofParser().parseProof(text);

        assertTrue(proof.isDone());
        assertEquals(text, proof.toString());
        var dto = transformer.fromProof(proof);
        assertEquals(step("exists y. P(y)", "∃E [1, 2-3]", 0), dto.steps().getLast());
        assertEquals(1, dto.steps().get(1).assmsLevel());
    }

    @Test
    void rejectsABadProofText() {
        var parser = new FirstOrderProofParser();

        assertThrows(InvalidProofException.class, () -> parser.parseProof("P(a)" + GAP + "Ass\nforall x. P(x)" + GAP + "∀I [1]\n"));
        assertThrows(InvalidProofException.class, () -> parser.parseProof("P(a)" + GAP + "Nope\n"));
        assertThrows(InvalidProofException.class, () -> parser.parseProof("P(" + GAP + "Ass\n"));
        assertThrows(InvalidProofException.class, () -> parser.parseProof(""));
    }
}
