package com.dan323.uses.modal.test;

import com.dan323.model.Difficulty;
import com.dan323.proof.modal.proof.ParseModalAction;
import com.dan323.uses.Exercise;
import com.dan323.uses.InvalidProofException;
import com.dan323.uses.modal.ModalConfiguration;
import com.dan323.uses.modal.ModalExercises;
import com.dan323.uses.modal.ModalProofParser;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.opentest4j.AssertionFailedError;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ModalExercisesTest {

    private final ModalExercises catalog = new ModalExercises();

    @TestFactory
    Stream<DynamicTest> everyReferenceSolutionProvesItsExercise() {
        return catalog.exercises().stream()
                .map(exercise -> DynamicTest.dynamicTest(exercise.id(), () -> assertSolved(exercise)));
    }

    @Test
    void anUnprovableExerciseIsCaught() {
        // Proof.isDone() does not look at the state, so the test does: p holds in s1 here, not in s0.
        var wrongState = new Exercise("wrong-state", "Wrong state", List.of("[] p", "s0 <= s1"), "p", Difficulty.EASY, """
                s0: [] p           Ass
                s0 <= s1           Ass
                s1: p           []E [1, 2]
                """);
        var error = assertThrows(AssertionFailedError.class, () -> assertSolved(wrongState));
        assertTrue(error.getMessage().contains("initial state"), error.getMessage());
        var badStep = new Exercise("bad-step", "Bad step", List.of("[] p"), "p", Difficulty.EASY, """
                s0: [] p           Ass
                s0: p           []E [1, 1]
                """);
        assertThrows(InvalidProofException.class, () -> assertSolved(badStep));
    }

    @Test
    void catalogShape() {
        var exercises = catalog.exercises();
        assertEquals("modal", catalog.logic());
        assertTrue(exercises.size() >= 5, "a handful of exercises");
        assertEquals(exercises.size(), new HashSet<>(exercises.stream().map(Exercise::id).toList()).size(), "ids are unique");
        var difficulties = exercises.stream().map(Exercise::difficulty).toList();
        assertEquals(difficulties.stream().sorted().toList(), difficulties, "ordered from easy to hard");
        assertEquals(new HashSet<>(List.of(Difficulty.values())), new HashSet<>(difficulties));
        exercises.forEach(exercise -> assertFalse(exercise.title().isBlank()));
        // Refl and Trans, the rules of the relation between states, are both practised.
        assertTrue(exercises.stream().anyMatch(exercise -> exercise.solution().contains("Refl [")));
        assertTrue(exercises.stream().anyMatch(exercise -> exercise.solution().contains("Trans [")));
    }

    @Test
    void theConfigurationExposesTheCatalog() {
        var bean = new ModalConfiguration().modalExercises();
        assertEquals("modal", bean.logic());
        assertEquals(catalog.exercises(), bean.exercises());
    }

    private static void assertSolved(Exercise exercise) {
        var premises = exercise.premises().stream().map(ParseModalAction::parseExpression).toList();
        var goal = ParseModalAction.parseExpression(exercise.goal());
        // The formulas are written the way the parser prints them, so a client shows them as the proof table does.
        exercise.premises().forEach(premise -> assertEquals(premise, ParseModalAction.parseExpression(premise).toString()));
        assertEquals(exercise.goal(), goal.toString());

        var proof = new ModalProofParser().parseProof(exercise.solution());
        assertEquals(premises, proof.getAssms(), exercise.id() + ": premises");
        var last = proof.getSteps().getLast();
        assertEquals(goal, last.getStep(), exercise.id() + ": last line");
        assertEquals(0, last.getAssumptionLevel(), exercise.id() + ": the goal is not inside a subproof");
        assertEquals(proof.getState0(), last.getState(), exercise.id() + ": the goal is derived in the initial state");
        assertEquals(goal, proof.getGoal(), exercise.id() + ": goal");
        assertTrue(proof.isDone(), exercise.id() + ": the reference solution is done");
    }
}
