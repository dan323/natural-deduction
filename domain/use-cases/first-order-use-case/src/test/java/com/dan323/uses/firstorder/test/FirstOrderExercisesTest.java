package com.dan323.uses.firstorder.test;

import com.dan323.model.Difficulty;
import com.dan323.proof.firstorder.proof.ParseFirstOrderAction;
import com.dan323.uses.Exercise;
import com.dan323.uses.firstorder.FirstOrderConfiguration;
import com.dan323.uses.firstorder.FirstOrderExercises;
import com.dan323.uses.firstorder.FirstOrderProofParser;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class FirstOrderExercisesTest {

    private final FirstOrderExercises catalog = new FirstOrderExercises();

    @TestFactory
    Stream<DynamicTest> everyReferenceSolutionProvesItsExercise() {
        return catalog.exercises().stream()
                .map(exercise -> DynamicTest.dynamicTest(exercise.id(), () -> assertSolved(exercise)));
    }

    @Test
    void catalogShape() {
        var exercises = catalog.exercises();
        assertEquals("first-order", catalog.logic());
        var ids = exercises.stream().map(Exercise::id).toList();
        assertEquals(exercises.size(), new HashSet<>(ids).size(), "ids are unique");
        var difficulties = exercises.stream().map(Exercise::difficulty).toList();
        assertEquals(difficulties.stream().sorted().toList(), difficulties, "ordered from easy to hard");
        assertEquals(new HashSet<>(List.of(Difficulty.values())), new HashSet<>(difficulties));
        exercises.forEach(exercise -> assertFalse(exercise.title().isBlank()));
        assertTrue(ids.containsAll(List.of("symmetry", "transitivity", "forall-and", "forall-swap", "exists-forall-swap")));
    }

    @Test
    void theConfigurationExposesTheCatalog() {
        var bean = new FirstOrderConfiguration().firstOrderExercises();
        assertEquals("first-order", bean.logic());
        assertEquals(catalog.exercises(), bean.exercises());
    }

    private static void assertSolved(Exercise exercise) {
        var premises = exercise.premises().stream().map(ParseFirstOrderAction::parseExpression).toList();
        var goal = ParseFirstOrderAction.parseExpression(exercise.goal());
        // The formulas are written the way the parser prints them, so a client shows them as the proof table does.
        exercise.premises().forEach(premise -> assertEquals(premise, ParseFirstOrderAction.parseExpression(premise).toString()));
        assertEquals(exercise.goal(), goal.toString());

        var proof = new FirstOrderProofParser().parseProof(exercise.solution());
        assertEquals(premises, proof.getAssms(), exercise.id() + ": premises");
        assertEquals(goal, proof.getSteps().getLast().getStep(), exercise.id() + ": last line");
        assertEquals(exercise.solution(), proof.toString(), exercise.id() + ": the solution is written as the proof prints");
        assertTrue(proof.isDone(), exercise.id() + ": the reference solution is done");
    }
}
