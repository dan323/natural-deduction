package com.dan323.adapter;

import com.dan323.primaryports.Rule;
import com.dan323.proof.generic.bean.Construct;
import com.dan323.proof.generic.bean.Input;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PrimitiveRuleDao {

    private final Map<String, List<Construct<Input<?>,?,?>>> actionsByLogic;
    private final Map<String, Function<String,Construct<?,?,?>>> rulesByLogic;

    PrimitiveRuleDao(Map<String, List<Construct<Input<?>,?,?>>> actionsByLogic, Map<String, Function<String, Construct<?,?,?>>> rulesByLogic) {
        this.actionsByLogic = actionsByLogic;
        this.rulesByLogic = rulesByLogic;
    }

    public <T> Flux<Rule<T>> getRules(String logic) {
        if (actionsByLogic.containsKey(logic)) {
            return Flux.fromIterable(actionsByLogic.get(logic)).map(Rule::toRule);
        } else {
            return Flux.empty();
        }
    }

    public <T> Mono<Rule<T>> getRule(String logic, String ruleName){
        if (rulesByLogic.containsKey(logic)){
            try {
                return Mono.just(rulesByLogic.get(logic).apply(ruleName)).map(Rule::toRule);
            } catch (Exception e){
                return Mono.empty();
            }
        } else {
            return Mono.empty();
        }
    }
}
