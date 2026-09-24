package com.dan323.integration;

import com.dan323.uses.ActionsUseCases;
import com.dan323.uses.LogicalExercises;
import com.dan323.uses.internal.ActionsUseCaseConfiguration;
import com.dan323.uses.modal.ModalConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A deployment whose logics bring no {@link LogicalExercises} catalog at all (here: only the modal logic) must still
 * start. {@link ActionsUseCaseConfiguration#useCases} takes the catalogs as a {@code List}, and Spring injects an empty
 * list for it when no bean of that type exists, so every known logic answers an empty exercise list.
 */
class NoExerciseCatalogContextTest {

    @Test
    void contextWithoutAnyExerciseCatalogStartsAndAnswersNoExercises() {
        try (var context = new AnnotationConfigApplicationContext()) {
            // Spring Boot's conversions, so that the solve-timeout default ("10s") binds as it does in the application
            context.getBeanFactory().setConversionService(ApplicationConversionService.getSharedInstance());
            context.register(ActionsUseCaseConfiguration.class, ModalConfiguration.class);
            context.refresh();
            assertTrue(context.getBeansOfType(LogicalExercises.class).isEmpty());
            var useCases = context.getBean(ActionsUseCases.class);
            assertEquals(List.of(), useCases.getExercises("modal").perform());
        }
    }
}
