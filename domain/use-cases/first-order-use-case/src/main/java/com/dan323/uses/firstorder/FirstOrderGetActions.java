package com.dan323.uses.firstorder;

import com.dan323.model.ActionDescriptorDto;
import com.dan323.uses.LogicalGetActions;

import java.util.Arrays;
import java.util.List;

/**
 * The actions of first-order logic: one per {@link AvailableFirstOrderAction}, built once.
 */
public class FirstOrderGetActions implements LogicalGetActions {

    private final List<ActionDescriptorDto> actions = Arrays.stream(AvailableFirstOrderAction.values())
            .map(AvailableFirstOrderAction::descriptor)
            .toList();

    @Override
    public List<ActionDescriptorDto> perform() {
        return actions;
    }

    @Override
    public String getLogicName() {
        return FirstOrderConfiguration.LOGIC;
    }
}
