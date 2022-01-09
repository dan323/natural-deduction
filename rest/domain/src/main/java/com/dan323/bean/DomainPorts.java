package com.dan323.bean;

import com.dan323.ApplyRuleUseCase;
import com.dan323.GetAllActionsUseCase;
import com.dan323.IsDoneUseCase;
import com.dan323.SaveRuleUseCase;
import com.dan323.expressions.base.LogicOperation;
import com.dan323.primaryports.*;
import com.dan323.primaryports.Input;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.bean.*;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.secondaryports.RuleDao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.Serializable;
import java.util.List;

@Configuration
public class DomainPorts {

    @Bean
    public LogicUseCases logicUseCases(RuleDao ruleDao, List<ToModel<?,?,?>> toModels, List<NaturalDeductionFactory<?,?,?,?,?>> naturalDeductionFactories, List<ToGoalParser<?,?,?>> toGoalParsers, List<ToAssmParser<?,?,?>> toAssmsParsers, List<ParseAction<?,?>> parseActions, List<Actions<?,?,?>> actions, List<ExpressionParser<?>> parsers){

        return new LogicUseCases() {
            @Override
            public GetAllActions getAllActions(String logic) {
                return new GetAllActionsUseCase(logic, ruleDao);
            }

            @Override
            public <Q extends Serializable, C extends Action<L,S,P>, S extends ProofStep<L>, L extends LogicOperation, A, G, P extends com.dan323.proof.generic.proof.Proof<L, A, G, S>> ApplyRule<Q,S,L,A,G,P> applyRule(String logic, Proof<P, A, G, S, Q, L> proof, String ruleName, Input<Q> inputs) {
                NaturalDeductionFactory<P,A,G,L,S> naturalDeductionFactory = (NaturalDeductionFactory<P,A,G,L,S>)naturalDeductionFactories.stream().filter(nat -> nat.ofLogic().equals(logic)).findAny().get();
                ToGoalParser<Goal<Q>,Q,G> toGoalParser = (ToGoalParser<Goal<Q>,Q, G>) toGoalParsers.stream().filter(goal -> goal.ofLogic().equals(logic)).findAny().get();
                ToAssmParser<Assumption<Q>,Q,A> toAssmParser = (ToAssmParser<Assumption<Q>,Q, A>) toAssmsParsers.stream().filter(goal -> goal.ofLogic().equals(logic)).findAny().get();
                ToModel<Q, S, L> toModel = (ToModel<Q, S, L>) toModels.stream().filter(mod -> mod.ofLogic().equals(logic)).findAny().get();
                Actions<Input<Q>,L,C> actionList = (Actions<Input<Q>,L,C>)actions.stream().filter(mod -> mod.ofLogic().equals(logic)).findAny().get();
                ExpressionParser<L> parser = (ExpressionParser<L>) parsers.stream().filter(mod -> mod.ofLogic().equals(logic)).findAny().get();

                return new ApplyRuleUseCase<>(logic, ruleName, proof, ruleDao, naturalDeductionFactory, toGoalParser, toAssmParser, toModel, actionList, inputs, parser);
            }

            @Override
            public <Q extends Serializable, C extends Action<L, S, P>, S extends ProofStep<L>, L extends LogicOperation, A, G, P extends com.dan323.proof.generic.proof.Proof<L, A, G, S>> IsDone isDone(String logic, Proof<P, A, G, S, Q, L> proof) {
                NaturalDeductionFactory<P,A,G,L,S> naturalDeductionFactory = (NaturalDeductionFactory<P,A,G,L,S>)naturalDeductionFactories.stream().filter(nat -> nat.ofLogic().equals(logic)).findAny().get();
                ToGoalParser<Goal<Q>,Q,G> toGoalParser = (ToGoalParser<Goal<Q>,Q, G>) toGoalParsers.stream().filter(goal -> goal.ofLogic().equals(logic)).findAny().get();
                ToAssmParser<Assumption<Q>,Q,A> toAssmParser = (ToAssmParser<Assumption<Q>,Q, A>) toAssmsParsers.stream().filter(goal -> goal.ofLogic().equals(logic)).findAny().get();
                ToModel<Q, S, L> toModel = (ToModel<Q, S, L>) toModels.stream().filter(mod -> mod.ofLogic().equals(logic)).findAny().get();
                Actions<Input<Q>,L,C> actionList = (Actions<Input<Q>,L,C>)actions.stream().filter(mod -> mod.ofLogic().equals(logic)).findAny().get();
                ParseAction<C,P> parseAction = (ParseAction<C,P>) parseActions.stream().filter(goal -> goal.ofLogic().equals(logic)).findAny().get();

                return new IsDoneUseCase<>(proof,naturalDeductionFactory,toAssmParser,toGoalParser,toModel,parseAction);
            }

            @Override
            public <Q extends Serializable, C extends Action<L,S,P>, S extends ProofStep<L>, L extends LogicOperation, A, G, P extends com.dan323.proof.generic.proof.Proof<L,A,G,S>> SaveRule saveRule(String logic, Proof<P, A, G, S, Q, L>  proof) {
                NaturalDeductionFactory<P,A,G,L,S> naturalDeductionFactory = (NaturalDeductionFactory<P,A,G,L,S>)naturalDeductionFactories.stream().filter(nat -> nat.ofLogic().equals(logic)).findAny().get();
                ToGoalParser<Goal<Q>,Q,G> toGoalParser = (ToGoalParser<Goal<Q>,Q, G>) toGoalParsers.stream().filter(goal -> goal.ofLogic().equals(logic)).findAny().get();
                ToAssmParser<Assumption<Q>,Q,A> toAssmParser = (ToAssmParser<Assumption<Q>,Q, A>) toAssmsParsers.stream().filter(goal -> goal.ofLogic().equals(logic)).findAny().get();
                ParseAction<C,P> parseAction = (ParseAction<C,P>) parseActions.stream().filter(goal -> goal.ofLogic().equals(logic)).findAny().get();
                ToModel<Q, S, L> toModel = (ToModel<Q, S, L>) toModels.stream().filter(mod -> mod.ofLogic().equals(logic)).findAny().get();

                return new SaveRuleUseCase<>(logic, proof, ruleDao, parseAction, naturalDeductionFactory, toGoalParser, toAssmParser, toModel);
            }
        };
    }
}
