package com.dan323.uses.internal;

import com.dan323.uses.ProofParser;
import com.dan323.uses.*;
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
    public ActionsUseCases useCases(List<LogicalGetActions> getActions, List<LogicalSolver> solvers, List<Transformer> transformers, List<ProofParser> parsers) {

        Map<String, ActionsUseCases.GetActions> actionGetters;
        Map<String, ActionsUseCases.Solve> problemSolvers;
        Map<String, ActionsUseCases.ParseProof> parserMap;
        Map<String, Transformer> transformerMap;

        transformerMap = transformers.stream().map(transformer -> new AbstractMap.SimpleEntry<>(transformer.logic(), transformer))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        actionGetters = getActions.stream()
                .map(logicalGetActions -> new AbstractMap.SimpleEntry<>(logicalGetActions.getLogicName(), logicalGetActions))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        problemSolvers = solvers.stream()
                .map(logicalGetActions -> new AbstractMap.SimpleEntry<>(logicalGetActions.getLogicName(), logicalGetActions))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        parserMap = parsers.stream()
                .map(parser -> new AbstractMap.SimpleEntry<>(parser.logic(), (ActionsUseCases.ParseProof) (String proof) -> transformerMap.get(parser.logic()).fromProof(parser.parseProof(proof))))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return new ActionsUseCases() {

            @Override
            public GetActions getActions(String logicName) {
                return Optional.ofNullable(actionGetters.get(logicName)).orElseThrow(IllegalArgumentException::new);
            }

            @Override
            public ApplyAction applyAction(String logicName) {
                return new LogicalApplyAction(transformers.stream().filter(transformer -> transformer.logic().equals(logicName)).findFirst().orElseThrow());
            }

            @Override
            public Solve solveProblem(String logicName) {
                return Optional.ofNullable(problemSolvers.get(logicName)).orElseThrow(IllegalArgumentException::new);
            }

            @Override
            public ParseProof parseToProof(String logic) {
                return parserMap.get(logic);
            }
        };
    }
}
