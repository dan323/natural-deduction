package com.dan323.uses.firstorder;

import com.dan323.uses.LogicalExercises;
import com.dan323.uses.LogicalGetActions;
import com.dan323.uses.LogicalTheories;
import com.dan323.uses.ProofParser;
import com.dan323.uses.Transformer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@code first-order}: first-order logic with equality, with function symbols, predicates, {@code =} and the
 * quantifiers {@code forall x. A} and {@code exists x. A}. Its solver is a best-effort proof search
 * ({@code FirstOrderNaturalDeduction.automate()}). Its one theory is {@code group}.
 */
@Configuration
public class FirstOrderConfiguration {

    /** The name of this logic in the URLs, {@code /logic/first-order/...}. */
    public static final String LOGIC = "first-order";

    @Bean
    public LogicalGetActions firstOrderActions() {
        return new FirstOrderGetActions();
    }

    @Bean
    public Transformer firstOrderTransformer() {
        return new FirstOrderProofTransformer();
    }

    @Bean
    public ProofParser firstOrderProofParser() {
        return new FirstOrderProofParser();
    }

    @Bean
    public LogicalExercises firstOrderExercises() {
        return new FirstOrderExercises();
    }

    @Bean
    public LogicalTheories firstOrderTheories() {
        return new FirstOrderTheories();
    }
}
