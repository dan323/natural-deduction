package com.dan323.uses.modal.nextuntil.test;

import com.dan323.model.Difficulty;
import com.dan323.proof.modal.nextuntil.ParseModalNextUntilAction;
import com.dan323.uses.Exercise;
import com.dan323.uses.modal.nextuntil.ModalNextUntilConfiguration;
import com.dan323.uses.modal.nextuntil.ModalNextUntilExercises;
import com.dan323.uses.modal.nextuntil.ModalNextUntilProofParser;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ModalNextUntilExercisesTest {

    private final ModalNextUntilExercises catalog = new ModalNextUntilExercises();

    @TestFactory
    Stream<DynamicTest> everyReferenceSolutionProvesItsExercise() {
        return catalog.exercises().stream()
                .map(exercise -> DynamicTest.dynamicTest(exercise.id(), () -> assertSolved(exercise)));
    }

    @Test
    void catalogShape() {
        var exercises = catalog.exercises();
        assertEquals("modal-next-until", catalog.logic());
        var ids = exercises.stream().map(Exercise::id).toList();
        assertEquals(exercises.size(), new HashSet<>(ids).size(), "ids are unique");
        var difficulties = exercises.stream().map(Exercise::difficulty).toList();
        assertEquals(difficulties.stream().sorted().toList(), difficulties, "ordered from easy to hard");
        assertEquals(new HashSet<>(List.of(Difficulty.values())), new HashSet<>(difficulties));
        exercises.forEach(exercise -> assertFalse(exercise.title().isBlank()));
    }

    @Test
    void theConfigurationExposesTheCatalog() {
        var bean = new ModalNextUntilConfiguration().modalNextUntilExercises();
        assertEquals("modal-next-until", bean.logic());
        assertEquals(catalog.exercises(), bean.exercises());
    }

    private static void assertSolved(Exercise exercise) {
        var premises = exercise.premises().stream().map(ParseModalNextUntilAction::parseExpression).toList();
        var goal = ParseModalNextUntilAction.parseExpression(exercise.goal());
        // The formulas are written the way the parser prints them, so a client shows them as the proof table does.
        exercise.premises().forEach(premise -> assertEquals(premise, ParseModalNextUntilAction.parseExpression(premise).toString()));
        assertEquals(exercise.goal(), goal.toString());

        var proof = new ModalNextUntilProofParser().parseProof(exercise.solution());
        assertEquals(premises, proof.getAssms(), exercise.id() + ": premises");
        assertEquals(goal, proof.getSteps().getLast().getStep(), exercise.id() + ": last line");
        assertEquals("s0", proof.getSteps().getLast().getState(), exercise.id() + ": the goal is in s0");
        assertTrue(proof.isDone(), exercise.id() + ": the reference solution is done");
    }
}
