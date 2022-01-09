package com.dan323.adapter;

import com.dan323.primaryports.Rule;
import com.dan323.proof.generic.bean.Actions;
import com.dan323.proof.generic.bean.Construct;
import com.dan323.proof.generic.bean.Input;
import com.dan323.secondaryports.RuleDao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class LogicAdapters {

    @Bean
    public PrimitiveRuleDao primitiveRuleDao(List<Actions<Input<?>,?,?>> actionsList) {
        Map<String, List<String>> actionsMap = actionsList
                .stream()
                .collect(Collectors.toMap(Actions::ofLogic, Actions::get));
        Map<String, Function<String,Construct<?,?,?>>> actions2Map = actionsList
                .stream()
                .collect(Collectors.toMap(Actions::ofLogic, act -> act::getAction));
        return new PrimitiveRuleDao(actionsMap, actions2Map);
    }

    @Bean
    public CustomRuleDao customRuleDao() {
        return new CustomRuleDao();
    }

    @Bean
    public RuleDao ruleDao(CustomRuleDao customRuleDao, PrimitiveRuleDao primitiveRuleDao) {
        return new RuleDao() {
            @Override
            public <T> Mono<Rule<T>> getRule(String logic, String ruleName, Class<T> tClass) {
                return primitiveRuleDao.<T>getRule(logic,ruleName).switchIfEmpty(customRuleDao.readRule(logic, ruleName, tClass));
            }

            @Override
            public Flux<String> getPossibleActions(String logic) {
                return primitiveRuleDao.getRules(logic).concatWith(customRuleDao.getRuleNames(logic));
            }

            @Override
            public <T> Mono<Boolean> saveRule(String logic, Rule<T> rule) {
                return customRuleDao.saveRule(logic, rule);
            }
        };
    }
}
