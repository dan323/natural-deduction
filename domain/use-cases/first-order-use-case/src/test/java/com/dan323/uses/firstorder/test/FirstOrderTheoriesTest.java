package com.dan323.uses.firstorder.test;

import com.dan323.model.TheoryDto;
import com.dan323.proof.firstorder.proof.ParseFirstOrderAction;
import com.dan323.uses.firstorder.FirstOrderConfiguration;
import com.dan323.uses.firstorder.FirstOrderTheories;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class FirstOrderTheoriesTest {

    private final FirstOrderTheories catalog = new FirstOrderTheories();

    @TestFactory
    Stream<DynamicTest> thePremisesOfEveryTheoryParse() {
        return catalog.theories().stream()
                .flatMap(theory -> theory.premises().stream()
                        .map(premise -> DynamicTest.dynamicTest(theory.id() + ": " + premise, () -> {
                            var parsed = ParseFirstOrderAction.parseExpression(premise);
                            assertNotNull(parsed);
                            // Written the way the parser prints them, so a client shows them as the proof table does.
                            assertEquals(premise, parsed.toString());
                            // Axioms: nothing is free in them except the constants of the theory.
                            assertTrue(Stream.of("x", "y", "z").noneMatch(parsed.freeVariables()::contains));
                        })));
    }

    @Test
    void theGroupAxioms() {
        assertEquals("first-order", catalog.logic());
        assertEquals(List.of(new TheoryDto("group", "Group", List.of(
                "forall x. forall y. forall z. m(m(x, y), z) = m(x, m(y, z))",
                "forall x. m(e, x) = x & m(x, e) = x",
                "forall x. m(i(x), x) = e & m(x, i(x)) = e"))), catalog.theories());
        var ids = catalog.theories().stream().map(TheoryDto::id).toList();
        assertEquals(ids.size(), new HashSet<>(ids).size(), "ids are unique");
    }

    @Test
    void theConfigurationExposesTheTheories() {
        var bean = new FirstOrderConfiguration().firstOrderTheories();
        assertEquals("first-order", bean.logic());
        assertEquals(catalog.theories(), bean.theories());
    }
}
