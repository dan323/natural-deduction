package com.dan323.uses.intuitionistic;

import com.dan323.classical.proof.AvailableAction;
import com.dan323.model.ActionDescriptorDto;
import com.dan323.uses.LogicalGetActions;
import com.dan323.uses.classical.ClassicGetActions;

import java.util.List;

/**
 * The actions of intuitionistic logic: the classical ones, with the same descriptions, except those for which
 * {@link AvailableAction#isIntuitionistic()} is false.
 */
public class IntuitionisticGetActions implements LogicalGetActions {

    private final List<ActionDescriptorDto> actions = new ClassicGetActions().perform().stream()
            .filter(action -> AvailableAction.valueOf(action.name()).isIntuitionistic())
            .toList();

    @Override
    public List<ActionDescriptorDto> perform() {
        return actions;
    }

    @Override
    public String getLogicName() {
        return IntuitionisticRules.LOGIC;
    }
}
