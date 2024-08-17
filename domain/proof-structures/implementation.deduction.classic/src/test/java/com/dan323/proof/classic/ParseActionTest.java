package com.dan323.proof.classic;

import com.dan323.classical.*;
import com.dan323.classical.proof.ParseClassicalAction;
import com.dan323.expressions.classical.VariableClassic;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ParseActionTest {

    @Test
    public void parseAssTest() {
        ClassicalAction action = ParseClassicalAction.parseAction("ClassicAssume", List.of(), new VariableClassic("P"));
        assertEquals(new ClassicAssume(new VariableClassic("P")), action);
    }

    @Test
    public void parseOrTest() {
        ClassicalAction action1 = ParseClassicalAction.parseAction("ClassicOrI1", List.of(1), new VariableClassic("P"));
        ClassicalAction action2 = ParseClassicalAction.parseAction("ClassicOrI2", List.of(1), new VariableClassic("P"));
        ClassicalAction action3 = ParseClassicalAction.parseAction("ClassicOrE", List.of(1, 2, 3), null);
        assertEquals(new ClassicOrI1(1, new VariableClassic("P")), action1);
        assertEquals(new ClassicOrI2(1, new VariableClassic("P")), action2);
        assertEquals(new ClassicOrE(1, 2, 3), action3);
    }

    @Test
    public void parseAndTest() {
        ClassicalAction action1 = ParseClassicalAction.parseAction("ClassicAndE1", List.of(1), null);
        ClassicalAction action2 = ParseClassicalAction.parseAction("ClassicAndE2", List.of(1), null);
        ClassicalAction action3 = ParseClassicalAction.parseAction("ClassicAndI", List.of(1, 2), null);
        assertEquals(new ClassicAndE1(1), action1);
        assertEquals(new ClassicAndE2(1), action2);
        assertEquals(new ClassicAndI(1, 2), action3);
    }

    @Test
    public void parseCopyTest() {
        ClassicalAction action = ParseClassicalAction.parseAction("ClassicCopy", List.of(1), null);
        assertEquals(new ClassicCopy(1), action);
    }

    @Test
    public void parseNegTest() {
        ClassicalAction action = ParseClassicalAction.parseAction("ClassicNotE", List.of(1), null);
        assertEquals(new ClassicNotE(1), action);
        ClassicalAction action1 = ParseClassicalAction.parseAction("ClassicNotI", List.of(), null);
        assertEquals(new ClassicNotI(), action1);
    }

    @Test
    public void parseImpTest() {
        ClassicalAction action = ParseClassicalAction.parseAction("ClassicDeductionTheorem", List.of(), null);
        assertEquals(new ClassicDeductionTheorem(), action);
        ClassicalAction action1 = ParseClassicalAction.parseAction("ClassicModusPonens", List.of(1, 2), null);
        assertEquals(new ClassicModusPonens(1, 2), action1);
    }

    @Test
    public void parseFalseTest() {
        ClassicalAction action = ParseClassicalAction.parseAction("ClassicFI", List.of(1, 2), null);
        assertEquals(new ClassicFI(1, 2), action);
        ClassicalAction action1 = ParseClassicalAction.parseAction("ClassicFE", List.of(2), new VariableClassic("P"));
        assertEquals(new ClassicFE(2, new VariableClassic("P")), action1);
    }

    @Test
    public void parseExcepTest() {
        List<Integer> lst = List.of();
        assertThrows(IllegalArgumentException.class, () -> ParseClassicalAction.parseAction("BLA", lst, null));
    }
}