package com.dan323.uses.internal;

import com.dan323.uses.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class ActionsUseCaseConfiguration {

    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ActionsUseCases useCases(List<LogicalGetActions> getActions, List<Transformer> transformers, List<ProofParser> parsers) {

        Map<String, Transformer> transformerMap = transformers.stream()
                .collect(Collectors.toMap(Transformer::logic, Function.identity()));
        Map<String, ActionsUseCases.GetActions> actionGetters = getActions.stream()
                .collect(Collectors.toMap(LogicalGetActions::getLogicName, Function.identity()));
        Map<String, ActionsUseCases.ApplyAction> appliers = transformerMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> new LogicalApplyAction(entry.getValue())));
        Map<String, ActionsUseCases.Solve> solvers = transformerMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> new LogicalSolver(entry.getValue())));
        Map<String, ActionsUseCases.ParseProof> parserMap = parsers.stream()
                .collect(Collectors.toMap(ProofParser::logic, parser -> (ActionsUseCases.ParseProof) proof -> transformerMap.get(parser.logic()).fromProof(parser.parseProof(proof))));

        return new ActionsUseCases() {

            @Override
            public GetActions getActions(String logicName) {
                return lookup(actionGetters, logicName);
            }

            @Override
            public ApplyAction applyAction(String logicName) {
                return lookup(appliers, logicName);
            }

            @Override
            public Solve solveProblem(String logicName) {
                return lookup(solvers, logicName);
            }

            @Override
            public ParseProof parseToProof(String logic) {
                return lookup(parserMap, logic);
            }
        };
    }

    private static <V> V lookup(Map<String, V> map, String logic) {
        var value = map.get(logic);
        if (value == null) {
            throw new UnknownLogicException(logic);
        }
        return value;
    }
}
