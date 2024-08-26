package com.dan323.uses.classical;

import com.dan323.classical.ClassicalAction;
import com.dan323.classical.proof.AvailableAction;
import com.dan323.uses.LogicalGetActions;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClassicGetActions implements LogicalGetActions {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassicGetActions.class);

    @Override
    public List<String> perform() {
        LOGGER.debug("Getting actions for classical logic.");
        Reflections reflections = new Reflections("com.dan323.classical");
        return
                reflections.getSubTypesOf(ClassicalAction.class)
                        .stream()
                        .filter(clazz -> !clazz.getName().contains("complex"))
                        .map(clazz -> clazz.getConstructors()[0])
                        .map(cons -> Arrays.stream(AvailableAction.values()).filter(action -> action.getActionName()
                                .equals(cons.getDeclaringClass().getSimpleName())).findFirst().orElseThrow()
                                + "(" + Arrays.stream(cons.getParameters())
                                .map(Parameter::getParameterizedType).map(Type::getTypeName)
                                .map(name -> {
                                    if (name.contains("Classical")) {
                                        return "expression";
                                    } else {
                                        return name;
                                    }
                                }).toList() + ")")
                        .collect(Collectors.toList());
    }

    @Override
    public String getLogicName() {
        return "classical";
    }
}
