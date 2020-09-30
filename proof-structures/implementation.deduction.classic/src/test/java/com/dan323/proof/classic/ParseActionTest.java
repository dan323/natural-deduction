package com.dan323.proof.classic;

import com.dan323.classical.*;
import com.dan323.classical.proof.ParseClassicalAction;
import com.dan323.expressions.classical.VariableClassic;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParseActionTest {

    @Test
    public void parseAssTest() {
        ClassicalAction action = ParseClassicalAction.parseAction("Ass", List.of(), new VariableClassic("P"));
        assertEquals(new ClassicAssume(new VariableClassic("P")), action);
    }

    @Test
    public void parseOrTest() {
        ClassicalAction action1 = ParseClassicalAction.parseAction("|I1", List.of(1), new VariableClassic("P"));
        ClassicalAction action2 = ParseClassicalAction.parseAction("|I2", List.of(1), new VariableClassic("P"));
        ClassicalAction action3 = ParseClassicalAction.parseAction("|E", List.of(1, 2, 3), null);
        assertEquals(new ClassicOrI1(1, new VariableClassic("P")), action1);
        assertEquals(new ClassicOrI2(1, new VariableClassic("P")), action2);
        assertEquals(new ClassicOrE(1, 2, 3), action3);
    }

    @Test
    public void parseAndTest() {
        ClassicalAction action1 = ParseClassicalAction.parseAction("&E1", List.of(1), null);
        ClassicalAction action2 = ParseClassicalAction.parseAction("&E2", List.of(1), null);
        ClassicalAction action3 = ParseClassicalAction.parseAction("&I", List.of(1,2), null);
        assertEquals(new ClassicAndE1(1), action1);
        assertEquals(new ClassicAndE2(1), action2);
        assertEquals(new ClassicAndI(1,2), action3);
    }
}