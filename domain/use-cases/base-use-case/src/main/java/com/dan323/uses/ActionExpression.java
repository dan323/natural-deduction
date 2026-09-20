package com.dan323.uses;

import com.dan323.model.ActionDescriptorDto;
import com.dan323.model.ActionDto;
import com.dan323.model.ParamKind;

import java.util.List;
import java.util.Optional;

/**
 * Reads the {@code expression} of an {@link ActionDto}, so that every logic treats a missing or blank one the same way.
 */
public final class ActionExpression {

    private ActionExpression() {
    }

    /**
     * @param action      what the client asked for
     * @param descriptors the actions of the logic, to know whether {@code action} takes an expression
     * @return the expression text, empty when there is none and the action does not need it
     * @throws InvalidActionException when the action needs an expression and there is none, or it is blank
     */
    public static Optional<String> of(ActionDto action, List<ActionDescriptorDto> descriptors) {
        var expression = Optional.ofNullable(action.extraParameters().get("expression")).filter(text -> !text.isBlank());
        if (expression.isEmpty() && needsExpression(action.name(), descriptors)) {
            throw new InvalidActionException(action.name() + " needs an expression");
        }
        return expression;
    }

    private static boolean needsExpression(String name, List<ActionDescriptorDto> descriptors) {
        return descriptors.stream()
                .anyMatch(descriptor -> descriptor.name().equals(name) && descriptor.params().contains(ParamKind.EXPRESSION));
    }
}
