package com.dan323;

import com.dan323.primaryports.LogicUseCases;
import com.dan323.primaryports.Rule;
import com.dan323.secondaryports.RuleDao;
import reactor.core.publisher.Flux;

public class GetAllActionsUseCase implements LogicUseCases.GetAllActions {

    private final String logic;
    private final RuleDao ruleDao;

    public GetAllActionsUseCase(String logic, RuleDao ruleDao){
        this.logic = logic;
        this.ruleDao = ruleDao;
    }

    @Override
    public Flux<String> perform() {
        return ruleDao.getPossibleActions(logic);
    }

}
