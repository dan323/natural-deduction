package com.dan323.uses.internal;

import com.dan323.uses.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class ActionsUseCaseConfiguration {

    /**
     * How long a solve may take before it is abandoned, see {@link LogicalSolver}. Set with
     * {@code natural-deduction.solve-timeout}, e.g. {@code 5s} or {@code PT5S}.
     */
    @Value("${natural-deduction.solve-timeout:10s}")
    private Duration solveTimeout = Duration.ofSeconds(10);

    // Every logic brings its own Transformer/ProofParser type arguments, so the beans are collected raw and matched
    // per logic name; each logic module's own tests check that its pieces fit together.
    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ActionsUseCases useCases(List<LogicalGetActions> getActions, List<Transformer> transformers, List<ProofParser> parsers,
                                    List<LogicalExercises> exercises) {

        Map<String, Transformer> transformerMap = transformers.stream()
                .collect(Collectors.toMap(Transformer::logic, Function.identity()));
        Map<String, ActionsUseCases.GetActions> actionGetters = getActions.stream()
                .collect(Collectors.toMap(LogicalGetActions::getLogicName, Function.identity()));
        Map<String, ActionsUseCases.ApplyAction> appliers = transformerMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> new LogicalApplyAction(entry.getValue())));
        Map<String, ActionsUseCases.Solve> solvers = transformerMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> new LogicalSolver(entry.getValue(), solveTimeout)));
        Map<String, ActionsUseCases.ParseProof> parserMap = parsers.stream()
                .collect(Collectors.toMap(ProofParser::logic, parser -> (ActionsUseCases.ParseProof) proof -> transformerMap.get(parser.logic()).fromProof(parser.parseProof(proof))));

        // A logic without a catalog has no exercises, but it is still a known logic (not a 404).
        Set<String> knownLogics = new HashSet<>(transformerMap.keySet());
        knownLogics.addAll(actionGetters.keySet());
        knownLogics.addAll(parserMap.keySet());
        // A catalog only makes sense for a logic the other use cases serve; otherwise its exercises would be listed
        // for a logic whose actions, proofs and solver all answer 404.
        exercises.stream()
                .map(LogicalExercises::logic)
                .filter(logic -> !knownLogics.contains(logic))
                .findFirst()
                .ifPresent(logic -> {
                    throw new IllegalStateException("Exercise catalog for unknown logic '" + logic + "'");
                });
        Map<String, ActionsUseCases.GetExercises> exerciseMap = new HashMap<>(exercises.stream()
                .collect(Collectors.toMap(LogicalExercises::logic, catalog -> {
                    var dtos = catalog.exercises().stream().map(Exercise::toDto).toList();
                    return (ActionsUseCases.GetExercises) () -> dtos;
                })));
        knownLogics.forEach(logic -> exerciseMap.putIfAbsent(logic, List::of));

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

            @Override
            public GetExercises getExercises(String logicName) {
                return lookup(exerciseMap, logicName);
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
