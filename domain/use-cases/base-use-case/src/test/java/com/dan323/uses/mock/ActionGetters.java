package com.dan323.uses.mock;

import com.dan323.uses.LogicalGetActions;

import java.util.List;
import java.util.stream.Stream;

public final class ActionGetters {

    public static List<LogicalGetActions> actionsList() {
        return List.of(actions("l1"), actions("l2"));
    }

    private static LogicalGetActions actions(String logic) {
        return new LogicalGetActions() {
            @Override
            public String getLogicName() {
                return logic;
            }

            @Override
            public List<String> perform() {
                return Stream.of("A1", "A2", "A3").map(st -> logic + "." + st).toList();
            }
        };
    }
}
