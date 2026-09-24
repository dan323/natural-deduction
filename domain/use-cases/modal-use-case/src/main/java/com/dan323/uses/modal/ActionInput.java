package com.dan323.uses.modal;

import com.dan323.model.ActionCategory;
import com.dan323.model.ActionDescriptorDto;
import com.dan323.model.ParamKind;

import java.util.List;

/**
 * An input of a modal action: what kind of value it is, and how to call it. The action enums of modal logic and of
 * the logics that extend it describe their actions with these.
 *
 * @param kind  the kind of value
 * @param label how the client names the input
 */
public record ActionInput(ParamKind kind, String label) {

    /** A line of the proof (1-based). */
    public static ActionInput line(String label) {
        return new ActionInput(ParamKind.INT, label);
    }

    /** A formula typed by the user. */
    public static ActionInput formula(String label) {
        return new ActionInput(ParamKind.EXPRESSION, label);
    }

    /** A state, e.g. {@code s1}. */
    public static ActionInput state(String label) {
        return new ActionInput(ParamKind.STATE, label);
    }

    /**
     * @return the descriptor of an action with these inputs, in the order the action reads them
     */
    public static ActionDescriptorDto describe(String ruleName, String label, String symbol, ActionCategory category,
                                               String description, ActionInput... inputs) {
        var inputList = List.of(inputs);
        return new ActionDescriptorDto(ruleName, inputList.stream().map(ActionInput::kind).toList(), label, symbol,
                category, description, inputList.stream().map(ActionInput::label).toList());
    }
}
