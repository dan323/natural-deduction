package com.dan323.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ModelTest {

    @Test
    public void actionTest() {
        ActionDto action = new ActionDto("Name", List.of(1, 2), Map.of("expression", "P", "state", "Information"));
        assertEquals("Name", action.name());
        assertEquals("Information", action.extraParameters().get("state"));
        assertEquals(List.of(1, 2), action.sources());
    }

    @Test
    public void proofTest() {
        ProofDto p = new ProofDto(List.of(new StepDto("Q", "->I", 1, Map.of()),
                new StepDto("P | Q", "->E", 8, Map.of())), "classic", "P & G");
        assertEquals("classic", p.logic());
        assertEquals("P & G", p.goal());
        assertEquals(2, p.steps().size());
        assertEquals("Q", p.steps().get(0).expression());
        assertEquals("->E", p.steps().get(1).rule());
        assertEquals(8, p.steps().get(1).assmsLevel());
    }

}
