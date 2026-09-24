package com.dan323.uses.modal.nextuntil;

import com.dan323.model.ActionDescriptorDto;
import com.dan323.uses.LogicalGetActions;
import com.dan323.uses.modal.ModalGetActions;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * The actions of {@code modal-next-until}: those of modal logic, with the same descriptions, then one per
 * {@link AvailableNextUntilAction}. Built once.
 */
public class ModalNextUntilGetActions implements LogicalGetActions {

    private final List<ActionDescriptorDto> actions = Stream.concat(new ModalGetActions().perform().stream(),
                    Arrays.stream(AvailableNextUntilAction.values()).map(AvailableNextUntilAction::descriptor))
            .toList();

    @Override
    public List<ActionDescriptorDto> perform() {
        return actions;
    }

    @Override
    public String getLogicName() {
        return ModalNextUntilConfiguration.LOGIC;
    }
}
