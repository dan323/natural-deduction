package com.dan323.uses.modal;

import com.dan323.proof.modal.AbstractModalAction;
import com.dan323.uses.LogicalGetActions;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class ModalGetActions implements LogicalGetActions {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModalGetActions.class);

    @Override
    public List<String> perform() {
        LOGGER.debug("Getting actions for modal logic.");
        var reflections = new Reflections("com.dan323.proof.modal");
        return reflections.getSubTypesOf(AbstractModalAction.class).stream()
                .filter(clazz -> !clazz.getName().contains("complex"))
                .map(Class::getSimpleName)
                .filter(st -> !st.contains("Action"))
                .collect(Collectors.toList());
    }

    @Override
    public String getLogicName() {
        return "modal";
    }
}
