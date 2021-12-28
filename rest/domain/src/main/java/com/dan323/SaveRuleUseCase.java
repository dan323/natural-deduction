package com.dan323;

import com.dan323.exception.InvalidProofException;
import com.dan323.primaryports.LogicUseCases;
import com.dan323.primaryports.Proof;
import com.dan323.primaryports.Rule;
import com.dan323.primaryports.Step;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.proof.ParseAction;
import com.dan323.secondaryports.RuleDao;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SaveRuleUseCase<Q extends Serializable> implements LogicUseCases.SaveRule<Q> {

    private final String logic;
    private final Proof<Q> proof;
    private final String name;
    private final RuleDao ruleDao;
    private final ApplicationContext applicationContext;

    SaveRuleUseCase(String logic, Proof<Q> proof, String name, RuleDao ruleDao, ApplicationContext applicationContext){
        this.logic = logic;
        this.proof = proof;
        this.name = name;
        this.ruleDao = ruleDao;
        this.applicationContext = applicationContext;
    }

    @Override
    public void perform() throws InvalidProofException {
        if (validate()){
            save();
        } else {
            throw new InvalidProofException();
        }
    }

    private void save(){
        ruleDao.saveRule(logic, new Rule(extractPremises(proof),proof.goal(),name));
    }

    private boolean validate(){
        /*ParseAction<Action<?,?,com.dan323.proof.generic.proof.Proof<?,?>>,com.dan323.proof.generic.proof.Proof<?,?>>*/
        var parseAction = applicationContext.getBeansOfType(ParseAction.class).get(logic);
        /*List<Action<?,?,com.dan323.proof.generic.proof.Proof<?,?>>>*/
        var actionList = parseAction.translateToActions(proof.toInternalProof());
        //Go through assms
        //Apply actions
        //return true if all actions applied, false otherwise
        return false;
    }

    private List<String> extractPremises(Proof<Q> proof){
        return proof.steps()
                .stream()
                .takeWhile(qStep -> qStep.assmsLevel() == 0)
                .takeWhile(qStep -> qStep.ruleString().contains("Ass"))
                .map(Step::expression)
                .collect(Collectors.toList());
    }
}
