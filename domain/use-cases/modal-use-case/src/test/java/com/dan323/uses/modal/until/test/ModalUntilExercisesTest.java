package com.dan323.uses.modal.until.test;

import com.dan323.proof.modal.until.ParseModalUntilAction;
import com.dan323.uses.Exercise;
import com.dan323.uses.modal.until.ModalUntilConfiguration;
import com.dan323.uses.modal.until.ModalUntilExercises;
import com.dan323.uses.modal.until.ModalUntilProofParser;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.HashSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ModalUntilExercisesTest {

    private final ModalUntilExercises catalog = new ModalUntilExercises();

    @TestFactory
    Stream<DynamicTest> everyReferenceSolutionProvesItsExercise() {
        return catalog.exercises().stream()
                .map(exercise -> DynamicTest.dynamicTest(exercise.id(), () -> assertSolved(exercise)));
    }

    @Test
    void catalogShape() {
        var exercises = catalog.exercises();
        assertEquals("modal-until", catalog.logic());
        var ids = exercises.stream().map(Exercise::id).toList();
        assertEquals(exercises.size(), new HashSet<>(ids).size(), "ids are unique");
        var difficulties = exercises.stream().map(Exercise::difficulty).toList();
        assertEquals(difficulties.stream().sorted().toList(), difficulties, "ordered from easy to hard");
        exercises.forEach(exercise -> assertFalse(exercise.title().isBlank()));
        exercises.forEach(exercise -> assertTrue(exercise.solution().contains(" U") || exercise.goal().contains(" U "), exercise.id()));
    }

    @Test
    void theConfigurationExposesTheCatalog() {
        var bean = new ModalUntilConfiguration().modalUntilExercises();
        assertEquals(catalog.exercises(), bean.exercises());
    }

    private static void assertSolved(Exercise exercise) {
        var premises = exercise.premises().stream().map(ParseModalUntilAction::parseExpression).toList();
        var goal = ParseModalUntilAction.parseExpression(exercise.goal());
        // The formulas are written the way the parser prints them, so a client shows them as the proof table does.
        exercise.premises().forEach(premise -> assertEquals(premise, ParseModalUntilAction.parseExpression(premise).toString()));
        assertEquals(exercise.goal(), goal.toString());

        var proof = new ModalUntilProofParser().parseProof(exercise.solution());
        assertEquals(premises, proof.getAssms(), exercise.id() + ": premises");
        assertEquals(goal, proof.getSteps().getLast().getStep(), exercise.id() + ": last line");
        assertEquals(goal, proof.getGoal(), exercise.id() + ": goal");
        assertTrue(proof.isDone(), exercise.id() + ": the reference solution is done");
        proof.getSteps().stream().filter(step -> step.getAssumptionLevel() == 0 && step.getState() != null)
                .forEach(step -> assertEquals("s0", step.getState(), exercise.id() + ": premises and goal are in s0"));
    }
}
