package com.dan323.adapter;

import com.dan323.primaryports.Assumption;
import com.dan323.primaryports.Goal;
import com.dan323.primaryports.Rule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.converter.json.Jackson2ObjectMapperFactoryBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomRuleDao {

    public <T> Flux<Rule<T>> getRuleNames(String logic) {
        Path ruleList = Paths.get(logic + "/rules/list.txt");

        if (Files.exists(ruleList)) {
            return Flux.using(
                    () -> Files.lines(ruleList),
                    Flux::fromStream,
                    Stream::close
            ).flatMap(ruleName -> readRule(logic, ruleName));
        } else {
            return Flux.empty();
        }
    }

    public <T> Mono<Boolean> saveRule(String logic, Rule<T> rule) {
        return Mono.<PairData>create(sink -> {
            Flux.fromArray(new PairData[]{new PairData(RuleSubscriber.Action.LIST, rule.ruleName()), new PairData(RuleSubscriber.Action.RULE, rule.toString())})
                    .subscribe(new RuleSubscriber(logic, sink));
        }).thenReturn(true).onErrorReturn(false);
    }

    public <T> Mono<Rule<T>> readRule(String logic, String ruleName) {
        Path ruleList = Paths.get(logic + "/rules/" + ruleName + ".txt");
        if (Files.exists(ruleList)) {
            return Flux.using(
                    () -> Files.lines(ruleList),
                    Flux::fromStream,
                    Stream::close
            ).collect(new RuleCollector<>());
        } else {
            return Mono.empty();
        }
    }

    private static class DataRule<T> {

        private List<DataAssms<T>> assms;
        private DataGoal<T> goal;
        private String name;

        public void add(DataAssms<T> assms){
            this.assms.add(assms);
        }

        public void setGoal(DataGoal<T> goal){
            this.goal = goal;
        }

        public void setName(String name){
            this.name = name;
        }

        public DataGoal<T> getGoal() {
            return goal;
        }

        public String getName() {
            return name;
        }

        public List<DataAssms<T>> getAssms(){
            return assms;
        }
    }

    private static class DataGoal<T> {
        private final String expression;
        private T extraInformation;

        public DataGoal(String input, Class<T> tClass){
            String[] split = input.split("#@#");
            this.expression = split[0];
            var mapper = new Jackson2ObjectMapperFactoryBean().getObject();
            try {
                this.extraInformation = mapper.readValue(split[1],tClass);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        public Goal<T> toGoal(){
            return new Goal<>(expression, extraInformation);
        }
    }

    private static class DataAssms<T> {

        public DataAssms(Class<T> tClass){
            this.tClass = tClass;
        }

        public DataAssms(String input, Class<T> clazz){
            String[] split = input.split("#@#");
            this.tClass = clazz;
            this.expression = split[0];
            ObjectMapper mapper = new Jackson2ObjectMapperFactoryBean().getObject();
            try {
                this.extraInformation = mapper.readValue(split[1],tClass);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        public Assumption<T> toAssm(){
            return new Assumption<>(expression, extraInformation);
        }

        private final Class<T> tClass;
        private String expression;
        private T extraInformation;
    }

    private static class RuleCollector<T> implements Collector<String,DataRule<T>,Rule<T>>{

        private Class<T> tClass;
        private State state = null;

        private enum State{NAME,ASSMS,GOAL,CLASS}

        @Override
        public Supplier<DataRule<T>> supplier() {
            return DataRule::new;
        }

        @Override
        public BiConsumer<DataRule<T>, String> accumulator() {
            return (dt,input) -> {
                switch (input){
                    case "#CLASS#" -> state = State.CLASS;
                    case "#NAME#" -> state = State.NAME;
                    case "#ASSMS#" -> state = State.ASSMS;
                    case "#GOAL#" -> state = State.GOAL;
                    case "#END#" -> {}
                    default -> {
                        switch (state){
                            case CLASS -> {
                                try {
                                    tClass = (Class<T>)Class.forName(input);
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                            case GOAL -> dt.setGoal(new DataGoal<>(input, tClass));
                            case NAME -> dt.setName(input);
                            case ASSMS -> dt.add(new DataAssms<>(input, tClass));
                        }
                    }
                }
            };
        }

        @Override
        public BinaryOperator<DataRule<T>> combiner() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Function<DataRule<T>, Rule<T>> finisher() {
            return (data) -> new Rule<>(data.getAssms().stream().map(DataAssms::toAssm).collect(Collectors.toList()),data.getGoal().toGoal(),data.getName(),data.getName(),0, false, false);
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Set.of();
        }
    }
}
