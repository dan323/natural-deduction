package com.dan323.adapter;

import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

public class PrimitiveRuleDao {

    private final Map<String, List<String>> actionsByLogic;

    PrimitiveRuleDao(Map<String, List<String>> actionsByLogic) {
        this.actionsByLogic = actionsByLogic;
    }

    public Flux<String> getRules(String logic) {
        if (actionsByLogic.containsKey(logic)) {
            return Flux.fromIterable(actionsByLogic.get(logic));
        } else {
            return Flux.empty();
        }
    }
}
