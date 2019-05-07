package com.dan323.expresions;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public interface LogicOperation {

    static <T extends LogicOperation> T construct(Function<List<LogicOperation>, T> creator, LogicOperation... logicOperations) {
        return creator.apply(Arrays.asList(logicOperations));
    }

    String toString();

}
