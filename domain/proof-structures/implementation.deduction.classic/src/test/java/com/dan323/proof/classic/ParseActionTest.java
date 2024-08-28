package com.dan323.proof.classic;

import com.dan323.classical.*;
import com.dan323.classical.proof.AvailableAction;
import com.dan323.classical.proof.ParseClassicalAction;
import com.dan323.expressions.classical.VariableClassic;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ParseActionTest {

    @Test
    public void parseAssTest() {
        ClassicalAction action = ParseClassicalAction.parseAction(AvailableAction.ASSUME.name(), List.of(), new VariableClassic("P"));
        assertEquals(new ClassicAssume(new VariableClassic("P")), action);
    }

    @Test
    public void parseOrTest() {
        ClassicalAction action1 = ParseClassicalAction.parseAction(AvailableAction.ORI1.name(), List.of(1), new VariableClassic("P"));
        ClassicalAction action2 = ParseClassicalAction.parseAction(AvailableAction.ORI2.name(), List.of(1), new VariableClassic("P"));
        ClassicalAction action3 = ParseClassicalAction.parseAction(AvailableAction.ORE.name(), List.of(1, 2, 3), null);
        assertEquals(new ClassicOrI1(1, new VariableClassic("P")), action1);
        assertEquals(new ClassicOrI2(1, new VariableClassic("P")), action2);
        assertEquals(new ClassicOrE(1, 2, 3), action3);
    }

    @Test
    public void parseAndTest() {
        ClassicalAction action1 = ParseClassicalAction.parseAction(AvailableAction.ANDE1.name(), List.of(1), null);
        ClassicalAction action2 = ParseClassicalAction.parseAction(AvailableAction.ANDE2.name(), List.of(1), null);
        ClassicalAction action3 = ParseClassicalAction.parseAction(AvailableAction.ANDI.name(), List.of(1, 2), null);
        assertEquals(new ClassicAndE1(1), action1);
        assertEquals(new ClassicAndE2(1), action2);
        assertEquals(new ClassicAndI(1, 2), action3);
    }

    @Test
    public void parseCopyTest() {
        ClassicalAction action = ParseClassicalAction.parseAction(AvailableAction.COPY.name(), List.of(1), null);
        assertEquals(new ClassicCopy(1), action);
    }

    @Test
    public void parseNegTest() {
        ClassicalAction action = ParseClassicalAction.parseAction(AvailableAction.NOTE.name(), List.of(1), null);
        assertEquals(new ClassicNotE(1), action);
        ClassicalAction action1 = ParseClassicalAction.parseAction(AvailableAction.NOTI.name(), List.of(), null);
        assertEquals(new ClassicNotI(), action1);
    }

    @Test
    public void parseImpTest() {
        ClassicalAction action = ParseClassicalAction.parseAction(AvailableAction.DT.name(), List.of(), null);
        assertEquals(new ClassicDeductionTheorem(), action);
        ClassicalAction action1 = ParseClassicalAction.parseAction(AvailableAction.MP.name(), List.of(1, 2), null);
        assertEquals(new ClassicModusPonens(1, 2), action1);
    }

    @Test
    public void parseFalseTest() {
        ClassicalAction action = ParseClassicalAction.parseAction(AvailableAction.FI.name(), List.of(1, 2), null);
        assertEquals(new ClassicFI(1, 2), action);
        ClassicalAction action1 = ParseClassicalAction.parseAction(AvailableAction.FE.name(), List.of(2), new VariableClassic("P"));
        assertEquals(new ClassicFE(2, new VariableClassic("P")), action1);
    }

    @Test
    public void parseExcepTest() {
        List<Integer> lst = List.of();
        assertThrows(IllegalArgumentException.class, () -> ParseClassicalAction.parseAction("BLA", lst, null));
    }
}