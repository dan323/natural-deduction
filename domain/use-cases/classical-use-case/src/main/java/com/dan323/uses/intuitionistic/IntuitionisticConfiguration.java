package com.dan323.uses.intuitionistic;

import com.dan323.uses.LogicalExercises;
import com.dan323.uses.LogicalGetActions;
import com.dan323.uses.ProofParser;
import com.dan323.uses.Transformer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Intuitionistic propositional logic: the classical language, proofs and rules without double negation elimination.
 */
@Configuration
public class IntuitionisticConfiguration {

    @Bean
    public LogicalGetActions intuitionisticActions() {
        return new IntuitionisticGetActions();
    }

    @Bean
    public ProofParser intuitionisticProofParser() {
        return new ParseIntuitionisticProof();
    }

    @Bean
    public Transformer intuitionisticTransformer() {
        return new IntuitionisticProofTransformer();
    }

    @Bean
    public LogicalExercises intuitionisticExercises() {
        return new IntuitionisticExercises();
    }
}
