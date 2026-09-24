package com.dan323.uses.intuitionistic.test;

import com.dan323.classical.proof.ParseClassicalAction;
import com.dan323.model.Difficulty;
import com.dan323.uses.Exercise;
import com.dan323.uses.InvalidProofException;
import com.dan323.uses.classical.ClassicalExercises;
import com.dan323.uses.classical.ParseClassicalProof;
import com.dan323.uses.intuitionistic.IntuitionisticConfiguration;
import com.dan323.uses.intuitionistic.IntuitionisticExercises;
import com.dan323.uses.intuitionistic.ParseIntuitionisticProof;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class IntuitionisticExercisesTest {

    private final IntuitionisticExercises catalog = new IntuitionisticExercises();

    @TestFactory
    Stream<DynamicTest> everyReferenceSolutionProvesItsExercise() {
        return catalog.exercises().stream()
                .map(exercise -> DynamicTest.dynamicTest(exercise.id(), () -> assertSolved(exercise)));
    }

    @TestFactory
    Stream<DynamicTest> theClassicalOnlyExercisesAreNotProvedIntuitionistically() {
        var classicalOnly = new ClassicalExercises().exercises().stream()
                .filter(exercise -> IntuitionisticExercises.CLASSICAL_ONLY.contains(exercise.id()))
                .toList();
        assertEquals(IntuitionisticExercises.CLASSICAL_ONLY.size(), classicalOnly.size(), "every excluded id is a classical exercise");
        return classicalOnly.stream().map(exercise -> DynamicTest.dynamicTest(exercise.id(), () -> {
            assertTrue(new ParseClassicalProof().parseProof(exercise.solution()).isDone());
            var parser = new ParseIntuitionisticProof();
            assertThrows(InvalidProofException.class, () -> parser.parseProof(exercise.solution()));
        }));
    }

    @Test
    void catalogShape() {
        var exercises = catalog.exercises();
        assertEquals("intuitionistic", catalog.logic());
        assertTrue(exercises.size() >= 12, "about a dozen exercises");
        var ids = exercises.stream().map(Exercise::id).toList();
        assertEquals(exercises.size(), new HashSet<>(ids).size(), "ids are unique");
        var difficulties = exercises.stream().map(Exercise::difficulty).toList();
        assertEquals(difficulties.stream().sorted().toList(), difficulties, "ordered from easy to hard");
        assertEquals(new HashSet<>(List.of(Difficulty.values())), new HashSet<>(difficulties));
        exercises.forEach(exercise -> assertFalse(exercise.title().isBlank()));
        IntuitionisticExercises.CLASSICAL_ONLY.forEach(id -> assertFalse(ids.contains(id), id));
        new ClassicalExercises().exercises().stream()
                .filter(exercise -> !IntuitionisticExercises.CLASSICAL_ONLY.contains(exercise.id()))
                .forEach(exercise -> assertTrue(exercises.contains(exercise), exercise.id()));
    }

    @Test
    void theConfigurationExposesTheCatalog() {
        var bean = new IntuitionisticConfiguration().intuitionisticExercises();
        assertEquals("intuitionistic", bean.logic());
        assertEquals(catalog.exercises(), bean.exercises());
    }

    private static void assertSolved(Exercise exercise) {
        var premises = exercise.premises().stream().map(ParseClassicalAction::parseExpression).toList();
        var goal = ParseClassicalAction.parseExpression(exercise.goal());
        // The formulas are written the way the parser prints them, so a client shows them as the proof table does.
        exercise.premises().forEach(premise -> assertEquals(premise, ParseClassicalAction.parseExpression(premise).toString()));
        assertEquals(exercise.goal(), goal.toString());

        var proof = new ParseIntuitionisticProof().parseProof(exercise.solution());
        assertEquals(premises, proof.getAssms(), exercise.id() + ": premises");
        assertEquals(goal, proof.getSteps().getLast().getStep(), exercise.id() + ": last line");
        assertEquals(goal, proof.getGoal(), exercise.id() + ": goal");
        assertTrue(proof.isDone(), exercise.id() + ": the reference solution is done");
    }
}
