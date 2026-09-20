package com.dan323.uses.modal;

import com.dan323.model.ActionDescriptorDto;
import com.dan323.uses.LogicalGetActions;

import java.util.Arrays;
import java.util.List;

/**
 * The actions of modal logic: one per {@link AvailableModalAction}, built once.
 */
public class ModalGetActions implements LogicalGetActions {

    private final List<ActionDescriptorDto> actions = Arrays.stream(AvailableModalAction.values())
            .map(AvailableModalAction::descriptor)
            .toList();

    @Override
    public List<ActionDescriptorDto> perform() {
        return actions;
    }

    @Override
    public String getLogicName() {
        return "modal";
    }
}
