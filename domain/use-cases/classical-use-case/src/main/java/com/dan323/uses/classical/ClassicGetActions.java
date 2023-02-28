package com.dan323.uses.classical;

import com.dan323.classical.ClassicalAction;
import com.dan323.uses.LogicalGetActions;
import org.reflections.Reflections;

import java.util.List;
import java.util.stream.Collectors;

public class ClassicGetActions implements LogicalGetActions {
    @Override
    public List<String> perform() {
        Reflections reflections = new Reflections("com.dan323.classical");
        return reflections.getSubTypesOf(ClassicalAction.class)
                .stream()
                .filter(clazz -> !clazz.getName().contains("complex"))
                .map(Class::getSimpleName)
                .collect(Collectors.toList());
    }

    @Override
    public String getLogicName() {
        return "classical";
    }
}
