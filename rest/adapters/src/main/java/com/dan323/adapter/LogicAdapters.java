package com.dan323.adapter;

import com.dan323.primaryports.Rule;
import com.dan323.proof.generic.bean.Actions;
import com.dan323.secondaryports.RuleDao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class LogicAdapters {

    @Bean
    public PrimitiveRuleDao primitiveRuleDao(List<Actions> actionsList) {
        Map<String, List<String>> actionsMap = actionsList
                .stream()
                .collect(Collectors.toMap(Actions::ofLogic, Actions::get));
        return new PrimitiveRuleDao(actionsMap);
    }

    @Bean
    public CustomRuleDao customRuleDao() {
        return new CustomRuleDao();
    }

    @Bean
    public RuleDao ruleDao(CustomRuleDao customRuleDao, PrimitiveRuleDao primitiveRuleDao) {
        return new RuleDao() {
            @Override
            public Mono<Rule> getRule(String logic) {
                return null;
            }

            @Override
            public Flux<String> getPossibleActions(String logic) {
                return primitiveRuleDao.getRules(logic).concatWith(customRuleDao.getRuleNames(logic));
            }

            @Override
            public Mono<Boolean> saveRule(String logic, Rule rule) {
                return customRuleDao.saveRule(logic, rule);
            }
        };
    }
}
