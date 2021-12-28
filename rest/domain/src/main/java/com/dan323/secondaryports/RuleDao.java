package com.dan323.secondaryports;

import com.dan323.primaryports.Rule;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RuleDao {

    Mono<Rule> getRule(String logic);

    Flux<String> getPossibleActions(String logic);

    Mono<Boolean> saveRule(String logic, Rule rule);

}
