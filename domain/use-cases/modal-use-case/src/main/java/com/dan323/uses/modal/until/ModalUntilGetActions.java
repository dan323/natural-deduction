package com.dan323.uses.modal.until;

import com.dan323.model.ActionDescriptorDto;
import com.dan323.uses.LogicalGetActions;
import com.dan323.uses.modal.ModalGetActions;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * The actions of modal logic with Until: those of modal logic, with the same descriptions, then one per
 * {@link AvailableUntilAction}. Built once.
 */
public class ModalUntilGetActions implements LogicalGetActions {

    private final List<ActionDescriptorDto> actions = Stream.concat(new ModalGetActions().perform().stream(),
                    Arrays.stream(AvailableUntilAction.values()).map(AvailableUntilAction::descriptor))
            .toList();

    @Override
    public List<ActionDescriptorDto> perform() {
        return actions;
    }

    @Override
    public String getLogicName() {
        return ModalUntilConfiguration.LOGIC;
    }
}
