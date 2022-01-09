package com.dan323.adapter;

import com.dan323.primaryports.Rule;
import com.dan323.proof.generic.bean.Construct;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PrimitiveRuleDao {

    private final Map<String, List<String>> actionsByLogic;
    private final Map<String, Function<String,Construct<?,?,?>>> rulesByLogic;

    PrimitiveRuleDao(Map<String, List<String>> actionsByLogic, Map<String, Function<String, Construct<?,?,?>>> rulesByLogic) {
        this.actionsByLogic = actionsByLogic;
        this.rulesByLogic = rulesByLogic;
    }

    public Flux<String> getRules(String logic) {
        if (actionsByLogic.containsKey(logic)) {
            return Flux.fromIterable(actionsByLogic.get(logic));
        } else {
            return Flux.empty();
        }
    }

    public <T> Mono<Rule<T>> getRule(String logic, String ruleName){
        if (rulesByLogic.containsKey(logic)){
            try {
                var k = rulesByLogic.get(logic).apply(ruleName);
                return Mono.just(new Rule<>(List.of(), null, ruleName));
            } catch (Exception e){
                return Mono.empty();
            }
        } else {
            return Mono.empty();
        }
    }
}
