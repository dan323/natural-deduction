package com.dan323.proof;


import com.dan323.expresions.LogicOperation;

import java.util.Map;

@FunctionalInterface
public interface PatternMapper {

    Map<String, LogicOperation> compareLogic(LogicOperation l1, LogicOperation l2);

}
