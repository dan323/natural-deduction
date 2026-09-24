package com.dan323.uses.classical.test;

import com.dan323.classical.proof.ParseClassicalAction;
import com.dan323.model.Difficulty;
import com.dan323.uses.Exercise;
import com.dan323.uses.InvalidProofException;
import com.dan323.uses.classical.ClassicalExercises;
import com.dan323.uses.classical.ParseClassicalProof;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.opentest4j.AssertionFailedError;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ClassicalExercisesTest {

    private final ClassicalExercises catalog = new ClassicalExercises();

    @TestFactory
    public Stream<DynamicTest> everyReferenceSolutionProvesItsExercise() {
        return catalog.exercises().stream()
                .map(exercise -> DynamicTest.dynamicTest(exercise.id(), () -> assertSolved(exercise)));
    }

    @Test
    public void anUnprovableExerciseIsCaught() {
        var wrongGoal = new Exercise("wrong-goal", "Wrong goal", List.of("p", "p -> q"), "r", Difficulty.EASY, """
                p           Ass
                p -> q           Ass
                q           ->E [2, 1]
                """);
        assertThrows(AssertionFailedError.class, () -> assertSolved(wrongGoal));
        var badStep = new Exercise("bad-step", "Bad step", List.of("p", "p -> q"), "q", Difficulty.EASY, """
                p           Ass
                p -> q           Ass
                q           ->E [1, 2]
                """);
        assertThrows(InvalidProofException.class, () -> assertSolved(badStep));
    }

    @Test
    public void catalogShape() {
        var exercises = catalog.exercises();
        assertEquals("classical", catalog.logic());
        assertTrue(exercises.size() >= 12, "about a dozen exercises");
        assertEquals(exercises.size(), new HashSet<>(exercises.stream().map(Exercise::id).toList()).size(), "ids are unique");
        var difficulties = exercises.stream().map(Exercise::difficulty).toList();
        assertEquals(difficulties.stream().sorted().toList(), difficulties, "ordered from easy to hard");
        assertEquals(new HashSet<>(List.of(Difficulty.values())), new HashSet<>(difficulties));
        exercises.forEach(exercise -> assertFalse(exercise.title().isBlank()));
    }

    @Test
    public void dtoLeavesTheSolutionOut() {
        var exercise = catalog.exercises().getFirst();
        var dto = exercise.toDto();
        assertEquals(exercise.id(), dto.id());
        assertEquals(exercise.title(), dto.title());
        assertEquals(exercise.premises(), dto.premises());
        assertEquals(exercise.goal(), dto.goal());
        assertEquals(exercise.difficulty(), dto.difficulty());
        assertEquals(5, dto.getClass().getRecordComponents().length);
    }

    private static void assertSolved(Exercise exercise) {
        var premises = exercise.premises().stream().map(ParseClassicalAction::parseExpression).toList();
        var goal = ParseClassicalAction.parseExpression(exercise.goal());
        // The formulas are written the way the parser prints them, so a client shows them as the proof table does.
        exercise.premises().forEach(premise -> assertEquals(premise, ParseClassicalAction.parseExpression(premise).toString()));
        assertEquals(exercise.goal(), goal.toString());

        var proof = new ParseClassicalProof().parseProof(exercise.solution());
        assertEquals(premises, proof.getAssms(), exercise.id() + ": premises");
        assertEquals(goal, proof.getSteps().getLast().getStep(), exercise.id() + ": last line");
        assertEquals(goal, proof.getGoal(), exercise.id() + ": goal");
        assertTrue(proof.isDone(), exercise.id() + ": the reference solution is done");
    }
}
