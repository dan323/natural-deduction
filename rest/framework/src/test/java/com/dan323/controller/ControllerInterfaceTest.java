package com.dan323.controller;

import com.dan323.model.Difficulty;
import com.dan323.model.ExerciseDto;
import com.dan323.uses.ActionsUseCases;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ControllerInterfaceTest {

    private final ActionsUseCases useCases = mock(ActionsUseCases.class);
    private final ControllerInterface controller = new ControllerInterface(useCases);

    @Test
    void exercisesAreReturnedAsTheUseCaseListsThem() {
        var exercises = List.of(new ExerciseDto("modus-ponens", "Modus ponens", List.of("p", "p -> q"), "q", Difficulty.EASY));
        when(useCases.getExercises("classical")).thenReturn(() -> exercises);

        var response = controller.getExercises("classical");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(exercises, response.getBody());
    }

    @Test
    void aLogicWithoutExercisesIsAnEmptyList() {
        when(useCases.getExercises("modal")).thenReturn(List::of);

        var response = controller.getExercises("modal");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(), response.getBody());
    }
}
