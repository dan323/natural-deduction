package com.dan323.secondaryports;

import com.dan323.primaryports.Rule;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RuleDao {

    <T> Mono<Rule<T>> getRule(String logic, String ruleName, Class<T> tClass);

    Flux<String> getPossibleActions(String logic);

    <T> Mono<Boolean> saveRule(String logic, Rule<T> rule);

}
