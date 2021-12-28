package com.dan323.bean;

import com.dan323.GetAllActionsUseCase;
import com.dan323.primaryports.LogicUseCases;
import com.dan323.primaryports.Proof;
import com.dan323.secondaryports.RuleDao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.Serializable;

@Configuration
public class DomainPorts {

    @Bean
    public LogicUseCases logicUseCases(RuleDao ruleDao){
        return new LogicUseCases() {
            @Override
            public GetAllActions getAllActions(String logic) {
                return new GetAllActionsUseCase(logic, ruleDao);
            }

            @Override
            public <Q extends Serializable> SaveRule<Q> saveRule(String logic, Proof<Q> proof) {
                return null;
            }
        };
    }
}
