package com.dan323.uses.modal;

import com.dan323.proof.modal.AbstractModalAction;
import com.dan323.uses.LogicalGetActions;
import org.reflections.Reflections;

import java.util.List;
import java.util.stream.Collectors;

public class ModalGetActions implements LogicalGetActions {
    @Override
    public List<String> perform() {
        var reflections = new Reflections("com.dan323.proof.modal");
        return reflections.getSubTypesOf(AbstractModalAction.class).stream()
                .map(Class::getSimpleName)
                .filter(st -> !st.contains("Action"))
                .collect(Collectors.toList());
    }

    @Override
    public String getLogicName() {
        return "modal";
    }
}
