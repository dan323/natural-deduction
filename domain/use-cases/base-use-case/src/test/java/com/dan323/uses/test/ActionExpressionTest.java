package com.dan323.uses.test;

import com.dan323.model.ActionDescriptorDto;
import com.dan323.model.ActionDto;
import com.dan323.model.ParamKind;
import com.dan323.uses.ActionExpression;
import com.dan323.uses.InvalidActionException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ActionExpressionTest {

    private static final List<ActionDescriptorDto> DESCRIPTORS = List.of(
            ActionDescriptorDto.of("ASSUME", ParamKind.EXPRESSION),
            ActionDescriptorDto.of("ORI1", ParamKind.INT, ParamKind.EXPRESSION),
            ActionDescriptorDto.of("COPY", ParamKind.INT),
            ActionDescriptorDto.of("DT"));

    @Test
    public void missingExpressionIsRejectedWhenTheActionNeedsOneTest() {
        var exception = assertThrows(InvalidActionException.class, () -> ActionExpression.of(new ActionDto("ASSUME", List.of(), Map.of()), DESCRIPTORS));
        assertEquals("ASSUME needs an expression", exception.getMessage());
        var withLine = assertThrows(InvalidActionException.class, () -> ActionExpression.of(new ActionDto("ORI1", List.of(1), Map.of("state", "s0")), DESCRIPTORS));
        assertEquals("ORI1 needs an expression", withLine.getMessage());
    }

    @Test
    public void emptyExpressionIsRejectedWhenTheActionNeedsOneTest() {
        var action = new ActionDto("ASSUME", List.of(), Map.of("expression", ""));
        var exception = assertThrows(InvalidActionException.class, () -> ActionExpression.of(action, DESCRIPTORS));
        assertEquals("ASSUME needs an expression", exception.getMessage());
    }

    @Test
    public void whitespaceOnlyExpressionIsRejectedWhenTheActionNeedsOneTest() {
        var action = new ActionDto("ORI1", List.of(1), Map.of("expression", "   "));
        var exception = assertThrows(InvalidActionException.class, () -> ActionExpression.of(action, DESCRIPTORS));
        assertEquals("ORI1 needs an expression", exception.getMessage());
    }

    @Test
    public void presentExpressionIsReturnedTest() {
        assertEquals(Optional.of("P -> Q"), ActionExpression.of(new ActionDto("ASSUME", List.of(), Map.of("expression", "P -> Q")), DESCRIPTORS));
        assertEquals(Optional.of(" P "), ActionExpression.of(new ActionDto("ORI1", List.of(1), Map.of("expression", " P ")), DESCRIPTORS));
    }

    @Test
    public void expressionIsOptionalWhenTheActionDoesNotTakeOneTest() {
        for (var name : List.of("COPY", "DT")) {
            assertEquals(Optional.empty(), ActionExpression.of(new ActionDto(name, List.of(1), Map.of()), DESCRIPTORS));
            assertEquals(Optional.empty(), ActionExpression.of(new ActionDto(name, List.of(1), Map.of("expression", "")), DESCRIPTORS));
            assertEquals(Optional.empty(), ActionExpression.of(new ActionDto(name, List.of(1), Map.of("expression", "  ")), DESCRIPTORS));
        }
        assertEquals(Optional.of("P"), ActionExpression.of(new ActionDto("COPY", List.of(1), Map.of("expression", "P")), DESCRIPTORS));
    }

    @Test
    public void unknownActionDoesNotNeedAnExpressionTest() {
        assertEquals(Optional.empty(), ActionExpression.of(new ActionDto("NOPE", List.of(), Map.of()), DESCRIPTORS));
        assertEquals(Optional.empty(), ActionExpression.of(new ActionDto("ASSUME", List.of(), Map.of("expression", "")), List.of()));
    }
}
