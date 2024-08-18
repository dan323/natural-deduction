package com.dan323.uses.classical;

import com.dan323.uses.LogicalGetActions;
import com.dan323.uses.LogicalSolver;
import com.dan323.uses.ProofParser;
import com.dan323.uses.Transformer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClassicalConfiguration {

    @Bean
    public LogicalSolver classicalSolver() {
        return new LogicalSolver(classicalTransformer());
    }

    @Bean
    public LogicalGetActions classicalActions() {
        return new ClassicGetActions();
    }

    @Bean
    public ProofParser classicalProofParser() {
        return new ParseClassicalProof();
    }

    @Bean
    public Transformer classicalTransformer() {
        return new ClassicalProofTransformer();
    }

}
