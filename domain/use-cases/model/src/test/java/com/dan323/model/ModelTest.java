package com.dan323.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    public void omittedFieldsBecomeEmpty() {
        var action = new ActionDto("Name", null, null);
        assertEquals(List.of(), action.sources());
        assertEquals(Map.of(), action.extraParameters());
        assertEquals(Map.of(), new StepDto("P", "Ass", 0, null).extraParameters());
        assertEquals(List.of(), new ProofDto(null, "classic", "P").steps());
    }

    @Test
    public void isDoneIsSafeOnEmptyProofs() {
        assertFalse(new ProofDto(List.of(), "classic", "P").isDone());
        assertTrue(new ProofDto(List.of(new StepDto("P", "Ass", 0, Map.of())), "classic", "P").isDone());
    }

    @Test
    void actionDescriptorTest() {
        var descriptor = ActionDescriptorDto.of("ORI1", ParamKind.INT, ParamKind.EXPRESSION);
        assertEquals("ORI1", descriptor.name());
        assertEquals(List.of(ParamKind.INT, ParamKind.EXPRESSION), descriptor.params());
        assertEquals(List.of(), ActionDescriptorDto.of("DT").params());
        assertEquals(List.of(), new ActionDescriptorDto("DT", null).params());
    }

    @Test
    void actionDescriptorCopiesItsParams() {
        var params = new ArrayList<>(List.of(ParamKind.INT));
        var descriptor = new ActionDescriptorDto("Rep", params);
        params.add(ParamKind.STATE);
        assertEquals(List.of(ParamKind.INT), descriptor.params());
    }

}
