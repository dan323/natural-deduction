package com.dan323.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ModelTest {

    @Test
    public void actionTest() {
        Action action = new Action();
        action.setName("Name");
        action.setExtraInformation(new Extra("P","Information"));
        action.setSources(List.of(1, 2));
        assertEquals("Name", action.getName());
        assertEquals("Information", action.getExtraInformation().getState());
        assertEquals(List.of(1, 2), action.getSources());
    }

    @Test
    public void proofTest() {
        Proof<String> p = new Proof<>();
        p.setLogic("classic");
        p.setGoal("P & G");
        p.addStep(new Step<>("Q", "->I", 1));
        p.addStep(new Step<>("P | Q", "->E", 8));
        assertEquals("classic", p.getLogic());
        assertEquals("P & G", p.getGoal());
        assertEquals(List.of(new Step<>("Q", "->I", 1), new Step<>("P | Q", "->E", 8)), p.getSteps());
    }

}
