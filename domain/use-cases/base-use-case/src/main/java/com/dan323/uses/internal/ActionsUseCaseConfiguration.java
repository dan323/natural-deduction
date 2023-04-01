package com.dan323.uses.internal;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.uses.ActionsUseCases;
import com.dan323.uses.LogicalApplyAction;
import com.dan323.uses.LogicalGetActions;
import com.dan323.uses.LogicalSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Configuration
public class ActionsUseCaseConfiguration {

    @Bean
    public ActionsUseCases useCases(List<LogicalGetActions> getActions, List<LogicalSolver> solvers) {

        Map<String, ActionsUseCases.GetActions> actionGetters;
        Map<String, ActionsUseCases.Solve> problemSolvers;

        actionGetters = getActions.stream()
                .map(logicalGetActions -> new AbstractMap.SimpleEntry<>(logicalGetActions.getLogicName(), logicalGetActions))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        problemSolvers = solvers.stream()
                .map(logicalGetActions -> new AbstractMap.SimpleEntry<>(logicalGetActions.getLogicName(), logicalGetActions))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return new ActionsUseCases() {

            @Override
            public GetActions getActions(String logicName) {
                return Optional.ofNullable(actionGetters.get(logicName)).orElseThrow(IllegalArgumentException::new);
            }

            @Override
            public <A extends Action<T, Q, P>, T extends LogicOperation, Q extends ProofStep<T>, P extends Proof<T, Q>> ApplyAction<A,T, Q, P> applyAction(String logicName) {
                return new LogicalApplyAction<>();
            }

            @Override
            public <T extends LogicOperation, Q extends ProofStep<T>, P extends Proof<T, Q>> Solve<T, Q, P> solveProblem(String logicName) {
                return Optional.ofNullable(problemSolvers.get(logicName)).orElseThrow(IllegalArgumentException::new);
            }
        };
    }
}
